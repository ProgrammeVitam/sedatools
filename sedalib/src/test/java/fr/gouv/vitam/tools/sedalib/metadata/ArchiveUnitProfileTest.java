package fr.gouv.vitam.tools.sedalib.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// test ArchveUnitProfile and SchemeType subclass
class ArchiveUnitProfileTest {

    @Test
    void testSimpleConstructor() {

        // Given
        ArchiveUnitProfile aup = new ArchiveUnitProfile("AUP-00001");

        // When
        String out = aup.toString();
//        System.out.println("Value to verify=" + out);

        // Then
        String testOut = "<ArchiveUnitProfile>AUP-00001</ArchiveUnitProfile>";
        assertThat(out).isEqualTo(testOut);
    }

    @Test
    void testComplexConstructor() {

        // Given
        ArchiveUnitProfile aup = new ArchiveUnitProfile("AUP-00001", "TestShemeAgencyID", "TestSchemeAgencyName", "TestSchemeDataURI",
                "TestSchemeID", "TestSchemeName", "TestSchemeURI", "TestSchemeVersionID");
        // When
        String out = aup.toString();
//        System.out.println("Value to verify=" + out);

        // Then
        String testOut = "<ArchiveUnitProfile schemeAgencyID=\"TestShemeAgencyID\" schemeAgencyName=\"TestSchemeAgencyName\" schemeDataURI=\"TestSchemeDataURI\" schemeID=\"TestSchemeID\" schemeName=\"TestSchemeName\" schemeURI=\"TestSchemeURI\" schemeVersionID=\"TestSchemeVersionID\">AUP-00001</ArchiveUnitProfile>";
        assertThat(out).isEqualTo(testOut);
    }
}