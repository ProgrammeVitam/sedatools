package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.readFileToString;
import static org.assertj.core.api.Assertions.assertThat;

public class SEDAValidationTest {

    @Test
    public void testSeda2_1ArchiveTransferCompliance() throws SEDALibException {

        // Given
        String sedaFilePath = "src/test/resources/xml/seda2_1ArchiveTransfer.xml"; // Modify with actual path
        SEDA2Version.setSeda2Version(1);
        SEDAXMLValidator validator = new SEDAXMLValidator();
        String sedaContent = readFileToString(sedaFilePath);

        // When
        boolean isValid = validator.checkWithXSDSchema(sedaContent, SEDAXMLValidator.getSEDASchema());

        // Then
        assertThat(isValid).as("Validation process on SEDA 2.1 failed").isTrue();
    }

    // TODO test seda 2.2 et 2.3
}
