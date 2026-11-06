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

/**
 * The SEDA versions whose XSD are bundled in this module, in both flavours (see
 * {@link SedaFlavor}).
 */
public enum SedaVersion {
    V2_1(
        "2.1",
        "fr:gouv:culture:archivesdefrance:seda:v2.1",
        "sedaschema/xsd/official/seda2_1/seda-2.1-main.xsd",
        "sedaschema/xsd/vitam/seda2_1/seda-vitam-2.1-main.xsd"
    ),
    V2_2(
        "2.2",
        "fr:gouv:culture:archivesdefrance:seda:v2.2",
        "sedaschema/xsd/official/seda2_2/seda-2.2-main.xsd",
        "sedaschema/xsd/vitam/seda2_2/seda-vitam-2.2-main.xsd"
    ),
    // Vitam doesn't alter the 2.3: the single bundled XSD is the official one
    V2_3(
        "2.3",
        "fr:gouv:culture:archivesdefrance:seda:v2.3",
        "sedaschema/xsd/official/seda2_3/seda-2.3-main.xsd",
        null
    );

    private final String label;
    private final String namespace;
    private final String officialMainXsdResource;
    private final String vitamMainXsdResource;

    SedaVersion(String label, String namespace, String officialMainXsdResource, String vitamMainXsdResource) {
        this.label = label;
        this.namespace = namespace;
        this.officialMainXsdResource = officialMainXsdResource;
        this.vitamMainXsdResource = vitamMainXsdResource;
    }

    public String getLabel() {
        return label;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * @return true if Vitam diverges from the standard in this version
     */
    public boolean hasVitamVariant() {
        return vitamMainXsdResource != null;
    }

    /**
     * @return the classpath resource of the official main XSD (the one including all the others)
     */
    public String getMainXsdResource() {
        return officialMainXsdResource;
    }

    /**
     * @param flavor the wanted flavour
     * @return the classpath resource of its main XSD, the official one when Vitam doesn't diverge
     * in this version
     */
    public String getMainXsdResource(SedaFlavor flavor) {
        return flavor == SedaFlavor.VITAM && vitamMainXsdResource != null
            ? vitamMainXsdResource
            : officialMainXsdResource;
    }

    /**
     * @param namespace a SEDA namespace
     * @return the version of this namespace, or null if unknown
     */
    public static SedaVersion fromNamespace(String namespace) {
        for (SedaVersion v : values()) if (v.namespace.equals(namespace)) return v;
        return null;
    }

    /**
     * @param label a version label, such as "2.3"
     * @return the version of this label, or null if no XSD is bundled for it
     */
    public static SedaVersion fromLabel(String label) {
        for (SedaVersion v : values()) if (v.label.equals(label)) return v;
        return null;
    }
}
