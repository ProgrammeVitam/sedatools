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
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SedaContextExtension.class)
class SEDAValidationTest implements UseTestFiles {

    @Test
    void testSedaXmlValidationOK() throws IllegalArgumentException, SEDALibException, InterruptedException {
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

        // validation
        assertAll(() -> di.getArchiveTransfer().sedaSchemaValidate(null));
    }

    @Test
    void testSedaXmlArchiveTransferGenerationAndValidationForSedaVersion2()
        throws IllegalArgumentException, SEDALibException, InterruptedException {
        // do import of test directory
        SedaContext.setVersion(SedaVersion.V2_2);
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
        "<HoldRuleCodeListVersion>HoldRuleCodeListVersion0</HoldRuleCodeListVersion>\n" +
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

        ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getArchiveUnitById("ID11");
        BinaryDataObject bdo = (BinaryDataObject) au.getTheDataObjectGroup().getBinaryDataObjectList().get(0);
        bdo.addMetadata(new StringType("DataObjectProfile", "Test"));

        // validation
        assertAll(() -> di.getArchiveTransfer().sedaSchemaValidate(null));
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)
        ) {
            di.getArchiveTransfer().toSedaXml(xmlWriter, true, null);
            String atout = baos.toString(StandardCharsets.UTF_8);
            assertThat(atout).contains("fr:gouv:culture:archivesdefrance:seda:v2.2");
            assertThat(atout).contains("DataObjectProfile");
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        SedaContext.setVersion(SedaVersion.V2_1);
    }

    @Test
    void testSedaXmlValidationKO() throws IllegalArgumentException, SEDALibException, InterruptedException {
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

        ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getArchiveUnitById("ID38");
        au.setContentXmlData("");

        // validation
        assertThatThrownBy(() -> di.getArchiveTransfer().sedaSchemaValidate(null)).hasMessageContaining(
            "The content of element 'ArchiveUnit' is not complete"
        );
    }

    @Test
    void testSedaRNGProfileValidationOK() throws IllegalArgumentException, SEDALibException, InterruptedException {
        // given
        TestUtilities.eraseAll("target/tmpJunit/OK_468.zip-tmpdir");
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/OK_468.zip",
            "target/tmpJunit/OK_468.zip-tmpdir",
            null
        );
        si.doImport();

        // when validate then no exception
        assertAll(
            () -> si.getArchiveTransfer().sedaProfileValidate("src/test/resources/PacketSamples/profile.rng", null)
        );
    }

    @Test
    void testSedaRNGProfileValidationKO() throws IllegalArgumentException, SEDALibException, InterruptedException {
        // given
        TestUtilities.eraseAll("target/tmpJunit/KO_468.zip-tmpdir");
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/KO_468.zip",
            "target/tmpJunit/KO_468.zip-tmpdir",
            null
        );
        si.doImport();

        // when validate then no exception
        assertThatThrownBy(
            () -> si.getArchiveTransfer().sedaProfileValidate("src/test/resources/PacketSamples/profile.rng", null)
        ).hasMessageContaining("\"Title\" invalid; must be equal to \"Versement de la matrice cadastrale numérique\"");
    }
    // TODO testWithXSD
}
