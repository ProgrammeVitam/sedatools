package fr.gouv.vitam.tools.sedaschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SedaVersionTest {

    @Test
    void parsesMinorOnlyVersions() {
        assertThat(SedaVersion.from("1")).isEqualTo(SedaVersion.V2_1);
        assertThat(SedaVersion.from("2")).isEqualTo(SedaVersion.V2_2);
        assertThat(SedaVersion.from("3")).isEqualTo(SedaVersion.V2_3);
    }

    @Test
    void parsesMajorMinorVersions() {
        assertThat(SedaVersion.from("2.1")).isEqualTo(SedaVersion.V2_1);
        assertThat(SedaVersion.from("2.2")).isEqualTo(SedaVersion.V2_2);
        assertThat(SedaVersion.from("2.3")).isEqualTo(SedaVersion.V2_3);
    }

    @Test
    void refusesUnsupportedVersions() {
        // no XSD is bundled for the 2.0, which is not supported
        assertThatThrownBy(() -> SedaVersion.from("0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported SEDA version: 2.0");
        assertThatThrownBy(() -> SedaVersion.from("4"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported SEDA version: 2.4");
        assertThatThrownBy(() -> SedaVersion.from("3.0"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported SEDA version: 3.0");
    }

    @Test
    void refusesInvalidFormats() {
        assertThatThrownBy(() -> SedaVersion.from("2.1.0")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SedaVersion.from("a")).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> SedaVersion.from("2.a")).isInstanceOf(NumberFormatException.class);
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void isStoredAndReadBackByItsLabel(SedaVersion version) {
        // the label is the form the version is stored in, in preferences and saved sessions
        assertThat(version.toString()).isEqualTo(version.getMajor() + "." + version.getMinor());
        assertThat(SedaVersion.from(version.toString())).isEqualTo(version);
        assertThat(version.displayString()).isEqualTo("SEDA " + version.getLabel());
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void isFoundByItsNamespace(SedaVersion version) {
        assertThat(version.getNamespace()).isEqualTo("fr:gouv:culture:archivesdefrance:seda:v" + version.getLabel());
        assertThat(SedaVersion.fromNamespace(version.getNamespace())).isEqualTo(version);
    }

    @Test
    void comparesAsVersionsDo() {
        assertThat(SedaVersion.V2_2.isBetween(null, null)).isTrue();
        assertThat(SedaVersion.V2_2.isBetween(SedaVersion.V2_2, SedaVersion.V2_2)).isTrue();
        assertThat(SedaVersion.V2_2.isBetween(SedaVersion.V2_3, null)).isFalse();
        assertThat(SedaVersion.V2_3.isBetween(SedaVersion.V2_1, null)).isTrue();
        assertThat(SedaVersion.V2_3.isBetween(null, SedaVersion.V2_2)).isFalse();
    }
}
