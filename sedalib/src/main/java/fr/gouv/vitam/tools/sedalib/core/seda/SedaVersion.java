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
package fr.gouv.vitam.tools.sedalib.core.seda;

public enum SedaVersion {
    V2_0(2, 0),
    V2_1(2, 1),
    V2_2(2, 2),
    V2_3(2, 3);

    private final int major;
    private final int minor;

    SedaVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }

    public String displayString() {
        return "SEDA " + major + "." + minor;
    }

    public static SedaVersion from(int major, int minor) {
        for (SedaVersion version : values()) {
            if (version.major == major && version.minor == minor) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unsupported SEDA version: " + major + "." + minor);
    }

    public static SedaVersion from(String version) {
        String[] fragments = version.split("[.]");
        int major = 2;
        int minor;

        if (fragments.length == 1) {
            minor = Integer.parseInt(fragments[0]);

            return from(major, minor);
        }

        if (fragments.length == 2) {
            major = Integer.parseInt(fragments[0]);
            minor = Integer.parseInt(fragments[1]);

            return from(major, minor);
        }

        throw new IllegalArgumentException("Unsupported SEDA version: " + version);
    }
}
