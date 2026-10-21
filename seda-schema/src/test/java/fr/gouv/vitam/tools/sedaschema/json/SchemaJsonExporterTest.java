package fr.gouv.vitam.tools.sedaschema.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaJsonExporterTest {

    @Test
    void exportsRootsAndNamedTypes() throws Exception {
        String json = new SchemaJsonExporter(false).toJson(SedaSchema.of(SedaVersion.V2_3));
        JsonNode tree = new ObjectMapper().readTree(json);

        assertThat(tree.get("version").asText()).isEqualTo("2.3");
        assertThat(tree.get("roots").get("ArchiveTransfer").get("type").asText()).isEqualTo("ArchiveTransferType");
        JsonNode au = tree.get("types").get("ArchiveUnitType");
        assertThat(au.get("kind").asText()).isEqualTo("complex");
        assertThat(au.get("model").get("group").get("compositor").asText()).isEqualTo("choice");
        assertThat(tree.get("types").get("LevelType").get("enumeration")).isNotEmpty();
        assertThat(json).contains("\"type\" : \"xsd:date\"").doesNotContain("\"type\" : \"date\"");
        assertThat(json).doesNotContain("\"doc\"");
        assertThat(new SchemaJsonExporter().toJson(SedaSchema.of(SedaVersion.V2_3))).contains("\"doc\"");
    }
}
