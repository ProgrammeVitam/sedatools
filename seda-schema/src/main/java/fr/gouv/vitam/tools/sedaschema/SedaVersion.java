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
 * The SEDA versions whose official XSD are bundled in this module.
 */
public enum SedaVersion {
    V2_1("2.1", "fr:gouv:culture:archivesdefrance:seda:v2.1", "sedaschema/xsd/seda2_1/seda-vitam-2.1-main.xsd"),
    V2_2("2.2", "fr:gouv:culture:archivesdefrance:seda:v2.2", "sedaschema/xsd/seda2_2/seda-vitam-2.2-main.xsd"),
    V2_3("2.3", "fr:gouv:culture:archivesdefrance:seda:v2.3", "sedaschema/xsd/seda2_3/seda-2.3-main.xsd");

    private final String label;
    private final String namespace;
    private final String mainXsdResource;

    SedaVersion(String label, String namespace, String mainXsdResource) {
        this.label = label;
        this.namespace = namespace;
        this.mainXsdResource = mainXsdResource;
    }

    public String getLabel() {
        return label;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * @return the classpath resource of the main XSD (the one including all the others)
     */
    public String getMainXsdResource() {
        return mainXsdResource;
    }

    /**
     * @param namespace a SEDA namespace
     * @return the version of this namespace, or null if unknown
     */
    public static SedaVersion fromNamespace(String namespace) {
        for (SedaVersion v : values()) if (v.namespace.equals(namespace)) return v;
        return null;
    }
}
