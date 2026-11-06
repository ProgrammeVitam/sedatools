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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The warnings about a digest edited by hand, which never refuse it.
 */
class DigestTypeEditorTest {

    @Test
    void shouldNotWarnAboutAConsistentDigest() {
        assertThat(DigestTypeEditor.getWarnings("a".repeat(64), "SHA-256")).isEmpty();
    }

    @Test
    void shouldWarnAboutAWrongDigestTypedByHand() {
        assertThat(DigestTypeEditor.getWarnings("not-a-digest", "SHA-256"))
            .singleElement()
            .asString()
            .contains("hexadécimale");
        assertThat(DigestTypeEditor.getWarnings("a".repeat(64), "SHA-512")).singleElement().asString().contains("128");
    }

    @Test
    void shouldWarnAboutAMissingOrNonVitamAlgorithm() {
        assertThat(DigestTypeEditor.getWarnings("abcd", " ")).singleElement().asString().contains("absent");
        assertThat(DigestTypeEditor.getWarnings("a".repeat(64), "SHA3-256"))
            .singleElement()
            .asString()
            .contains("Vitam");
    }

    @Test
    void shouldKeepAWrongDigestTypedByHand() throws Exception {
        // Given
        DigestType digest = new DigestType("MessageDigest", "a".repeat(64), "SHA-256");

        // When an erroneous value is set, as the editor does on extraction
        digest.setValue("not-a-digest");
        digest.setAlgorithm("UNKNOWN-ALGORITHM");

        // Then
        assertThat(digest.getValue()).isEqualTo("not-a-digest");
        assertThat(digest.getAlgorithm()).isEqualTo("UNKNOWN-ALGORITHM");
    }
}
