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
package fr.gouv.vitam.tools.sedaschema;

import fr.gouv.vitam.tools.sedaschema.model.AttributeDef;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Group;
import fr.gouv.vitam.tools.sedaschema.model.Occurrence;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.TypeDef;
import fr.gouv.vitam.tools.sedaschema.model.Wildcard;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.util.XMLCatalogResolver;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link SedaSchema} from the Xerces XSModel of an XSD.
 * <p>
 * Named types are built once and shared, anonymous types are built in place. Recursive types
 * (ArchiveUnit in ArchiveUnit) are handled by registering a type before filling it.
 */
final class SedaSchemaLoader {

    private static final String CATALOG_RESOURCE = "sedaschema/xsd/catalog.xml";
    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private final Map<XSTypeDefinition, TypeDef> builtTypes = new IdentityHashMap<>();
    private final Map<String, SimpleTypeDef> builtInSimpleTypes = new HashMap<>();

    SedaSchema load(URL mainXsd, SedaVersion version) throws SedaSchemaException {
        XSModel model = loadModel(mainXsd);
        String targetNamespace = findTargetNamespace(model);
        if (version == null) version = SedaVersion.fromNamespace(targetNamespace);

        Map<String, TypeDef> namedTypes = new LinkedHashMap<>();
        XSNamedMap types = model.getComponentsByNamespace(XSConstants.TYPE_DEFINITION, targetNamespace);
        for (int i = 0; i < types.getLength(); i++) {
            XSTypeDefinition t = (XSTypeDefinition) types.item(i);
            namedTypes.put(t.getName(), buildType(t));
        }

        Map<String, ElementDef> globalElements = new LinkedHashMap<>();
        XSNamedMap elements = model.getComponentsByNamespace(XSConstants.ELEMENT_DECLARATION, targetNamespace);
        for (int i = 0; i < elements.getLength(); i++) {
            globalElements.put(elements.item(i).getName(), buildElement((XSElementDeclaration) elements.item(i)));
        }
        return new SedaSchema(targetNamespace, version, globalElements, namedTypes);
    }

    private XSModel loadModel(URL mainXsd) throws SedaSchemaException {
        XMLSchemaLoader loader = new XMLSchemaLoader();
        URL catalog = SedaSchemaLoader.class.getClassLoader().getResource(CATALOG_RESOURCE);
        if (catalog != null) loader.setEntityResolver(
            new XMLCatalogResolver(new String[] { catalog.toString() }, false)
        );
        List<String> errors = new ArrayList<>();
        loader.setErrorHandler(new CollectingErrorHandler(errors));
        XSModel model = loader.loadURI(mainXsd.toString());
        if (model == null || !errors.isEmpty()) throw new SedaSchemaException(
            "Impossible de charger le schéma " + mainXsd + (errors.isEmpty() ? "" : " : " + String.join("; ", errors))
        );
        return model;
    }

    private static String findTargetNamespace(XSModel model) throws SedaSchemaException {
        StringList namespaces = model.getNamespaces();
        for (int i = 0; i < namespaces.getLength(); i++) {
            String ns = namespaces.item(i);
            if (ns != null && !ns.startsWith("http://www.w3.org/")) return ns;
        }
        throw new SedaSchemaException("Aucun espace de nom cible trouvé dans le schéma");
    }

    // Elements

    private ElementDef buildElement(XSElementDeclaration decl) throws SedaSchemaException {
        ElementDef def = new ElementDef(decl.getName(), decl.getNamespace());
        def.setGlobal(decl.getScope() == XSConstants.SCOPE_GLOBAL);
        def.setAbstract(decl.getAbstract());
        def.setNillable(decl.getNillable());
        if (decl.getConstraintType() == XSConstants.VC_DEFAULT) def.setDefaultValue(decl.getConstraintValue());
        if (decl.getConstraintType() == XSConstants.VC_FIXED) def.setFixedValue(decl.getConstraintValue());
        def.setDocumentation(documentation(decl.getAnnotations()));
        def.setType(buildType(decl.getTypeDefinition()));
        return def;
    }

    // Types

    private TypeDef buildType(XSTypeDefinition t) throws SedaSchemaException {
        TypeDef built = builtTypes.get(t);
        if (built != null) return built;
        if (t.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) return buildSimpleType((XSSimpleTypeDefinition) t);
        return buildComplexType((XSComplexTypeDefinition) t);
    }

    private SimpleTypeDef buildSimpleType(XSSimpleTypeDefinition t) throws SedaSchemaException {
        if (XSD_NS.equals(t.getNamespace())) {
            SimpleTypeDef builtIn = builtInSimpleTypes.get(t.getName());
            if (builtIn != null) return builtIn;
        }
        SimpleTypeDef.Variety variety;
        switch (t.getVariety()) {
            case XSSimpleTypeDefinition.VARIETY_LIST:
                variety = SimpleTypeDef.Variety.LIST;
                break;
            case XSSimpleTypeDefinition.VARIETY_UNION:
                variety = SimpleTypeDef.Variety.UNION;
                break;
            default:
                variety = SimpleTypeDef.Variety.ATOMIC;
        }
        XSSimpleTypeDefinition primitive = t.getPrimitiveType();
        SimpleTypeDef def = new SimpleTypeDef(
            t.getAnonymous() ? null : t.getName(),
            t.getNamespace(),
            variety,
            primitive == null ? null : primitive.getName(),
            builtInBase(t)
        );
        builtTypes.put(t, def);
        if (XSD_NS.equals(t.getNamespace())) builtInSimpleTypes.put(t.getName(), def);
        def.setDocumentation(documentation(t.getAnnotations()));
        def.setEnumeration(toList(t.getLexicalEnumeration()));
        def.setPatterns(toList(t.getLexicalPattern()));
        def.setFacets(facets(t));
        if (t.getItemType() != null) def.setItemType(buildSimpleType(t.getItemType()));
        XSObjectList members = t.getMemberTypes();
        if (members != null && members.getLength() > 0) {
            List<SimpleTypeDef> memberDefs = new ArrayList<>();
            for (int i = 0; i < members.getLength(); i++) memberDefs.add(
                buildSimpleType((XSSimpleTypeDefinition) members.item(i))
            );
            def.setMemberTypes(memberDefs);
        }
        return def;
    }

    private static String builtInBase(XSSimpleTypeDefinition t) {
        XSTypeDefinition current = t;
        while (current != null && !XSD_NS.equals(current.getNamespace())) current = current.getBaseType();
        return current == null ? null : current.getName();
    }

    private static Map<String, String> facets(XSSimpleTypeDefinition t) {
        Map<String, String> facets = new LinkedHashMap<>();
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_LENGTH, "length");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MINLENGTH, "minLength");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MAXLENGTH, "maxLength");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MININCLUSIVE, "minInclusive");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MAXINCLUSIVE, "maxInclusive");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MINEXCLUSIVE, "minExclusive");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE, "maxExclusive");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_TOTALDIGITS, "totalDigits");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_FRACTIONDIGITS, "fractionDigits");
        putFacet(facets, t, XSSimpleTypeDefinition.FACET_WHITESPACE, "whiteSpace");
        return facets;
    }

    private static void putFacet(Map<String, String> facets, XSSimpleTypeDefinition t, short facet, String name) {
        if ((t.getDefinedFacets() & facet) != 0) facets.put(name, t.getLexicalFacetValue(facet));
    }

    private ComplexTypeDef buildComplexType(XSComplexTypeDefinition t) throws SedaSchemaException {
        ComplexTypeDef def = new ComplexTypeDef(t.getAnonymous() ? null : t.getName(), t.getNamespace());
        // registered before being filled so that recursive content models terminate
        builtTypes.put(t, def);
        def.setAbstract(t.getAbstract());
        def.setDocumentation(documentation(t.getAnnotations()));
        XSTypeDefinition base = t.getBaseType();
        if (base != null && !base.getAnonymous() && !"anyType".equals(base.getName())) def.setBaseTypeName(
            base.getName()
        );

        switch (t.getContentType()) {
            case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
                def.setContentKind(ComplexTypeDef.ContentKind.SIMPLE);
                def.setSimpleContentType(buildSimpleType(t.getSimpleType()));
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                def.setContentKind(ComplexTypeDef.ContentKind.ELEMENT);
                def.setContent(buildParticle(t.getParticle()));
                break;
            case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                def.setContentKind(ComplexTypeDef.ContentKind.MIXED);
                def.setContent(buildParticle(t.getParticle()));
                break;
            default:
                def.setContentKind(ComplexTypeDef.ContentKind.EMPTY);
        }

        List<AttributeDef> attributes = new ArrayList<>();
        XSObjectList uses = t.getAttributeUses();
        for (int i = 0; i < uses.getLength(); i++) attributes.add(buildAttribute((XSAttributeUse) uses.item(i)));
        def.setAttributes(attributes);
        def.setAttributeWildcard(t.getAttributeWildcard() != null);
        return def;
    }

    private AttributeDef buildAttribute(XSAttributeUse use) throws SedaSchemaException {
        XSAttributeDeclaration decl = use.getAttrDeclaration();
        String defaultValue = null, fixedValue = null;
        short constraint = use.getConstraintType() != XSConstants.VC_NONE
            ? use.getConstraintType()
            : decl.getConstraintType();
        String value = use.getConstraintType() != XSConstants.VC_NONE
            ? use.getConstraintValue()
            : decl.getConstraintValue();
        if (constraint == XSConstants.VC_DEFAULT) defaultValue = value;
        if (constraint == XSConstants.VC_FIXED) fixedValue = value;
        AttributeDef def = new AttributeDef(
            decl.getName(),
            decl.getNamespace(),
            buildSimpleType(decl.getTypeDefinition()),
            use.getRequired(),
            defaultValue,
            fixedValue
        );
        def.setDocumentation(documentation(decl.getAnnotations()));
        return def;
    }

    // Content model

    private Particle buildParticle(XSParticle p) throws SedaSchemaException {
        Occurrence occurrence = new Occurrence(
            p.getMinOccurs(),
            p.getMaxOccursUnbounded() ? Occurrence.UNBOUNDED : p.getMaxOccurs()
        );
        XSTerm term = p.getTerm();
        switch (term.getType()) {
            case XSConstants.ELEMENT_DECLARATION:
                return Particle.of(buildElement((XSElementDeclaration) term), occurrence);
            case XSConstants.MODEL_GROUP:
                return Particle.of(buildGroup((XSModelGroup) term), occurrence);
            case XSConstants.WILDCARD:
                return Particle.of(buildWildcard((XSWildcard) term), occurrence);
            default:
                throw new SedaSchemaException("Terme de particule inattendu " + term.getType());
        }
    }

    private Group buildGroup(XSModelGroup g) throws SedaSchemaException {
        Group.Compositor compositor;
        switch (g.getCompositor()) {
            case XSModelGroup.COMPOSITOR_CHOICE:
                compositor = Group.Compositor.CHOICE;
                break;
            case XSModelGroup.COMPOSITOR_ALL:
                compositor = Group.Compositor.ALL;
                break;
            default:
                compositor = Group.Compositor.SEQUENCE;
        }
        Group group = new Group(compositor);
        XSObjectList particles = g.getParticles();
        for (int i = 0; i < particles.getLength(); i++) group
            .getParticles()
            .add(buildParticle((XSParticle) particles.item(i)));
        return group;
    }

    private static Wildcard buildWildcard(XSWildcard w) {
        Wildcard.NamespaceConstraint constraint;
        switch (w.getConstraintType()) {
            case XSWildcard.NSCONSTRAINT_NOT:
                constraint = Wildcard.NamespaceConstraint.NOT;
                break;
            case XSWildcard.NSCONSTRAINT_LIST:
                constraint = Wildcard.NamespaceConstraint.LIST;
                break;
            default:
                constraint = Wildcard.NamespaceConstraint.ANY;
        }
        Wildcard.ProcessContents process;
        switch (w.getProcessContents()) {
            case XSWildcard.PC_STRICT:
                process = Wildcard.ProcessContents.STRICT;
                break;
            case XSWildcard.PC_SKIP:
                process = Wildcard.ProcessContents.SKIP;
                break;
            default:
                process = Wildcard.ProcessContents.LAX;
        }
        List<String> namespaces = new ArrayList<>();
        StringList list = w.getNsConstraintList();
        if (list != null) for (int i = 0; i < list.getLength(); i++) namespaces.add(
            list.item(i) == null ? "" : list.item(i)
        );
        return new Wildcard(constraint, namespaces, process);
    }

    // Annotations

    private static List<String> toList(StringList list) {
        List<String> result = new ArrayList<>();
        if (list != null) for (int i = 0; i < list.getLength(); i++) result.add(list.item(i));
        return result;
    }

    /**
     * Extracts the text of the xsd:documentation children of the annotations, joined by a blank
     * line.
     */
    private static String documentation(XSObjectList annotations) {
        if (annotations == null || annotations.getLength() == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < annotations.getLength(); i++) {
            XSObject o = annotations.item(i);
            if (!(o instanceof XSAnnotation)) continue;
            String xml = ((XSAnnotation) o).getAnnotationString();
            if (xml == null) continue;
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
                while (reader.hasNext()) {
                    if (
                        reader.next() == XMLStreamConstants.START_ELEMENT &&
                        "documentation".equals(reader.getLocalName())
                    ) {
                        String text = reader.getElementText().trim().replaceAll("\\s*\\n\\s*", "\n");
                        if (!text.isEmpty()) {
                            if (sb.length() > 0) sb.append("\n\n");
                            sb.append(text);
                        }
                    }
                }
                reader.close();
            } catch (Exception e) {
                // an unreadable annotation only loses its documentation
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private static final class CollectingErrorHandler implements org.apache.xerces.xni.parser.XMLErrorHandler {

        private final List<String> errors;

        CollectingErrorHandler(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void warning(String domain, String key, org.apache.xerces.xni.parser.XMLParseException e) {}

        @Override
        public void error(String domain, String key, org.apache.xerces.xni.parser.XMLParseException e) {
            errors.add(e.getMessage());
        }

        @Override
        public void fatalError(String domain, String key, org.apache.xerces.xni.parser.XMLParseException e) {
            errors.add(e.getMessage());
        }
    }
}
