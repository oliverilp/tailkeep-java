package org.tailkeep.domain;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.tailkeep.domain.shared.UseCase;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * The rules Gradle can't check, because they are about packages inside {@code mod-domain}.
 * {@code verifyModuleDependencies} in the root build checks the rules between modules.
 */
class DomainArchitectureTest {

    private static final JavaClasses DOMAIN = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("org.tailkeep.domain");

    @Test
    void featuresDoNotDependOnEachOtherInACycle() {
        slices().matching("org.tailkeep.domain.(*)..")
                .should().beFreeOfCycles()
                .check(DOMAIN);
    }

    @Test
    void sharedDoesNotKnowAnyFeature() {
        noClasses().that().resideInAPackage("org.tailkeep.domain.shared..")
                .should().dependOnClassesThat().resideOutsideOfPackages("org.tailkeep.domain.shared..", "java..")
                .check(DOMAIN);
    }

    @Test
    void useCasesAreImplClasses() {
        classes().that().areAnnotatedWith(UseCase.class)
                .should().haveSimpleNameEndingWith("Impl")
                .check(DOMAIN);
    }

    @Test
    void useCasesHaveOnePublicMethodCalledExecute() {
        methods().that().areDeclaredInClassesThat().areAnnotatedWith(UseCase.class)
                .and().arePublic()
                .should().haveName("execute")
                .check(DOMAIN);
    }

    @Test
    void timeComesFromAClock() {
        noClasses().should().callMethod(java.time.LocalDateTime.class, "now")
                .orShould().callMethod(java.time.Instant.class, "now")
                .orShould().callMethod(System.class, "currentTimeMillis")
                .check(DOMAIN);
    }
}
