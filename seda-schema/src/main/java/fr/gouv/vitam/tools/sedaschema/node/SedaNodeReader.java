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
package fr.gouv.vitam.tools.sedaschema.node;

import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Reads XML into a {@link SedaNode} tree, resolving each element against the schema.
 * <p>
 * Undeclared elements are kept as undeclared nodes when the content model has a wildcard, and
 * rejected otherwise. Structural validation (order, cardinality) is not done here, see
 * {@link NodeValidator}.
 */
public final class SedaNodeReader {

    private final SedaSchema schema;

    public SedaNodeReader(SedaSchema schema) {
        this.schema = schema;
    }

    /**
     * Reads a whole document whose root must be a global element of the schema.
     */
    public SedaNode read(InputStream in) throws SedaSchemaException {
        try {
            return read(newFactory().createXMLStreamReader(in));
        } catch (XMLStreamException e) {
            throw new SedaSchemaException("XML illisible", e);
        }
    }

    public SedaNode read(Reader in) throws SedaSchemaException {
        try {
            return read(newFactory().createXMLStreamReader(in));
        } catch (XMLStreamException e) {
            throw new SedaSchemaException("XML illisible", e);
        }
    }

    /**
     * Reads an XML fragment whose root is the given declaration (a Content, a Management...).
     */
    public SedaNode readFragment(ElementDef root, Reader in) throws SedaSchemaException {
        try {
            XMLStreamReader reader = newFactory().createXMLStreamReader(in);
            while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT);
            if (!reader.getLocalName().equals(root.getName())) throw new SedaSchemaException(
                "Élément [" + reader.getLocalName() + "] lu, [" + root.getName() + "] attendu"
            );
            return readElement(root, reader);
        } catch (XMLStreamException e) {
            throw new SedaSchemaException("XML illisible", e);
        }
    }

    private SedaNode read(XMLStreamReader reader) throws XMLStreamException, SedaSchemaException {
        while (reader.hasNext() && reader.next() != XMLStreamConstants.START_ELEMENT);
        ElementDef root = schema.getRootElement(reader.getLocalName());
        if (root == null) throw new SedaSchemaException(
            "Élément racine [" + reader.getLocalName() + "] inconnu du schéma"
        );
        if (!schema.getTargetNamespace().equals(reader.getNamespaceURI())) throw new SedaSchemaException(
            "Espace de nom [" +
            reader.getNamespaceURI() +
            "] différent de celui du schéma [" +
            schema.getTargetNamespace() +
            "]"
        );
        return readElement(root, reader);
    }

    private static XMLInputFactory newFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    /**
     * Reads the element the reader is positioned on (START_ELEMENT) up to its END_ELEMENT.
     */
    private SedaNode readElement(ElementDef def, XMLStreamReader reader)
        throws XMLStreamException, SedaSchemaException {
        SedaNode node = new SedaNode(def);
        readAttributes(node, reader);
        ComplexTypeDef ct = def.getComplexType();
        if (ct == null || !ct.hasElementContent()) {
            node.setText(readText(reader));
            return node;
        }
        StringBuilder mixedText = ct.hasTextContent() ? new StringBuilder() : null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) break;
            if (event == XMLStreamConstants.START_ELEMENT) {
                ElementDef childDef = ct.getChildElement(reader.getLocalName());
                if (childDef != null && childDef.getNamespace().equals(reader.getNamespaceURI())) node.addChild(
                    readElement(childDef, reader)
                );
                else if (ct.acceptsWildcard()) node.addChild(readUndeclared(reader));
                else throw new SedaSchemaException(
                    "Élément [" + reader.getLocalName() + "] non déclaré dans [" + node.getPath() + "]"
                );
            } else if (
                mixedText != null && (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA)
            ) {
                mixedText.append(reader.getText());
            }
        }
        if (mixedText != null && mixedText.toString().trim().length() > 0) node.setText(mixedText.toString());
        return node;
    }

    private SedaNode readUndeclared(XMLStreamReader reader) throws XMLStreamException {
        SedaNode node = new SedaNode(reader.getLocalName(), reader.getNamespaceURI());
        readAttributes(node, reader);
        StringBuilder text = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) break;
            if (event == XMLStreamConstants.START_ELEMENT) node.addChild(readUndeclared(reader));
            else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) text.append(
                reader.getText()
            );
        }
        if (node.getChildren().isEmpty()) node.setText(text.toString());
        return node;
    }

    private static void readAttributes(SedaNode node, XMLStreamReader reader) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String prefix = reader.getAttributePrefix(i);
            String name = prefix == null || prefix.isEmpty()
                ? reader.getAttributeLocalName(i)
                : prefix + ":" + reader.getAttributeLocalName(i);
            node.setAttribute(name, reader.getAttributeValue(i));
        }
    }

    private static String readText(XMLStreamReader reader) throws XMLStreamException, SedaSchemaException {
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) break;
            if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA) sb.append(
                reader.getText()
            );
            else if (event == XMLStreamConstants.START_ELEMENT) throw new SedaSchemaException(
                "Élément [" + reader.getLocalName() + "] dans un contenu textuel"
            );
        }
        return sb.toString();
    }
}
