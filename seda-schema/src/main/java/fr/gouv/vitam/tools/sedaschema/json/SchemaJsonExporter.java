/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedaschema.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;
import fr.gouv.vitam.tools.sedaschema.model.AttributeDef;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Group;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.TypeDef;
import fr.gouv.vitam.tools.sedaschema.model.Wildcard;

import java.util.Map;

/**
 * Exports a schema as JSON, for clients that don't read XSD (a web front end generating forms
 * for instance).
 * <p>
 * Named types are exported once under "types" and referenced by name (XSD built-ins as
 * "xsd:date", "xsd:boolean"...), anonymous types are inlined where they are used, so recursive
 * content models stay finite. Shape:
 *
 * <pre>
 * { "targetNamespace", "version",
 *   "roots": { name: element },
 *   "types": { name: type } }
 * element  = { "name", "type": "TypeName" | "xsd:builtin" | type, "nillable"?, "default"?, "fixed"?, "doc"? }
 * type     = { "kind": "simple", "name"?, "base", "primitive", "variety", "enumeration"?, "patterns"?, "facets"?, "itemType"?, "memberTypes"?, "doc"? }
 *          | { "kind": "complex", "name"?, "content": "empty|simple|element|mixed", "abstract"?, "base"?,
 *              "textType"?, "attributes": [attribute], "attributeWildcard"?, "model"?: particle, "doc"? }
 * particle = { "min", "max" (-1 = unbounded), "element": element | "group": { "compositor", "particles": [particle] } | "any": { "constraint", "namespaces", "process" } }
 * </pre>
 */
public final class SchemaJsonExporter {

    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private final JsonNodeFactory f = JsonNodeFactory.instance;
    private final boolean withDocumentation;

    public SchemaJsonExporter() {
        this(true);
    }

    /**
     * @param withDocumentation false to leave out the xsd:documentation texts (much smaller output)
     */
    public SchemaJsonExporter(boolean withDocumentation) {
        this.withDocumentation = withDocumentation;
    }

    public String toJson(SedaSchema schema) throws SedaSchemaException {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(toTree(schema));
        } catch (JsonProcessingException e) {
            throw new SedaSchemaException("Export JSON impossible", e);
        }
    }

    public ObjectNode toTree(SedaSchema schema) {
        ObjectNode root = f.objectNode();
        root.put("targetNamespace", schema.getTargetNamespace());
        if (schema.getVersion() != null) root.put("version", schema.getVersion().getLabel());
        ObjectNode roots = root.putObject("roots");
        for (ElementDef e : schema.getRootElements()) roots.set(e.getName(), element(e));
        ObjectNode types = root.putObject("types");
        for (Map.Entry<String, TypeDef> t : schema.getNamedTypes().entrySet()) types.set(
            t.getKey(),
            type(t.getValue())
        );
        return root;
    }

    private ObjectNode element(ElementDef e) {
        ObjectNode n = f.objectNode();
        n.put("name", e.getName());
        if (e.getType().isAnonymous()) n.set("type", type(e.getType()));
        else n.put("type", typeName(e.getType()));
        if (e.isAbstract()) n.put("abstract", true);
        if (e.isNillable()) n.put("nillable", true);
        if (e.getDefaultValue() != null) n.put("default", e.getDefaultValue());
        if (e.getFixedValue() != null) n.put("fixed", e.getFixedValue());
        doc(n, e.getDocumentation());
        return n;
    }

    private ObjectNode type(TypeDef t) {
        return t.isSimple() ? simpleType((SimpleTypeDef) t) : complexType((ComplexTypeDef) t);
    }

    private ObjectNode typeRef(TypeDef t) {
        if (!t.isAnonymous()) {
            ObjectNode ref = f.objectNode();
            ref.put("ref", typeName(t));
            return ref;
        }
        return type(t);
    }

    /**
     * @return the name a named type is referenced by: "xsd:date" for an XSD built-in, the bare
     * name for a type of the schema (found under "types")
     */
    private static String typeName(TypeDef t) {
        return XSD_NS.equals(t.getNamespace()) ? "xsd:" + t.getName() : t.getName();
    }

    private ObjectNode simpleType(SimpleTypeDef t) {
        ObjectNode n = f.objectNode();
        n.put("kind", "simple");
        if (!t.isAnonymous()) n.put("name", t.getName());
        n.put("variety", t.getVariety().name().toLowerCase());
        if (t.getBuiltInBase() != null) n.put("base", t.getBuiltInBase());
        if (t.getPrimitive() != null) n.put("primitive", t.getPrimitive());
        if (t.isEnumeration()) strings(n.putArray("enumeration"), t.getEnumeration());
        if (!t.getPatterns().isEmpty()) strings(n.putArray("patterns"), t.getPatterns());
        if (!t.getFacets().isEmpty()) {
            ObjectNode facets = n.putObject("facets");
            t.getFacets().forEach(facets::put);
        }
        if (t.getItemType() != null) n.set("itemType", typeRef(t.getItemType()));
        if (!t.getMemberTypes().isEmpty()) {
            ArrayNode members = n.putArray("memberTypes");
            for (SimpleTypeDef m : t.getMemberTypes()) members.add(typeRef(m));
        }
        doc(n, t.getDocumentation());
        return n;
    }

    private ObjectNode complexType(ComplexTypeDef t) {
        ObjectNode n = f.objectNode();
        n.put("kind", "complex");
        if (!t.isAnonymous()) n.put("name", t.getName());
        n.put("content", t.getContentKind().name().toLowerCase());
        if (t.isAbstract()) n.put("abstract", true);
        if (t.getBaseTypeName() != null) n.put("base", t.getBaseTypeName());
        if (t.getSimpleContentType() != null) n.set("textType", typeRef(t.getSimpleContentType()));
        ArrayNode attributes = n.putArray("attributes");
        for (AttributeDef a : t.getAttributes()) attributes.add(attribute(a));
        if (t.hasAttributeWildcard()) n.put("attributeWildcard", true);
        if (t.getContent() != null) n.set("model", particle(t.getContent()));
        doc(n, t.getDocumentation());
        return n;
    }

    private ObjectNode attribute(AttributeDef a) {
        ObjectNode n = f.objectNode();
        n.put("name", a.getName());
        if (a.getNamespace() != null && !a.getNamespace().isEmpty()) n.put("namespace", a.getNamespace());
        n.set("type", typeRef(a.getType()));
        if (a.isRequired()) n.put("required", true);
        if (a.getDefaultValue() != null) n.put("default", a.getDefaultValue());
        if (a.getFixedValue() != null) n.put("fixed", a.getFixedValue());
        doc(n, a.getDocumentation());
        return n;
    }

    private ObjectNode particle(Particle p) {
        ObjectNode n = f.objectNode();
        n.put("min", p.getOccurrence().getMin());
        n.put("max", p.getOccurrence().getMax());
        if (p.isElement()) {
            n.set("element", element(p.getElement()));
        } else if (p.isGroup()) {
            Group g = p.getGroup();
            ObjectNode group = n.putObject("group");
            group.put("compositor", g.getCompositor().name().toLowerCase());
            ArrayNode particles = group.putArray("particles");
            for (Particle c : g.getParticles()) particles.add(particle(c));
        } else {
            Wildcard w = p.getWildcard();
            ObjectNode any = n.putObject("any");
            any.put("constraint", w.getConstraint().name().toLowerCase());
            strings(any.putArray("namespaces"), w.getNamespaces());
            any.put("process", w.getProcessContents().name().toLowerCase());
        }
        return n;
    }

    private void doc(ObjectNode n, String documentation) {
        if (withDocumentation && documentation != null) n.put("doc", documentation);
    }

    private static void strings(ArrayNode array, Iterable<String> values) {
        for (String v : values) array.add(v);
    }
}
