# Local Environment Setup & Debugging Log

This document records the errors encountered during the initial local setup of the **Right Hand** project, their root causes, and how they were resolved.

---

## 1. Port 5432 Conflict (PostgreSQL)

### Symptom / Error Message
When running `sudo docker compose up -d`, the PostgreSQL container failed to start:
```text
Error response from daemon: failed to set up container networking: driver failed programming external connectivity on endpoint right-hand-postgres (...): failed to bind host port 0.0.0.0:5432/tcp: address already in use
```

### Root Cause
A local installation of PostgreSQL (version 18) was already running on the host machine as a system service, occupying port `5432`. Since two applications cannot bind to the same port on the host network interfaces simultaneously, the Docker container failed to bind port `5432`.

### Resolution
1. **Stop the local host service**: Free up the port on the host machine:
   ```bash
   sudo systemctl stop postgresql
   ```
2. **Recreate the containers**: Once the port was free, the Docker containers were recreated to bind the port correctly:
   ```bash
   sudo docker compose down
   sudo docker compose up -d
   ```

---

## 2. Missing Maven Wrapper (`mvnw`) in Root

### Symptom / Error Message
When running the `run.sh` script from the root directory:
```text
./run.sh: line 14: ./mvnw: No such file or directory
```

### Root Cause
The Maven wrapper configuration files (`mvnw` and the `.mvn` directory) were located inside the `right-hand-api` sub-module directory, rather than the root directory where the script was being executed.

### Resolution
1. **Copy the wrapper to the root**:
   ```bash
   cp -r right-hand-api/.mvn .
   cp right-hand-api/mvnw .
   ```
2. **Make it executable**:
   ```bash
   chmod +x mvnw
   ```

---

## 3. Dependency Resolution Failure in Maven Multi-Module Build

### Symptom / Error Message
When starting the API gateway from the root directory:
```text
[WARNING] The POM for com.yordanos_bekele:right-hand-platform:jar:0.0.1-SNAPSHOT is missing, no dependency information available
...
[ERROR] Failed to execute goal on project right-hand-api: Could not resolve dependencies for project com.yordanos_bekele:right-hand-api:jar:0.0.1-SNAPSHOT: Could not find artifact com.yordanos_bekele:right-hand-platform:jar:0.0.1-SNAPSHOT
```
And if we added the `-am` (also-make) flag (`./mvnw spring-boot:run -pl right-hand-api -am`), it failed with:
```text
[ERROR] Failed to execute goal org.springframework.boot:spring-boot-maven-plugin:4.0.6:run (default-cli) on project right-hand: Unable to find a suitable main class, please add a 'mainClass' property
```

### Root Cause
1. **Dependency Resolution**: The `right-hand-api` module depends on other sibling modules (such as `right-hand-platform`, `right-hand-people`, etc.). When running Maven specifically for the API module (`-pl right-hand-api`), Maven looks for these sibling dependencies in your local Maven repository (`~/.m2/repository`). Since they had not yet been compiled or installed locally, the build failed.
2. **Parent POM Run Error**: Using the `-am` (also-make) flag alongside direct goal execution (`spring-boot:run`) includes the parent POM in the reactor target list (since child modules depend on their parent POM). Since the parent POM has `<packaging>pom</packaging>` and no main class, the Spring Boot plugin fails trying to run it.

### Resolution
The two steps must be separated:
1. **Compile and Install Sibling Modules**: Run `clean install` on the root project with tests skipped to package and install all modules into the local Maven cache:
   ```bash
   ./mvnw clean install -DskipTests
   ```
2. **Run the API Module Only**: Run the Spring Boot plugin only on the `right-hand-api` project *without* the `-am` flag (since the sibling dependencies are now successfully resolved from the local repository cache):
   ```bash
   ./mvnw spring-boot:run -pl right-hand-api
   ```

