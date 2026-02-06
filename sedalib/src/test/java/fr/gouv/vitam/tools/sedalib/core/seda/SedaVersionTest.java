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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SedaVersionTest {

    @Test
    void shouldParseMinorOnlyVersions() {
        assertEquals(SedaVersion.V2_0, SedaVersion.from("0"));
        assertEquals(SedaVersion.V2_1, SedaVersion.from("1"));
        assertEquals(SedaVersion.V2_2, SedaVersion.from("2"));
        assertEquals(SedaVersion.V2_3, SedaVersion.from("3"));
    }

    @Test
    void shouldParseMajorMinorVersions() {
        assertEquals(SedaVersion.V2_0, SedaVersion.from("2.0"));
        assertEquals(SedaVersion.V2_1, SedaVersion.from("2.1"));
        assertEquals(SedaVersion.V2_2, SedaVersion.from("2.2"));
        assertEquals(SedaVersion.V2_3, SedaVersion.from("2.3"));
    }

    @Test
    void shouldThrowExceptionForUnsupportedMinorOnlyVersion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> SedaVersion.from("4"));

        assertEquals("Unsupported SEDA version: 2.4", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForUnsupportedMajorMinorVersion() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SedaVersion.from("3.0")
        );

        assertEquals("Unsupported SEDA version: 3.0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> SedaVersion.from("2.1.0"));
        assertThrows(NumberFormatException.class, () -> SedaVersion.from("a"));
        assertThrows(NumberFormatException.class, () -> SedaVersion.from("2.a"));
    }
}
