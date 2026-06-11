package com.yordanos_bekele.righthand.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.yordanos_bekele.righthand");

    @Test
    void domainDoesNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void modulesDoNotImportOtherModulesInfrastructure() {
        String[] modules = {
                "identity", "people", "interactions", "events",
                "gifts", "intelligence", "timeline", "platform"
        };

        for (String module : modules) {
            noClasses()
                    .that().resideInAPackage("..righthand." + module + "..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(otherInfrastructurePackages(module, modules))
                    .check(classes);
        }
    }

    private String[] otherInfrastructurePackages(String currentModule, String[] modules) {
        return java.util.Arrays.stream(modules)
                .filter(module -> !module.equals(currentModule))
                .map(module -> "..righthand." + module + ".infrastructure..")
                .toArray(String[]::new);
    }
}
