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
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(SedaContextExtension.class)
class SEDAValidationTest implements UseTestFiles {

    @Test
    void testSedaXmlValidationOK() throws IllegalArgumentException, SEDALibException, InterruptedException {
        DiskToArchiveTransferImporter di = importSampleWithCompleteGlobalMetadata();

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

    private static DiskToArchiveTransferImporter importSampleWithCompleteGlobalMetadata()
        throws SEDALibException, InterruptedException {
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
        return di;
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

    /**
     * The validator had no error handler, so it threw on the first anomaly: a non conformant manifest
     * gave one anomaly at a time and had to be checked as many times as it had problems. Every
     * anomaly is now collected in one pass.
     */
    @Test
    void shouldReportEveryAnomalyAndNotOnlyTheFirst() throws Exception {
        DiskToArchiveTransferImporter di = importSampleWithCompleteGlobalMetadata();
        int emptied = 0;
        for (ArchiveUnit au : di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().values()) {
            au.setContentXmlData("");
            if (++emptied == 2) break;
        }

        Throwable thrown = catchThrowable(() -> di.getArchiveTransfer().sedaSchemaValidate(null));

        assertThat(thrown).isInstanceOf(SEDALibException.class);
        assertThat(thrown.getMessage()).contains("2 anomalie(s) détectée(s)");
    }

    /**
     * The same check replayed on the same package has to give the same anomalies, which is what the
     * "résultats non reproductibles du contrôle au profil RNG" report denies.
     */
    @Test
    void shouldGiveTheSameAnomaliesWhenTheProfileCheckIsReplayed() throws Exception {
        TestUtilities.eraseAll("target/tmpJunit/KO_468_replay.zip-tmpdir");
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/KO_468.zip",
            "target/tmpJunit/KO_468_replay.zip-tmpdir",
            null
        );
        si.doImport();

        Throwable first = catchThrowable(
            () -> si.getArchiveTransfer().sedaProfileValidate("src/test/resources/PacketSamples/profile.rng", null)
        );
        Throwable second = catchThrowable(
            () -> si.getArchiveTransfer().sedaProfileValidate("src/test/resources/PacketSamples/profile.rng", null)
        );

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.getMessage()).isEqualTo(first.getMessage());
    }

    /**
     * Same requirement for the SEDA schema check.
     */
    @Test
    void shouldGiveTheSameAnomaliesWhenTheSchemaCheckIsReplayed() throws Exception {
        DiskToArchiveTransferImporter di = importSampleWithCompleteGlobalMetadata();
        di.getArchiveTransfer().getDataObjectPackage().getArchiveUnitById("ID38").setContentXmlData("");

        Throwable first = catchThrowable(() -> di.getArchiveTransfer().sedaSchemaValidate(null));
        Throwable second = catchThrowable(() -> di.getArchiveTransfer().sedaSchemaValidate(null));

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.getMessage()).isEqualTo(first.getMessage());
    }

    /**
     * Bug #16844: the profile check reported anomalies, then the following checks didn't, on the same
     * SIP and the same profile.
     * <p>
     * Showing the manifest ("Voir le manifeste") or exporting with the metadata filter active sets the
     * export metadata list on the DataObjectPackage of the work and never resets it. The compliance
     * check serializes the same DataObjectPackage, so every following check validates the filtered
     * manifest: the anomalies carried by a filtered out metadata are no longer reported, while the
     * package itself didn't change.
     * <p>
     * This replays the sequence of the resip threads: CheckProfileThread, then SeeManifestThread with
     * the metadata filter, then CheckProfileThread again.
     */
    @Test
    void shouldGiveTheSameProfileAnomaliesAfterAFilteredManifestView() throws Exception {
        TestUtilities.eraseAll("target/tmpJunit/OK_468_filter.zip-tmpdir");
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/OK_468.zip",
            "target/tmpJunit/OK_468_filter.zip-tmpdir",
            null
        );
        si.doImport();
        DataObjectPackage dataObjectPackage = si.getArchiveTransfer().getDataObjectPackage();
        // a SEDA compliant metadata the profile doesn't allow in this ArchiveUnit
        ArchiveUnit archiveUnit = dataObjectPackage.getArchiveUnitById("ID03");
        archiveUnit.setContentXmlData(
            archiveUnit
                .getContentXmlData()
                .replace(
                    "</Content>",
                    "<CustodialHistory><CustodialHistoryItem>Historique</CustodialHistoryItem></CustodialHistory></Content>"
                )
        );

        // first check
        Throwable first = catchThrowable(() -> si.getArchiveTransfer().sedaProfileValidate(PROFILE, null));
        // manifest view with the metadata filter active, CustodialHistory not kept
        dataObjectPackage.setExportMetadataList(
            List.of("DescriptionLevel", "Title", "Description", "OriginatingAgency")
        );
        new ArchiveTransferToSIPExporter(si.getArchiveTransfer(), null).getSEDAXMLManifest(true, true);
        // second check on the same, unchanged, package
        Throwable second = catchThrowable(() -> si.getArchiveTransfer().sedaProfileValidate(PROFILE, null));

        assertThat(first).isNotNull().hasMessageContaining("element \"CustodialHistory\" not allowed here");
        assertThat(second).as("the second profile check on the same package has to report anomalies").isNotNull();
        // the manifest view normalizes the Content of the ArchiveUnits, dropping the XML comments of
        // the imported manifest, so the anomalies are compared and not their line in the manifest
        assertThat(anomalies(second.getMessage()))
            .as("the second profile check on the same package has to report the same anomalies")
            .isEqualTo(anomalies(first.getMessage()));
        assertThat(dataObjectPackage.getExportMetadataList())
            .as("the check must leave the export choice of the user untouched")
            .containsExactly("DescriptionLevel", "Title", "Description", "OriginatingAgency");
    }

    private static final String PROFILE = "src/test/resources/PacketSamples/profile.rng";

    // the summary line and the context and raw message of each anomaly, without its position
    private static List<String> anomalies(String validationMessage) {
        return validationMessage
            .lines()
            .filter(
                line ->
                    line.startsWith("Le flux XML") ||
                    line.startsWith("-> Contexte de l'erreur") ||
                    line.startsWith("erreur brute")
            )
            .toList();
    }
}
