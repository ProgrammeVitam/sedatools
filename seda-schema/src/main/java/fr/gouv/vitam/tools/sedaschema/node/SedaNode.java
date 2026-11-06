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

import fr.gouv.vitam.tools.sedaschema.model.ElementDef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A generic SEDA element instance, driven by its schema declaration instead of a dedicated class.
 * <p>
 * A node carries its attributes, and either text (simple content) or child nodes (element
 * content). A node accepted by an xsd:any has no declaration ({@link #getDef()} is null) and its
 * subtree is kept as is.
 */
public final class SedaNode {

    private final String name;
    private final String namespace;
    private final ElementDef def;
    private SedaNode parent;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<SedaNode> children = new ArrayList<>();
    private String text;

    /**
     * Creates a node for a declared element.
     */
    public SedaNode(ElementDef def) {
        this.def = def;
        this.name = def.getName();
        this.namespace = def.getNamespace();
    }

    /**
     * Creates a node for an undeclared element (accepted by a wildcard).
     */
    public SedaNode(String name, String namespace) {
        this.def = null;
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the declaration, null for an undeclared (wildcard) node
     */
    public ElementDef getDef() {
        return def;
    }

    public boolean isDeclared() {
        return def != null;
    }

    public SedaNode getParent() {
        return parent;
    }

    /**
     * @return the attributes by name (local name, or "prefix:name" for a namespaced attribute like xml:id)
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public SedaNode setAttribute(String name, String value) {
        if (value == null) attributes.remove(name);
        else attributes.put(name, value);
        return this;
    }

    public String getText() {
        return text;
    }

    public SedaNode setText(String text) {
        this.text = text;
        return this;
    }

    public List<SedaNode> getChildren() {
        return children;
    }

    public List<SedaNode> getChildren(String name) {
        return children.stream().filter(c -> c.name.equals(name)).collect(Collectors.toList());
    }

    /**
     * @return the first child of this name, or null
     */
    public SedaNode getChild(String name) {
        for (SedaNode c : children) if (c.name.equals(name)) return c;
        return null;
    }

    public int countChildren(String name) {
        int n = 0;
        for (SedaNode c : children) if (c.name.equals(name)) n++;
        return n;
    }

    /**
     * Appends a child at the end. Use {@link StructureHelper#add(SedaNode, ElementDef)} to insert
     * it at the position the schema requires.
     */
    public SedaNode addChild(SedaNode child) {
        return addChild(children.size(), child);
    }

    public SedaNode addChild(int index, SedaNode child) {
        if (child.parent != null) child.parent.children.remove(child);
        child.parent = this;
        children.add(index, child);
        return child;
    }

    public boolean removeChild(SedaNode child) {
        if (children.remove(child)) {
            child.parent = null;
            return true;
        }
        return false;
    }

    public void remove() {
        if (parent != null) parent.removeChild(this);
    }

    /**
     * @return the path of this node from its root, like "ArchiveTransfer/DataObjectPackage/DescriptiveMetadata/ArchiveUnit"
     */
    public String getPath() {
        return parent == null ? name : parent.getPath() + "/" + name;
    }

    /**
     * @return the first descendant at this relative path, or null
     */
    public SedaNode find(String path) {
        SedaNode current = this;
        for (String step : path.split("/")) {
            current = current.getChild(step);
            if (current == null) return null;
        }
        return current;
    }

    /**
     * @return a deep copy, detached from any parent
     */
    public SedaNode copy() {
        SedaNode copy = def != null ? new SedaNode(def) : new SedaNode(name, namespace);
        copy.attributes.putAll(attributes);
        copy.text = text;
        for (SedaNode c : children) copy.addChild(c.copy());
        return copy;
    }

    @Override
    public String toString() {
        return (
            "<" + name + (text != null ? ">" + text : children.isEmpty() ? "/>" : ">" + children.size() + " children")
        );
    }
}
