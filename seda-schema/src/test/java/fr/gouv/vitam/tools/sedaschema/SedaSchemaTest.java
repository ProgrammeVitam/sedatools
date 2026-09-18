package fr.gouv.vitam.tools.sedaschema;

import fr.gouv.vitam.tools.sedaschema.model.AttributeDef;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Occurrence;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.Wildcard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SedaSchemaTest {

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void loadsEveryBundledVersion(SedaVersion version) throws Exception {
        SedaSchema schema = SedaSchema.of(version);

        assertThat(schema.getTargetNamespace()).isEqualTo(version.getNamespace());
        assertThat(schema.getVersion()).isEqualTo(version);
        assertThat(schema.getRootElement("ArchiveTransfer")).isNotNull();
        assertThat(schema.getRootElement("ArchiveDeliveryRequestReply")).isNotNull();
        assertThat(schema.getComplexType("ArchiveUnitType")).isNotNull();
        assertThat(schema.getNamedTypes().size()).isGreaterThan(80);
        assertThat(SedaSchema.of(version)).isSameAs(schema);
    }

    @Test
    void resolvesElementPathsThroughContentModels() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);

        ElementDef title = schema.getElementAt(
            "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content/Title"
        );

        assertThat(title.getType().getName()).isEqualTo("TextType");
        assertThat(title.hasTextContent()).isTrue();
        assertThat(title.getComplexType().getAttributes()).extracting(AttributeDef::getName).contains("lang");
        assertThatThrownBy(() -> schema.getElementAt("ArchiveTransfer/Nope")).isInstanceOf(SedaSchemaException.class);
    }

    @Test
    void exposesEnumerationsAndCardinalities() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef content = schema.getElementAt(
            "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content"
        );
        ComplexTypeDef ct = content.getComplexType();

        SimpleTypeDef level = ct.getChildElement("DescriptionLevel").getTextType();
        assertThat(level.isEnumeration()).isTrue();
        assertThat(level.getEnumeration()).contains("Fonds", "Item", "File");
        assertThat(ct.getChildElements())
            .extracting(ElementDef::getName)
            .containsSubsequence("DescriptionLevel", "Title", "Description");

        Particle titleParticle = findParticle(ct.getContent(), "Title");
        assertThat(titleParticle.getOccurrence().isRequired()).isFalse();
        assertThat(titleParticle.getOccurrence().isUnbounded()).isTrue();
        assertThat(findParticle(ct.getContent(), "DescriptionLevel").getOccurrence().getMax()).isEqualTo(1);
        assertThat(ct.getChildElement("Keyword")).isNotNull();
    }

    @Test
    void keepsWildcardOfOpenTypes() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef content = schema.getElementAt(
            "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content"
        );
        ElementDef orgMetadata = schema.getElementAt("ArchiveTransfer/ArchivalAgency/OrganizationDescriptiveMetadata");

        assertThat(content.getComplexType().acceptsWildcard()).isFalse();
        assertThat(orgMetadata.getComplexType().getBaseTypeName()).isEqualTo("OpenType");
        assertThat(orgMetadata.getComplexType().acceptsWildcard()).isTrue();
        Particle any = findWildcard(orgMetadata.getComplexType().getContent());
        assertThat(any.getWildcard().getConstraint()).isEqualTo(Wildcard.NamespaceConstraint.NOT);
        assertThat(any.getWildcard().accepts("urn:other")).isTrue();
        assertThat(any.getWildcard().accepts(schema.getTargetNamespace())).isFalse();
        assertThat(any.getOccurrence().getMax()).isEqualTo(Occurrence.UNBOUNDED);
    }

    @Test
    void handlesRecursiveArchiveUnits() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef au = schema.getElementAt("ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit");

        ElementDef nested = au.getComplexType().getChildElement("ArchiveUnit");
        assertThat(nested.getType()).isSameAs(au.getType());
        assertThat(au.getComplexType().getChildElements())
            .extracting(ElementDef::getName)
            .contains("ArchiveUnitRefId", "Management", "Content");
    }

    @Test
    void carriesDocumentation() throws Exception {
        SedaSchema schema = SedaSchema.of(SedaVersion.V2_3);
        ElementDef title = schema.getElementAt(
            "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit/Content/Title"
        );

        assertThat(title.getDocumentation()).isNotBlank();
    }

    static Particle findParticle(Particle p, String name) {
        if (p.isElement()) return p.getElement().getName().equals(name) ? p : null;
        if (p.isGroup()) for (Particle c : p.getGroup().getParticles()) {
            Particle found = findParticle(c, name);
            if (found != null) return found;
        }
        return null;
    }

    static Particle findWildcard(Particle p) {
        if (p.isWildcard()) return p;
        if (p.isGroup()) for (Particle c : p.getGroup().getParticles()) {
            Particle found = findWildcard(c);
            if (found != null) return found;
        }
        return null;
    }
}
