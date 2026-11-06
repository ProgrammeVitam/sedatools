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

/**
 * An element declaration, global (a possible document root) or local to a content model.
 */
public final class ElementDef {

    private final String name;
    private final String namespace;
    private TypeDef type;
    private boolean global;
    private boolean isAbstract;
    private boolean nillable;
    private String defaultValue;
    private String fixedValue;
    private String documentation;

    public ElementDef(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public TypeDef getType() {
        return type;
    }

    public void setType(TypeDef type) {
        this.type = type;
    }

    /**
     * @return the type as a complex type, or null if the element has a simple type
     */
    public ComplexTypeDef getComplexType() {
        return type instanceof ComplexTypeDef ? (ComplexTypeDef) type : null;
    }

    /**
     * @return the type as a simple type, or null if the element has a complex type
     */
    public SimpleTypeDef getSimpleType() {
        return type instanceof SimpleTypeDef ? (SimpleTypeDef) type : null;
    }

    /**
     * @return the simple type of the text content, whether the element type is simple or a complex
     * type with simple content, null if the element has no text content
     */
    public SimpleTypeDef getTextType() {
        if (type instanceof SimpleTypeDef) return (SimpleTypeDef) type;
        ComplexTypeDef ct = (ComplexTypeDef) type;
        return ct.getSimpleContentType();
    }

    public boolean hasTextContent() {
        return type instanceof SimpleTypeDef || ((ComplexTypeDef) type).hasTextContent();
    }

    public boolean hasElementContent() {
        return type instanceof ComplexTypeDef && ((ComplexTypeDef) type).hasElementContent();
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
