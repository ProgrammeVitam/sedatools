package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A manifest is read in the SEDA version of its namespace.
 */
class ManifestSedaVersionTest {

    private static final String SAMPLE = "src/test/resources/PacketSamples/SampleWithoutLinksModelV1";

    private static String manifestIn(SedaVersion sedaVersion) throws Exception {
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(sedaVersion, SAMPLE, null);
        di.doImport();
        GlobalMetadata globalMetadata = new GlobalMetadata();
        globalMetadata.messageIdentifier = "MessageIdentifier0";
        globalMetadata.archivalAgreement = "ArchivalAgreement0";
        globalMetadata.codeListVersionsXmlData =
            "<CodeListVersions><ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion></CodeListVersions>";
        globalMetadata.archivalAgencyIdentifier = "Identifier4";
        globalMetadata.transferringAgencyIdentifier = "Identifier5";
        di.getArchiveTransfer().setGlobalMetadata(globalMetadata);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            di.getArchiveTransfer().toSedaXml(xmlWriter, true, null);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static ArchiveTransfer read(String manifest) throws Exception {
        try (
            SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(
                new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))
            )
        ) {
            return ArchiveTransfer.fromSedaXml(xmlReader, SAMPLE, null);
        }
    }

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void readsTheManifestInTheVersionOfItsNamespace(SedaVersion sedaVersion) throws Exception {
        ArchiveTransfer archiveTransfer = read(manifestIn(sedaVersion));

        assertThat(archiveTransfer.getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(archiveTransfer.getDataObjectPackage().getSedaVersion()).isEqualTo(sedaVersion);
    }

    @Test
    void refusesAManifestInAnUnknownVersion() throws Exception {
        String manifest = manifestIn(SedaVersion.V2_1).replace(
            "fr:gouv:culture:archivesdefrance:seda:v2.1",
            "fr:gouv:culture:archivesdefrance:seda:v9.9"
        );

        assertThatThrownBy(() -> read(manifest))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("fr:gouv:culture:archivesdefrance:seda:v9.9");
    }
}
