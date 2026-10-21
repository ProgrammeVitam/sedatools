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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The technical metadata read from the file of a BinaryDataObject, without the graphic interface.
 */
class FileMetadataTest {

    private static final Path ZIP = Paths.get(
        "src/test/resources/PacketSamples/SampleWithoutLinksModelV2/Root/Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip"
    );

    @Test
    void readsTheDigestSizeFormatAndFileInformation() throws Exception {
        List<SEDAMetadata> metadata = FileMetadata.of(ZIP, SedaVersion.V2_3, "Inconnu");

        assertThat(metadata.stream().map(SEDAMetadata::getXmlElementName).collect(Collectors.toList())).containsExactly(
            "MessageDigest",
            "Size",
            "FormatIdentification",
            "FileInfo"
        );
        assertThat(((DigestType) metadata.get(0)).getValue()).hasSize(128);
        assertThat(((IntegerType) metadata.get(1)).getValue()).isEqualTo(Files.size(ZIP));
        FormatIdentification format = (FormatIdentification) metadata.get(2);
        assertThat(format.getSimpleMetadata("MimeType")).isEqualTo("application/zip");
        assertThat(format.getSedaVersion()).isEqualTo(SedaVersion.V2_3);
        FileInfo fileInfo = (FileInfo) metadata.get(3);
        assertThat(fileInfo.getSimpleMetadata("Filename")).isEqualTo("OK-RULES-MDRULES.zip");
        assertThat(fileInfo.getSimpleMetadata("LastModified")).isNotBlank();
    }
}
