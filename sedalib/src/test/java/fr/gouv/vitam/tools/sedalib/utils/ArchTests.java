/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitam.tools.sedalib.utils;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class ArchTests {


    @Test
    @Disabled("Only for research purpose")
    public void testStaticMethodsOfLocalDateTime() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("fr.gouv.vitam");

        ArchRuleDefinition.noClasses()
                .that()
                .doNotHaveFullyQualifiedName(LocalDateTimeUtil.class.getName())
                .should()
                .callMethodWhere(
                        new DescribedPredicate<>("static methods of LocalDateTime are invoked instead of LocalDateUtil helpers") {
                            @Override
                            public boolean test(JavaMethodCall javaMethodCall) {
                                return javaMethodCall.getTargetOwner().getFullName().equals(LocalDateTime.class.getName())
                                        && javaMethodCall.getTarget().resolveMember().orElseThrow()
                                        .getModifiers().contains(JavaModifier.STATIC);
                            }
                        }
                )
                .check(importedClasses);
    }

    @Test
    public void forbidden_LocalDateTime_toString_format() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("fr.gouv.vitam");

        ArchRuleDefinition.noClasses()
                .that()
                .doNotHaveFullyQualifiedName(LocalDateTimeUtil.class.getName())
                .should()
                .callMethodWhere(
                        new DescribedPredicate<>("LocalDateTime.toString is invoked instead of LocalDateTimeUtil.getFormattedDateTime") {
                            @Override
                            public boolean test(JavaMethodCall javaMethodCall) {
                                return javaMethodCall.getTargetOwner().getFullName().equals(LocalDateTime.class.getName())
                                        && (javaMethodCall.getTarget().getName().equals("toString")
                                        || javaMethodCall.getTarget().getName().equals("format"));
                            }
                        }
                )
                .check(importedClasses);
    }

}