package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.SchemaMetadataMapBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the map {@link SchemaMetadataMapBuilder} builds from the XSD is exactly the hand
 * written one: same elements, same order, same Java class, same cardinality, same expandability.
 * <p>
 * This is the safety net of the migration. While it is green, serving the maps from the builder
 * changes nothing that sedalib or ReSIP can observe; the day the hand written maps are gone, this
 * test goes with them.
 */
class SchemaMetadataMapBuilderTest {

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void buildsExactlyTheHandWrittenMaps(SedaVersion version) throws Exception {
        List<String> differences = new ArrayList<>();

        for (Class<?> clazz : SchemaMetadataMapBuilder.getXsdTypes().keySet()) {
            Map<String, ComplexListMetadataKind> handWritten = ComplexListInterface.metadataDefinitions(clazz).get(
                version
            );
            LinkedHashMap<String, ComplexListMetadataKind> built = SchemaMetadataMapBuilder.build(clazz, version);
            if (handWritten == null || built == null) continue;
            String name = clazz.getSimpleName();

            if (!new ArrayList<>(handWritten.keySet()).equals(new ArrayList<>(built.keySet()))) differences.add(
                name +
                " : éléments ou ordre différents\n      écrite    " +
                handWritten.keySet() +
                "\n      construite" +
                built.keySet()
            );
            for (Map.Entry<String, ComplexListMetadataKind> e : built.entrySet()) {
                ComplexListMetadataKind expected = handWritten.get(e.getKey());
                if (expected == null) continue;
                if (!expected.getMetadataClass().equals(e.getValue().getMetadataClass())) differences.add(
                    name +
                    "." +
                    e.getKey() +
                    " : classe " +
                    expected.getMetadataClass().getSimpleName() +
                    ", construite " +
                    e.getValue().getMetadataClass().getSimpleName()
                );
                if (!expected.getClass().equals(e.getValue().getClass())) differences.add(
                    name +
                    "." +
                    e.getKey() +
                    " : nature " +
                    expected.getClass().getSimpleName() +
                    ", construite " +
                    e.getValue().getClass().getSimpleName()
                );
                if (expected.isMany() != e.getValue().isMany()) differences.add(
                    name +
                    "." +
                    e.getKey() +
                    " : cardinalité " +
                    expected.isMany() +
                    ", construite " +
                    e.getValue().isMany()
                );
            }

            Boolean expandable = ComplexListInterface.notExpandableFlags(clazz).get(version);
            if (expandable != null && expandable == SchemaMetadataMapBuilder.isExpandable(clazz)) differences.add(
                name + " : expandable " + !expandable + ", construite " + SchemaMetadataMapBuilder.isExpandable(clazz)
            );
        }

        assertThat(differences)
            .as(
                "Écarts entre les metadataMap écrites et celles construites depuis la XSD " +
                version +
                "\n" +
                String.join("\n", differences)
            )
            .isEmpty();
    }
}
