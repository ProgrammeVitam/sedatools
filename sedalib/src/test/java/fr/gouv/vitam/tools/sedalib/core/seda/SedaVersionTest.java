package fr.gouv.vitam.tools.sedalib.core.seda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SedaVersionTest {

    @Test
    void shouldParseMinorOnlyVersions() {
        assertEquals(SedaVersion.V2_0, SedaVersion.from("0"));
        assertEquals(SedaVersion.V2_1, SedaVersion.from("1"));
        assertEquals(SedaVersion.V2_2, SedaVersion.from("2"));
        assertEquals(SedaVersion.V2_3, SedaVersion.from("3"));
    }

    @Test
    void shouldParseMajorMinorVersions() {
        assertEquals(SedaVersion.V2_0, SedaVersion.from("2.0"));
        assertEquals(SedaVersion.V2_1, SedaVersion.from("2.1"));
        assertEquals(SedaVersion.V2_2, SedaVersion.from("2.2"));
        assertEquals(SedaVersion.V2_3, SedaVersion.from("2.3"));
    }

    @Test
    void shouldThrowExceptionForUnsupportedMinorOnlyVersion() {
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SedaVersion.from("4"));

        assertEquals("Unsupported SEDA version: 2.4", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForUnsupportedMajorMinorVersion() {
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> SedaVersion.from("3.0"));

        assertEquals("Unsupported SEDA version: 3.0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> SedaVersion.from("2.1.0"));
        assertThrows(NumberFormatException.class, () -> SedaVersion.from("a"));
        assertThrows(NumberFormatException.class, () -> SedaVersion.from("2.a"));
    }
}