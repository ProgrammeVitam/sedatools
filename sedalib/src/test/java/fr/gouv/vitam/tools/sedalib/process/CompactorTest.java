package fr.gouv.vitam.tools.sedalib.process;

import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CompactorTest implements UseTestFiles {

    private void eraseAll(String dirOrFile) {
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {
        }
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {
        }
    }

    @Test
    void TestCompactor() throws Exception {

        // Given this test directory imported
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV1", null);
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // When Compact the root ArchiveUnit
        ArchiveUnit rootAu = di.getArchiveTransfer().getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList().get(0);
        eraseAll("target/tmpJunit/CompactorTest");
        Compactor compactor = new Compactor(rootAu, "target/tmpJunit/CompactorTest", null);
        compactor.setObjectVersionFilters(List.of("BinaryMaster"), List.of("BinaryMaster", "TextContent"));
        compactor.setCompactedDocumentPackLimit(4096, 4);
        compactor.setDeflatedFlag(true);
        ArchiveUnit compactedAU = compactor.doCompact();

        // Then assert the root DocumentContainer AU content
        String rootContent = "<Content>\n" +
                "  <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "  <Title>Root</Title>\n" +
                "  <DocumentContainer>\n" +
                "    <DocumentsCount>9</DocumentsCount>\n" +
                "    <FileObjectsCount>12</FileObjectsCount>\n" +
                "    <RecordGrp>\n" +
                "      <RecordGrpID>Node1</RecordGrpID>\n" +
                "      <Content>\n" +
                "        <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "        <Title>Root</Title>\n" +
                "      </Content>\n" +
                "      <RecordGrp>\n" +
                "        <RecordGrpID>Node2</RecordGrpID>\n" +
                "        <Content>\n" +
                "          <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "          <Title>Node 2</Title>\n" +
                "        </Content>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node3</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.2 - Title cases</Title>\n" +
                "          </Content>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node4</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.1-^@]}êë^$£ê%ùµ_§._,;_!œŒÊ</Title>\n" +
                "            </Content>\n" +
                "            <RecordGrp>\n" +
                "              <RecordGrpID>Node5</RecordGrpID>\n" +
                "              <Content>\n" +
                "                <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "                <Title>^@]}êë^$£ê%ùµ_§._,;_!œŒÊ</Title>\n" +
                "              </Content>\n" +
                "            </RecordGrp>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node6</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.2-1234567890-a</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node7</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.3-ENMAJUSCULE</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node8</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.4-Un                                             espace</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node9</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.3 - Many</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node10</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.4</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node11</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.5</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "      </RecordGrp>\n" +
                "    </RecordGrp>\n" +
                "  </DocumentContainer>\n" +
                "</Content>";
        assertThat(compactedAU.getContentXmlData()).isEqualTo(rootContent);

        // And assert there are 5 DocumentPack AU content
        assertThat(compactedAU.getChildrenAuList().getCount()).isEqualTo(5);

        // And assert the first DocumentPack AU content
        ArchiveUnit packAU = compactedAU.getChildrenAuList().getArchiveUnitList().get(0);
        String packContent = "<Content>\n" +
                "  <DescriptionLevel>Item</DescriptionLevel>\n" +
                "  <Title>DocumentPack1</Title>\n" +
                "  <DocumentPack>\n" +
                "    <DocumentsCount>1</DocumentsCount>\n" +
                "    <FileObjectsCount>4</FileObjectsCount>\n" +
                "    <RecordGrp>\n" +
                "      <RecordGrpID>Node1</RecordGrpID>\n" +
                "      <Content>\n" +
                "        <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "        <Title>Root</Title>\n" +
                "      </Content>\n" +
                "      <RecordGrp>\n" +
                "        <RecordGrpID>Node2</RecordGrpID>\n" +
                "        <Content>\n" +
                "          <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "          <Title>Node 2</Title>\n" +
                "        </Content>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node3</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.2 - Title cases</Title>\n" +
                "          </Content>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node4</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.1-^@]}êë^$£ê%ùµ_§._,;_!œŒÊ</Title>\n" +
                "            </Content>\n" +
                "            <RecordGrp>\n" +
                "              <RecordGrpID>Node5</RecordGrpID>\n" +
                "              <Content>\n" +
                "                <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "                <Title>^@]}êë^$£ê%ùµ_§._,;_!œŒÊ</Title>\n" +
                "              </Content>\n" +
                "            </RecordGrp>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node6</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.2-1234567890-a</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node7</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.3-ENMAJUSCULE</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "          <RecordGrp>\n" +
                "            <RecordGrpID>Node8</RecordGrpID>\n" +
                "            <Content>\n" +
                "              <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "              <Title>Node 2.2.4-Un                                             espace</Title>\n" +
                "            </Content>\n" +
                "          </RecordGrp>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node9</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.3 - Many</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node10</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.4</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "        <RecordGrp>\n" +
                "          <RecordGrpID>Node11</RecordGrpID>\n" +
                "          <Content>\n" +
                "            <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "            <Title>Node 2.5</Title>\n" +
                "          </Content>\n" +
                "        </RecordGrp>\n" +
                "      </RecordGrp>\n" +
                "    </RecordGrp>\n" +
                "    <Document>\n" +
                "      <RecordGrpID>Node1</RecordGrpID>\n" +
                "      <Content>\n" +
                "        <DescriptionLevel>Item</DescriptionLevel>\n" +
                "        <Title>CSIC Tech : points remarquables PMO</Title>\n" +
                "        <OriginatingSystemId>&lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></OriginatingSystemId>\n" +
                "        <Writer>\n" +
                "          <FirstName>PLANCHOT Benjamin</FirstName>\n" +
                "          <BirthName>PLANCHOT Benjamin</BirthName>\n" +
                "          <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\n" +
                "        </Writer>\n" +
                "        <Addressee>\n" +
                "          <FirstName>frederic.deguilhen@culture.gouv.fr</FirstName>\n" +
                "          <BirthName>frederic.deguilhen@culture.gouv.fr</BirthName>\n" +
                "          <Identifier>frederic.deguilhen@culture.gouv.fr</Identifier>\n" +
                "        </Addressee>\n" +
                "        <Addressee>\n" +
                "          <FirstName>jean-severin.lair@culture.gouv.fr</FirstName>\n" +
                "          <BirthName>jean-severin.lair@culture.gouv.fr</BirthName>\n" +
                "          <Identifier>jean-severin.lair@culture.gouv.fr</Identifier>\n" +
                "        </Addressee>\n" +
                "        <Recipient>\n" +
                "          <FirstName>PLANCHOT Benjamin</FirstName>\n" +
                "          <BirthName>PLANCHOT Benjamin</BirthName>\n" +
                "          <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\n" +
                "        </Recipient>\n" +
                "        <SentDate>2016-08-30T10:14:17</SentDate>\n" +
                "        <ReceivedDate>2016-08-30T10:14:18</ReceivedDate>\n" +
                "        <TextContent>Bonjour,\n" +
                "\n" +
                "Vous trouverez ci-joint les éléments collectés au mois de juillet sous forme de tableur correspondant à l'avancement de vos activités. Afin de publier une mise à jour en CSIC Tech, merci de mettre à jour les éléments pour le jeudi 08 septembre au plus tard. Sans retour de votre part, je tiendrai compte de la dernière mise à jour.\n" +
                "\n" +
                "Pour rappel :\n" +
                "- L'objectif est de remonter l'état des activités (statut, livrable/jalon, points importants).\n" +
                "- Les colonnes de N à V sont à mettre à jour si nécessaire (fond orange clair).\n" +
                "\n" +
                "Merci par avance.\n" +
                "\n" +
                "Bien cordialement,\n" +
                "\n" +
                "\n" +
                "[http://www.modernisation.gouv.fr/sites/default/files/bloc-sgmap-2.jpg]&lt; http://www.modernisation.gouv.fr/>\n" +
                "\n" +
                "Benjamin PLANCHOT | PMO\n" +
                "Service « performance des services numériques »\n" +
                "Direction interministérielle du numérique et du système d'information et de communication de l'Etat\n" +
                "01 40 15 71 50 | Tour Mirabeau - 39-43 Quai André Citroën, 75015 Paris - Bureau 4027\n" +
                "modernisation.gouv.fr&lt; http://www.modernisation.gouv.fr/></TextContent>\n" +
                "      </Content>\n" +
                "      <FileObject>\n" +
                "        <DataObjectVersion>BinaryMaster_1</DataObjectVersion>\n" +
                "        <URI>Node1-Document0-BinaryMaster_1.eml</URI>\n" +
                "        <MessageDigest algorithm=\"SHA-512\">aa5ffc20efc3ae8f8e25ab95e08b3f5daa9004eeaed33006f4849faddec7d82675284b5de4125e03f9aad336e5c79b394b3a39183dcd456496fa0fd354896a6b</MessageDigest>\n" +
                "        <Size>108810</Size>\n" +
                "        <FormatIdentification>\n" +
                "          <FormatLitteral>MIME Email</FormatLitteral>\n" +
                "          <MimeType>message/rfc822</MimeType>\n" +
                "          <FormatId>fmt/950</FormatId>\n" +
                "        </FormatIdentification>\n" +
                "        <FileInfo>\n" +
                "          <Filename>-79980C36BA239C449A957.eml</Filename>\n" +
                "          <LastModified>###TIMESTAMP###</LastModified>\n" +
                "        </FileInfo>\n" +
                "      </FileObject>\n" +
                "      <SubDocument>\n" +
                "        <Content>\n" +
                "          <DescriptionLevel>Item</DescriptionLevel>\n" +
                "          <Title>201609_TdB_suivi_des_activites_VITAM.ods</Title>\n" +
                "          <Description>Document \"201609_TdB_suivi_des_activites_VITAM.ods\" joint au message &lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></Description>\n" +
                "          <CreatedDate>2016-08-30T10:13:03</CreatedDate>\n" +
                "        </Content>\n" +
                "        <FileObject>\n" +
                "          <DataObjectVersion>BinaryMaster_1</DataObjectVersion>\n" +
                "          <URI>Node1-Document0" + File.separator + "SubDocument1-BinaryMaster_1.ods</URI>\n" +
                "          <MessageDigest algorithm=\"SHA-512\">ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e</MessageDigest>\n" +
                "          <Size>50651</Size>\n" +
                "          <FormatIdentification>\n" +
                "            <FormatLitteral>OpenDocument Spreadsheet</FormatLitteral>\n" +
                "            <MimeType>application/vnd.oasis.opendocument.spreadsheet</MimeType>\n" +
                "            <FormatId>fmt/294</FormatId>\n" +
                "          </FormatIdentification>\n" +
                "          <FileInfo>\n" +
                "            <Filename>201609-TdB-suivi-des-a.ods</Filename>\n" +
                "            <LastModified>###TIMESTAMP###</LastModified>\n" +
                "          </FileInfo>\n" +
                "        </FileObject>\n" +
                "        <FileObject>\n" +
                "          <DataObjectVersion>TextContent_1</DataObjectVersion>\n" +
                "          <URI>Node1-Document0" + File.separator + "SubDocument1-TextContent_1.txt</URI>\n" +
                "          <MessageDigest algorithm=\"SHA-512\">7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2</MessageDigest>\n" +
                "          <Size>3307</Size>\n" +
                "          <FormatIdentification>\n" +
                "            <FormatLitteral>Plain Text File</FormatLitteral>\n" +
                "            <MimeType>text/plain</MimeType>\n" +
                "            <FormatId>x-fmt/111</FormatId>\n" +
                "          </FormatIdentification>\n" +
                "          <FileInfo>\n" +
                "            <Filename>201609-TdB-suivi-des-a.txt</Filename>\n" +
                "            <LastModified>###TIMESTAMP###</LastModified>\n" +
                "          </FileInfo>\n" +
                "        </FileObject>\n" +
                "      </SubDocument>\n" +
                "      <SubDocument>\n" +
                "        <Content>\n" +
                "          <DescriptionLevel>Item</DescriptionLevel>\n" +
                "          <Title>image001.jpg</Title>\n" +
                "          <Description>Document \"image001.jpg\" joint au message &lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></Description>\n" +
                "          <CreatedDate>2016-08-30T10:14:17</CreatedDate>\n" +
                "        </Content>\n" +
                "        <FileObject>\n" +
                "          <DataObjectVersion>BinaryMaster_1</DataObjectVersion>\n" +
                "          <URI>Node1-Document0" + File.separator + "SubDocument2-BinaryMaster_1.jpg</URI>\n" +
                "          <MessageDigest algorithm=\"SHA-512\">e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3</MessageDigest>\n" +
                "          <Size>21232</Size>\n" +
                "          <FormatIdentification>\n" +
                "            <FormatLitteral>Exchangeable Image File Format (Compressed)</FormatLitteral>\n" +
                "            <MimeType>image/jpeg</MimeType>\n" +
                "            <FormatId>fmt/645</FormatId>\n" +
                "          </FormatIdentification>\n" +
                "          <FileInfo>\n" +
                "            <Filename>image001.jpg</Filename>\n" +
                "            <LastModified>###TIMESTAMP###</LastModified>\n" +
                "          </FileInfo>\n" +
                "        </FileObject>\n" +
                "      </SubDocument>\n" +
                "    </Document>\n" +
                "  </DocumentPack>\n" +
                "</Content>";
        assertThat(packAU.getContentXmlData().replaceAll("<LastModified>.+<\\/LastModified>",
            "<LastModified>###TIMESTAMP###<\\/LastModified>")).isEqualTo(packContent);

        // And assert created files
        File doc = new File("target/tmpJunit/CompactorTest/Document1.zip");
        assertThat(doc).exists();
        assertThat(doc.length()).isGreaterThan(124 * 1024);
        assertThat(doc.length()).isLessThan(126 * 1024);
        doc = new File("target/tmpJunit/CompactorTest/Document3.zip");
        assertThat(doc).exists();
        assertThat(doc.length()).isGreaterThan(2 * 1024);
        assertThat(doc.length()).isLessThan(3 * 1024);
    }
}
