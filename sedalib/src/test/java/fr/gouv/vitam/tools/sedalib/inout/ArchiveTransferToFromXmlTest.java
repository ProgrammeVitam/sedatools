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
package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SedaContextExtension.class)
class ArchiveTransferToFromXmlTest implements UseTestFiles {

    private static String readFileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @Test
    void testToFromSedaXml()
        throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {
        // do import of test directory
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SampleWithLinksModelV2",
            null
        );
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");

        di.doImport();

        di.getArchiveTransfer().setGlobalMetadata(new GlobalMetadata());
        di.getArchiveTransfer().getGlobalMetadata().comment = "2eme SIP";
        di.getArchiveTransfer().getGlobalMetadata().messageIdentifier = "MessageIdentifier0";
        di.getArchiveTransfer().getGlobalMetadata().archivalAgreement = "ArchivalAgreement0";
        di.getArchiveTransfer().getGlobalMetadata().codeListVersionsXmlData = "<CodeListVersions>\n" +
        "    <ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion>\n" +
        "<MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion0</MessageDigestAlgorithmCodeListVersion>\n" +
        "<MimeTypeCodeListVersion>MimeTypeCodeListVersion0</MimeTypeCodeListVersion>\n" +
        "<EncodingCodeListVersion>EncodingCodeListVersion0</EncodingCodeListVersion>\n" +
        "<FileFormatCodeListVersion>FileFormatCodeListVersion0</FileFormatCodeListVersion>\n" +
        "<CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion0</CompressionAlgorithmCodeListVersion>\n" +
        "<DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion0</DataObjectVersionCodeListVersion>\n" +
        "<StorageRuleCodeListVersion>StorageRuleCodeListVersion0</StorageRuleCodeListVersion>\n" +
        "<AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion0</AppraisalRuleCodeListVersion>\n" +
        "<AccessRuleCodeListVersion>AccessRuleCodeListVersion0</AccessRuleCodeListVersion>\n" +
        "<DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion0</DisseminationRuleCodeListVersion>\n" +
        "<ReuseRuleCodeListVersion>ReuseRuleCodeListVersion0</ReuseRuleCodeListVersion>\n" +
        "<ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion0</ClassificationRuleCodeListVersion>\n" +
        "<AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion0</AuthorizationReasonCodeListVersion>\n" +
        "<RelationshipCodeListVersion>RelationshipCodeListVersion0</RelationshipCodeListVersion>\n" +
        "  </CodeListVersions>";
        di
            .getArchiveTransfer()
            .getDataObjectPackage()
            .setManagementMetadataXmlData(
                "<ManagementMetadata>\n" +
                "      <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n" +
                "<LegalStatus>Public Archive</LegalStatus>\n" +
                "<OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n" +
                "<SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n" +
                "    </ManagementMetadata>"
            );
        di.getArchiveTransfer().getGlobalMetadata().archivalAgencyIdentifier = "Identifier4";
        di.getArchiveTransfer().getGlobalMetadata().transferringAgencyIdentifier = "Identifier5";

        // flat
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2);
        di.getArchiveTransfer().toSedaXml(xmlWriter, false, null);
        xmlWriter.close();
        String generatedFlatManifest = baos
            .toString(StandardCharsets.UTF_8)
            .replaceAll("<LastModified>.*</LastModified>\n", "");

        // hierarchical
        baos.reset();
        xmlWriter = new SEDAXMLStreamWriter(baos, 2);
        di.getArchiveTransfer().toSedaXml(xmlWriter, true, null);
        xmlWriter.close();
        String generatedHierarchicalManifest = baos
            .toString(StandardCharsets.UTF_8)
            .replaceAll("<LastModified>.*</LastModified>\n", "");

        String fileManifest = readFileToString("src/test/resources/PacketSamples/SampleWithLinkFlatManifest.xml");
        generatedFlatManifest = generatedFlatManifest.substring(generatedFlatManifest.indexOf("MessageIdentifier"));
        fileManifest = fileManifest.substring(fileManifest.indexOf("MessageIdentifier"));
        //WARNING: if Git is not set to respect LF this test will fail
        assertThat(generatedFlatManifest).isEqualToNormalizingNewlines(fileManifest);

        fileManifest = readFileToString("src/test/resources/PacketSamples/SampleWithLinkHierarchicalManifest.xml");
        generatedHierarchicalManifest = generatedHierarchicalManifest.substring(
            generatedHierarchicalManifest.indexOf("MessageIdentifier")
        );
        fileManifest = fileManifest.substring(fileManifest.indexOf("MessageIdentifier"));
        //WARNING: if Git is not set to respect LF this test will fail
        assertThat(generatedHierarchicalManifest).isEqualToNormalizingNewlines(fileManifest);
    }
}
