package fr.gouv.vitam.tools.sedalib.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

// test ArchveUnitProfile
class ArchiveUnitProfileTest {

    @Test
    void testConstructor() {

        // Given
        ArchiveUnitProfile aup = new ArchiveUnitProfile("AUP-00001");

        // When
        String out = aup.toString();

        // Then
        String testOut = "<ArchiveUnitProfile>AUP-00001</ArchiveUnitProfile>";
        assertThat(out).isEqualTo(testOut);
    }

}