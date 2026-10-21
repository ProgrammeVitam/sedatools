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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple type: the primitive base, and the facets restricting it (enumeration, patterns,
 * lengths, bounds...). List and union varieties are kept as such.
 */
public final class SimpleTypeDef extends TypeDef {

    public enum Variety {
        ATOMIC,
        LIST,
        UNION,
    }

    private final Variety variety;
    /**
     * The XSD built-in primitive this type derives from ("string", "date", "boolean"...).
     */
    private final String primitive;
    /**
     * The name of the built-in type closest to this type ("token", "NCName", "dateTime"...).
     */
    private final String builtInBase;
    private List<String> enumeration = Collections.emptyList();
    private List<String> patterns = Collections.emptyList();
    private Map<String, String> facets = new LinkedHashMap<>();
    private SimpleTypeDef itemType;
    private List<SimpleTypeDef> memberTypes = Collections.emptyList();

    public SimpleTypeDef(String name, String namespace, Variety variety, String primitive, String builtInBase) {
        super(name, namespace);
        this.variety = variety;
        this.primitive = primitive;
        this.builtInBase = builtInBase;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    public Variety getVariety() {
        return variety;
    }

    public String getPrimitive() {
        return primitive;
    }

    public String getBuiltInBase() {
        return builtInBase;
    }

    /**
     * @return the allowed values, empty if the type is not an enumeration
     */
    public List<String> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public boolean isEnumeration() {
        return !enumeration.isEmpty();
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    /**
     * @return the other facets by XSD facet name (length, minLength, maxLength, minInclusive...)
     */
    public Map<String, String> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, String> facets) {
        this.facets = facets;
    }

    /**
     * @return the item type for a LIST variety, null otherwise
     */
    public SimpleTypeDef getItemType() {
        return itemType;
    }

    public void setItemType(SimpleTypeDef itemType) {
        this.itemType = itemType;
    }

    /**
     * @return the member types for a UNION variety, empty otherwise
     */
    public List<SimpleTypeDef> getMemberTypes() {
        return memberTypes;
    }

    public void setMemberTypes(List<SimpleTypeDef> memberTypes) {
        this.memberTypes = memberTypes;
    }
}
