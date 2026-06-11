package com.yordanos_bekele.righthand.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {

    private final ObjectProvider<JdbcTemplate> jdbcTemplate;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final Environment environment;

    HealthController(
            ObjectProvider<JdbcTemplate> jdbcTemplate,
            ObjectProvider<StringRedisTemplate> redisTemplate,
            Environment environment
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }

    @GetMapping("/health")
    Map<String, Object> health() {
        Map<String, String> dependencies = new LinkedHashMap<>();
        dependencies.put("postgres", checkPostgres());
        dependencies.put("redis", checkRedis());
        dependencies.put("kafka", checkKafka());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", dependencies.values().stream().allMatch("UP"::equals) ? "UP" : "DOWN");
        response.put("dependencies", dependencies);
        return response;
    }

    private String checkPostgres() {
        try {
            Integer result = jdbcTemplate.getObject().queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(result) ? "UP" : "DOWN";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
    }

    private String checkRedis() {
        try {
            String response = redisTemplate.getObject().getConnectionFactory().getConnection().ping();
            return "PONG".equals(response) ? "UP" : "DOWN";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
    }

    private String checkKafka() {
        Map<String, Object> config = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"),
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,
                "1000",
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
                "1000"
        );

        try (AdminClient adminClient = AdminClient.create(config)) {
            adminClient.describeCluster().clusterId().get(1, TimeUnit.SECONDS);
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
