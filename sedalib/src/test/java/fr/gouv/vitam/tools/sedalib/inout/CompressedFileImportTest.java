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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SedaContextExtension.class)
class CompressedFileImportTest implements UseTestFiles {

    @Test
    void TestZipImport() throws Exception {

        // do import of test directory
        CompressedFileToArchiveTransferImporter zi = new CompressedFileToArchiveTransferImporter(
                "src/test/resources/zip/TestImport.zip", "target/tmpJunit/TestImport.zip-tmpdir", "UTF8", null, null);
        zi.addIgnorePattern("Thumbs.db");
        zi.addIgnorePattern("pagefile.sys");
        zi.doImport();

        // assert macro results
        assertEquals(22, zi.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(11, zi.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert one dataObjectGroup using serialization
        String testog = "{\n" +
                "  \"binaryDataObjectList\" : [ {\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"BinaryMaster_1\"\n" +
                "    }, {\n" +
                "      \"type\" : \"DigestType\",\n" +
                "      \"elementName\" : \"MessageDigest\",\n" +
                "      \"value\" : \"ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e\",\n" +
                "      \"algorithm\" : \"SHA-512\"\n" +
                "    }, {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 50651\n" +
                "    }, {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"elementName\" : \"FormatIdentification\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatLitteral\",\n" +
                "        \"value\" : \"OpenDocument Spreadsheet\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"MimeType\",\n" +
                "        \"value\" : \"application/vnd.oasis.opendocument.spreadsheet\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatId\",\n" +
                "        \"value\" : \"fmt/294\"\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"201609-TdB-suivi-des-a.ods\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2025-04-27T20:15:14.696302Z\"\n" +
                "      } ]\n" +
                "    } ],\n" +
                "    \"inDataObjectPackageId\" : \"ID17\",\n" +
                "    \"onDiskPath\" : \"C:\\\\Users\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestImport.zip-tmpdir\\\\Root\\\\Node 1\\\\Node 1.1\\\\__BinaryMaster_1__201609-TdB-suivi-des-a.ods\"\n" +
                "  }, {\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"TextContent_1\"\n" +
                "    }, {\n" +
                "      \"type\" : \"DigestType\",\n" +
                "      \"elementName\" : \"MessageDigest\",\n" +
                "      \"value\" : \"7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2\",\n" +
                "      \"algorithm\" : \"SHA-512\"\n" +
                "    }, {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 3307\n" +
                "    }, {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"elementName\" : \"FormatIdentification\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatLitteral\",\n" +
                "        \"value\" : \"Plain Text File\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"MimeType\",\n" +
                "        \"value\" : \"text/plain\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatId\",\n" +
                "        \"value\" : \"x-fmt/111\"\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"201609-TdB-suivi-des-a.txt\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2025-04-27T20:15:14.696302Z\"\n" +
                "      } ]\n" +
                "    } ],\n" +
                "    \"inDataObjectPackageId\" : \"ID19\",\n" +
                "    \"onDiskPath\" : \"C:\\\\Users\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestImport.zip-tmpdir\\\\Root\\\\Node 1\\\\Node 1.1\\\\__TextContent_1__201609-TdB-suivi-des-a.txt\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ {\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"PhysicalMaster_1\"\n" +
                "    }, {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"PhysicalId\",\n" +
                "      \"value\" : \"940 W\"\n" +
                "    }, {\n" +
                "      \"type\" : \"PhysicalDimensions\",\n" +
                "      \"elementName\" : \"PhysicalDimensions\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Width\",\n" +
                "        \"value\" : 10.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Height\",\n" +
                "        \"value\" : 8.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Depth\",\n" +
                "        \"value\" : 1.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Diameter\",\n" +
                "        \"value\" : 0.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"Weight\",\n" +
                "        \"elementName\" : \"Weight\",\n" +
                "        \"value\" : 59.0,\n" +
                "        \"unit\" : \"gram\"\n" +
                "      } ]\n" +
                "    } ],\n" +
                "    \"inDataObjectPackageId\" : \"ID18\",\n" +
                "    \"onDiskPath\" : null\n" +
                "  } ],\n" +
                "  \"logBook\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID16\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";
        DataObjectGroup og = zi.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID16");
		//System.out.println("Value to verify="+mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og);
        sog = sog.replaceAll("\"dateTimeString\" : .*", "").trim();
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.1");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = sog.replaceAll("\"onDiskPath\" : .*\"", "");

        testog = testog.replaceAll("\"dateTimeString\" : .*", "").trim();
        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = testog.replaceAll("\"onDiskPath\" : .*\"", "");

        assertTrue(sogpath & testogpath);
        assertThat(sog).isEqualToNormalizingNewlines(testog);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "  \"archiveUnitProfileXmlData\" : null,\n" +
                "  \"managementXmlData\" : \"<Management>\\n  <AccessRule>\\n    <Rule>ACC-00002</Rule>\\n    <StartDate>2015-11-19</StartDate>\\n  </AccessRule>\\n</Management>\",\n" +
                "  \"contentXmlData\" : \"<Content>\\n    <DescriptionLevel>Item</DescriptionLevel>\\n    <Title>CSIC Tech : points remarquables PMO</Title>\\n    <OriginatingSystemId>&lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></OriginatingSystemId>\\n    <Writer>\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\n    </Writer>\\n    <Addressee>\\n        <FirstName>frederic.deguilhen@culture.gouv.fr</FirstName>\\n        <BirthName>frederic.deguilhen@culture.gouv.fr</BirthName>\\n        <Identifier>frederic.deguilhen@culture.gouv.fr</Identifier>\\n    </Addressee>\\n    <Addressee>\\n        <FirstName>jean-severin.lair@culture.gouv.fr</FirstName>\\n        <BirthName>jean-severin.lair@culture.gouv.fr</BirthName>\\n        <Identifier>jean-severin.lair@culture.gouv.fr</Identifier>\\n    </Addressee>\\n    <Recipient>\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\n    </Recipient>\\n    <SentDate>2016-08-30T10:14:17Z</SentDate>\\n    <ReceivedDate>2016-08-30T10:14:18Z</ReceivedDate>\\n    <TextContent>Bonjour,\\n\\nVous trouverez ci-joint les éléments collectés au mois de juillet sous forme de tableur correspondant à l'avancement de vos activités. Afin de publier une mise à jour en CSIC Tech, merci de mettre à jour les éléments pour le jeudi 08 septembre au plus tard. Sans retour de votre part, je tiendrai compte de la dernière mise à jour.\\n\\nPour rappel :\\n- L'objectif est de remonter l'état des activités (statut, livrable/jalon, points importants).\\n- Les colonnes de N à V sont à mettre à jour si nécessaire (fond orange clair).\\n\\nMerci par avance.\\n\\nBien cordialement,\\n\\n\\n[http://www.modernisation.gouv.fr/sites/default/files/bloc-sgmap-2.jpg]&lt; http://www.modernisation.gouv.fr/>\\n\\nBenjamin PLANCHOT | PMO\\nService « performance des services numériques »\\nDirection interministérielle du numérique et du système d'information et de communication de l'Etat\\n01 40 15 71 50 | Tour Mirabeau - 39-43 Quai André Citroën, 75015 Paris - Bureau 4027\\nmodernisation.gouv.fr&lt; http://www.modernisation.gouv.fr/></TextContent>\\n</Content>\",\n" +
                "  \"childrenAuList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID15\", \"ID20\" ]\n" +
                "  },\n" +
                "  \"dataObjectRefList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID12\" ]\n" +
                "  },\n" +
                "  \"inDataObjectPackageId\" : \"ID11\",\n" +
                "  \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\"\n" +
                "}";

        Pattern pau = Pattern.compile("\"onDiskPath\" : .*Node 1\"");
        Matcher mtestau = pau.matcher(testau);
        boolean testaupath = mtestau.find();
        testau = testau.replaceAll("\"onDiskPath\" : .*\"", "");

        ArchiveUnit au = zi.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID11");
        String sau = mapper.writeValueAsString(au);
        //System.out.println(sau);
        Matcher msau = pau.matcher(sau);
        boolean saupath = msau.find();
        sau = sau.replaceAll("\"onDiskPath\" : .*\"", "");

        assertThat(saupath).isEqualTo(testaupath);
        assertThat(sau).isEqualToNormalizingNewlines(testau);
    }
}
