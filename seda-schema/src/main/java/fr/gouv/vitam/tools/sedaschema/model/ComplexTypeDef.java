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
package fr.gouv.vitam.tools.sedaschema.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A complex type: its attributes, and its content which is either empty, simple (text with
 * attributes, like TextType), element-only or mixed.
 */
public final class ComplexTypeDef extends TypeDef {

    public enum ContentKind {
        EMPTY,
        SIMPLE,
        ELEMENT,
        MIXED,
    }

    private ContentKind contentKind = ContentKind.EMPTY;
    private List<AttributeDef> attributes = new ArrayList<>();
    private boolean attributeWildcard;
    /**
     * The text type for SIMPLE content, null otherwise.
     */
    private SimpleTypeDef simpleContentType;
    /**
     * The root particle for ELEMENT or MIXED content, null otherwise.
     */
    private Particle content;
    private boolean isAbstract;
    private String baseTypeName;
    private Map<String, ElementDef> childElementsByName;

    public ComplexTypeDef(String name, String namespace) {
        super(name, namespace);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public ContentKind getContentKind() {
        return contentKind;
    }

    public void setContentKind(ContentKind contentKind) {
        this.contentKind = contentKind;
    }

    public List<AttributeDef> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDef> attributes) {
        this.attributes = attributes;
    }

    public AttributeDef getAttribute(String name) {
        for (AttributeDef a : attributes) if (a.getName().equals(name)) return a;
        return null;
    }

    public boolean hasAttributeWildcard() {
        return attributeWildcard;
    }

    public void setAttributeWildcard(boolean attributeWildcard) {
        this.attributeWildcard = attributeWildcard;
    }

    public SimpleTypeDef getSimpleContentType() {
        return simpleContentType;
    }

    public void setSimpleContentType(SimpleTypeDef simpleContentType) {
        this.simpleContentType = simpleContentType;
    }

    public Particle getContent() {
        return content;
    }

    public void setContent(Particle content) {
        this.content = content;
        this.childElementsByName = null;
    }

    public boolean hasElementContent() {
        return content != null && (contentKind == ContentKind.ELEMENT || contentKind == ContentKind.MIXED);
    }

    public boolean hasTextContent() {
        return contentKind == ContentKind.SIMPLE || contentKind == ContentKind.MIXED;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    /**
     * @return the name of the named type this one is derived from, null if derived from anyType
     */
    public String getBaseTypeName() {
        return baseTypeName;
    }

    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }

    /**
     * @return all the element declarations reachable in the content model, in document order,
     * whatever the group nesting (wildcards excluded)
     */
    public List<ElementDef> getChildElements() {
        return new ArrayList<>(childElementsByName().values());
    }

    /**
     * @param name the child element local name
     * @return the declaration of this child in the content model, or null if not declared (it may
     * still be accepted by a wildcard, see {@link #acceptsWildcard()})
     */
    public ElementDef getChildElement(String name) {
        return childElementsByName().get(name);
    }

    /**
     * @return true if the content model contains an xsd:any
     */
    public boolean acceptsWildcard() {
        return content != null && hasWildcard(content);
    }

    private static boolean hasWildcard(Particle p) {
        if (p.isWildcard()) return true;
        if (p.isGroup()) for (Particle c : p.getGroup().getParticles()) if (hasWildcard(c)) return true;
        return false;
    }

    private Map<String, ElementDef> childElementsByName() {
        if (childElementsByName == null) {
            Map<String, ElementDef> map = new LinkedHashMap<>();
            if (content != null) collect(content, map);
            childElementsByName = Collections.unmodifiableMap(map);
        }
        return childElementsByName;
    }

    private static void collect(Particle p, Map<String, ElementDef> map) {
        if (p.isElement()) map.putIfAbsent(p.getElement().getName(), p.getElement());
        else if (p.isGroup()) for (Particle c : p.getGroup().getParticles()) collect(c, map);
    }
}
