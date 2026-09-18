package fr.gouv.vitam.tools.sedaschema.node;

import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SedaNodeTest {

    @Test
    void readsValidatesAndRewritesAManifest() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_1);
        SedaNode root;
        try (InputStream in = getClass().getResourceAsStream("/SampleWithLinkFlatManifest.xml")) {
            root = new SedaNodeReader(schema).read(in);
        }

        assertThat(root.getName()).isEqualTo("ArchiveTransfer");
        assertThat(root.getAttribute("xml:id")).isEqualTo("ID1");
        assertThat(root.find("DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content/Title").getText()).isNotBlank();
        assertThat(NodeValidator.validate(root)).isEmpty();

        String xml = new SedaNodeWriter().toXml(root);
        SedaNode reread = new SedaNodeReader(schema).read(new StringReader(xml));
        assertThat(new SedaNodeWriter().toXml(reread)).isEqualTo(xml);
        assertThat(xml).contains("xmlns=\"" + schema.getTargetNamespace() + "\"").contains("xml:id=\"ID1\"");
    }

    @Test
    void createsMinimalValidInstances() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef auDef = schema.getElementAt("ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit");

        SedaNode au = StructureHelper.createDefault(auDef);

        // first choice alternative is ArchiveUnitRefId: minimal but legal
        assertThat(au.getChildren()).extracting(SedaNode::getName).containsExactly("ArchiveUnitRefId");
        assertThat(au.getAttribute("id")).isNotNull();
        assertThat(NodeValidator.validate(au)).isEmpty();
    }

    @Test
    void proposesOnlyLegalChildrenAndInsertsInSchemaOrder() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef auDef = schema.getElementAt("ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit");
        SedaNode au = new SedaNode(auDef).setAttribute("id", "ID1");

        List<StructureHelper.Candidate> before = StructureHelper.addableChildren(au);
        assertThat(before)
            .extracting(StructureHelper.Candidate::getName)
            .contains("ArchiveUnitRefId", "Management", "Content");

        SedaNode content = StructureHelper.add(au, "Content");
        StructureHelper.add(au, "Management");
        assertThat(au.getChildren()).extracting(SedaNode::getName).containsExactly("Management", "Content");

        List<StructureHelper.Candidate> after = StructureHelper.addableChildren(au);
        assertThat(after)
            .extracting(StructureHelper.Candidate::getName)
            .doesNotContain("ArchiveUnitRefId", "Management", "Content")
            .contains("ArchiveUnit");
        assertThatThrownBy(() -> StructureHelper.add(au, "Management")).isInstanceOf(SedaSchemaException.class);

        // nothing is required in Content, and every child lands at its schema position
        assertThat(content.getChildren()).isEmpty();
        StructureHelper.add(content, "Title").setText("Un titre");
        StructureHelper.add(content, "Description").setText("d");
        StructureHelper.add(content, "DescriptionLevel").setText("Item");
        StructureHelper.add(content, "Title").setText("Autre titre");
        assertThat(content.getChildren())
            .extracting(SedaNode::getName)
            .containsExactly("DescriptionLevel", "Title", "Title", "Description");
        assertThat(StructureHelper.addableChildren(content))
            .extracting(StructureHelper.Candidate::getName)
            .doesNotContain("DescriptionLevel");
        assertThat(NodeValidator.validate(au)).isEmpty();
    }

    @Test
    void reportsStructuralProblems() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef contentDef = schema.getElementAt(
            "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content"
        );
        SedaNode content = new SedaNode(contentDef);
        content.addChild(new SedaNode(contentDef.getComplexType().getChildElement("Description")).setText("d"));
        content.addChild(
            new SedaNode(contentDef.getComplexType().getChildElement("DescriptionLevel")).setText("Bidule")
        );

        List<NodeValidator.Problem> problems = NodeValidator.validate(content);

        assertThat(problems)
            .extracting(NodeValidator.Problem::getMessage)
            .anySatisfy(m -> assertThat(m).contains("hors énumération"));
        assertThat(problems)
            .extracting(NodeValidator.Problem::getMessage)
            .anySatisfy(m -> assertThat(m).contains("inattendu"));
    }

    @Test
    void keepsUndeclaredContentAcceptedByWildcards() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef agencyDef = schema.getElementAt("ArchiveTransfer/ArchivalAgency");
        String xml =
            "<ArchivalAgency xmlns=\"" +
            schema.getTargetNamespace() +
            "\"><Identifier>SAE</Identifier>" +
            "<OrganizationDescriptiveMetadata><x:Extra xmlns:x=\"urn:x\">v</x:Extra></OrganizationDescriptiveMetadata></ArchivalAgency>";

        SedaNode agency = new SedaNodeReader(schema).readFragment(agencyDef, new StringReader(xml));

        SedaNode extra = agency.find("OrganizationDescriptiveMetadata/Extra");
        assertThat(extra.isDeclared()).isFalse();
        assertThat(extra.getNamespace()).isEqualTo("urn:x");
        assertThat(extra.getText()).isEqualTo("v");
        assertThat(NodeValidator.validate(agency)).isEmpty();
        assertThat(new SedaNodeWriter(false).toXml(agency)).contains("<ns1:Extra xmlns:ns1=\"urn:x\">v</ns1:Extra>");
    }

    @Test
    void rejectsUndeclaredElementsWithoutWildcard() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        String xml = "<ArchiveTransfer xmlns=\"" + schema.getTargetNamespace() + "\"><Nope/></ArchiveTransfer>";

        assertThatThrownBy(() -> new SedaNodeReader(schema).read(new StringReader(xml)))
            .isInstanceOf(SedaSchemaException.class)
            .hasMessageContaining("Nope");
    }
}
