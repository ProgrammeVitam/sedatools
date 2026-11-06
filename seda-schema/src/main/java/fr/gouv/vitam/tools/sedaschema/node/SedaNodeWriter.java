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

import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a {@link SedaNode} tree as XML. The root gets the default namespace declaration and the
 * xml, xsi and xlink prefixes are bound when used.
 */
public final class SedaNodeWriter {

    private static final Map<String, String> KNOWN_PREFIXES = new HashMap<>();

    static {
        KNOWN_PREFIXES.put("xml", "http://www.w3.org/XML/1998/namespace");
        KNOWN_PREFIXES.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        KNOWN_PREFIXES.put("xlink", "http://www.w3.org/1999/xlink");
    }

    private final boolean indent;

    public SedaNodeWriter() {
        this(true);
    }

    public SedaNodeWriter(boolean indent) {
        this.indent = indent;
    }

    public String toXml(SedaNode node) throws SedaSchemaException {
        StringWriter sw = new StringWriter();
        write(node, sw, false);
        return sw.toString();
    }

    /**
     * @param node the root to write
     * @param out the destination
     * @param document true to write an XML declaration first
     */
    public void write(SedaNode node, Writer out, boolean document) throws SedaSchemaException {
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            if (document) writer.writeStartDocument("UTF-8", "1.0");
            writeNode(node, writer, 0, new HashMap<>());
            if (document) writer.writeEndDocument();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new SedaSchemaException("Écriture XML impossible", e);
        }
    }

    /**
     * @param inScope the namespaces already declared by the ancestors, by prefix ("" for the
     * default one); the root declares the default namespace and the known attribute prefixes it
     * uses, a node of a foreign namespace declares it with a generated prefix
     */
    private void writeNode(SedaNode node, XMLStreamWriter w, int depth, Map<String, String> inScope)
        throws XMLStreamException {
        if (indent && depth > 0) newLine(w, depth);
        String ns = node.getNamespace() == null ? "" : node.getNamespace();
        boolean root = depth == 0;
        Map<String, String> scope = inScope;
        String prefix = root ? "" : prefixFor(ns, inScope);
        if (prefix == null) {
            prefix = "ns" + inScope.size();
            scope = new HashMap<>(inScope);
            scope.put(prefix, ns);
        }
        w.writeStartElement(prefix, node.getName(), ns);
        if (root) {
            w.writeDefaultNamespace(ns);
            scope = new HashMap<>(inScope);
            scope.put("", ns);
            for (String attr : node.getAttributes().keySet()) {
                String attrPrefix = prefixOf(attr);
                if (attrPrefix != null && !"xml".equals(attrPrefix) && KNOWN_PREFIXES.containsKey(attrPrefix)) {
                    w.writeNamespace(attrPrefix, KNOWN_PREFIXES.get(attrPrefix));
                    scope.put(attrPrefix, KNOWN_PREFIXES.get(attrPrefix));
                }
            }
        } else if (scope != inScope) {
            w.writeNamespace(prefix, ns);
        }
        for (Map.Entry<String, String> a : node.getAttributes().entrySet()) {
            String attrPrefix = prefixOf(a.getKey());
            if (attrPrefix == null) w.writeAttribute(a.getKey(), a.getValue());
            else w.writeAttribute(
                attrPrefix,
                KNOWN_PREFIXES.getOrDefault(attrPrefix, ""),
                a.getKey().substring(attrPrefix.length() + 1),
                a.getValue()
            );
        }
        if (node.getChildren().isEmpty()) {
            if (node.getText() != null) w.writeCharacters(node.getText());
        } else {
            if (node.getText() != null) w.writeCharacters(node.getText());
            for (SedaNode child : node.getChildren()) writeNode(child, w, depth + 1, scope);
            if (indent) newLine(w, depth);
        }
        w.writeEndElement();
    }

    private static String prefixFor(String namespace, Map<String, String> inScope) {
        for (Map.Entry<String, String> e : inScope.entrySet()) if (e.getValue().equals(namespace)) return e.getKey();
        return null;
    }

    private static String prefixOf(String attributeName) {
        int i = attributeName.indexOf(':');
        return i < 0 ? null : attributeName.substring(0, i);
    }

    private static void newLine(XMLStreamWriter w, int depth) throws XMLStreamException {
        StringBuilder sb = new StringBuilder("\n");
        for (int i = 0; i < depth; i++) sb.append("  ");
        w.writeCharacters(sb.toString());
    }
}
