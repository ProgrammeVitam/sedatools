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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SedaContextExtension.class)
class DataObjectPackageTest {

    @Test
    void testCycleDetectionOK() throws SEDALibException, InterruptedException {
        // Given
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/TestSip.zip",
            "target/tmpJunit/TestSIP.zip-tmpdir",
            null
        );
        si.doImport();
        SIPToArchiveTransferImporter wrongSi = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples" + "/TestSipCyclic.zip",
            "target/tmpJunit/TestSIPCyclic.zip-tmpdir",
            null
        );
        wrongSi.doImport();

        // When test for acyclic
        si.getArchiveTransfer().getDataObjectPackage().verifyAcyclic();
        // Then ok
    }

    @Test
    void testCycleDetectionKO() throws SEDALibException, InterruptedException {
        // Given
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples" + "/TestSipCyclic.zip",
            "target/tmpJunit/TestSipCyclic.zip-tmpdir",
            null
        );
        si.doImport();

        // When test for acyclic, then KO
        assertThatThrownBy(() -> {
            si.getArchiveTransfer().getDataObjectPackage().verifyAcyclic();
            fail("Devrait détecter un cycle");
        }).hasMessageContaining("ID4->ID8->ID20->ID34->ID8");
    }

    @Test
    void testDogNormalisation() throws SEDALibException, InterruptedException, JsonProcessingException {
        //Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/TestSipWrongDogReferences.zip",
            "target/tmpJunit/TestSipWrongDogReferences.zip-tmpdir",
            null
        );
        si.doImport();
        try {
            si.getArchiveTransfer().getDataObjectPackage().verifyDogUnicityCapacity();
            fail("Devrait détecter un problème de normalisation des DataObjectGroup");
        } catch (SEDALibException e) {
            System.err.println(e.getMessage());
            assert (e.getMessage().contains("impossible sur l'ArchiveUnit [ID21]"));
        }

        si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/TestSipDogMerge.zip",
            "target/tmpJunit/TestSipDogMerge.zip-tmpdir",
            null
        );
        si.doImport();
        String testau =
            "{\r\n" +
            "  \"archiveUnitProfileXmlData\" : null,\r\n" +
            "  \"managementXmlData\" : null,\r\n" +
            "  \"contentXmlData\" : \"<Content>\\n              <DescriptionLevel>Item</DescriptionLevel>\\n              <Title>20160429_tuleap.pdf</Title>\\n              <Description>Document \\\"20160429_tuleap.pdf\\\" joint au message &lt;a8f34cc23a55bf2de3606d4e45609230@culture.gouv.fr></Description>\\n            </Content>\",\r\n" +
            "  \"childrenAuList\" : {\r\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\r\n" +
            "  },\r\n" +
            "  \"dataObjectRefList\" : {\r\n" +
            "    \"inDataObjectPackageIdList\" : [ \"ID16\", \"ID22\", \"ID200\", \"ID201\" ]\r\n" +
            "  },\r\n" +
            "  \"inDataObjectPackageId\" : \"ID21\",\r\n" +
            "  \"onDiskPath\" : null\r\n" +
            "}";

        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID21");
        String sau = mapper.writeValueAsString(au);
        assertThat(testau).isEqualToNormalizingNewlines(sau);

        si.getArchiveTransfer().getDataObjectPackage().normalizeUniqDataObjectGroup();

        testau = "{\r\n" +
        "  \"archiveUnitProfileXmlData\" : null,\r\n" +
        "  \"managementXmlData\" : null,\r\n" +
        "  \"contentXmlData\" : \"<Content>\\n              <DescriptionLevel>Item</DescriptionLevel>\\n              <Title>20160429_tuleap.pdf</Title>\\n              <Description>Document \\\"20160429_tuleap.pdf\\\" joint au message &lt;a8f34cc23a55bf2de3606d4e45609230@culture.gouv.fr></Description>\\n            </Content>\",\r\n" +
        "  \"childrenAuList\" : {\r\n" +
        "    \"inDataObjectPackageIdList\" : [ ]\r\n" +
        "  },\r\n" +
        "  \"dataObjectRefList\" : {\r\n" +
        "    \"inDataObjectPackageIdList\" : [ \"ID52\" ]\r\n" +
        "  },\r\n" +
        "  \"inDataObjectPackageId\" : \"ID21\",\r\n" +
        "  \"onDiskPath\" : null\r\n" +
        "}";

        //		for (Map.Entry<String, ArchiveUnit> pair : si.getArchiveTransfer().getDataObjectPackage()
        //				.getAuInDataObjectPackageIdMap().entrySet()) {
        //			if (pair.getValue().contentXmlData.contains("20160429_tuleap.pdf")) {
        //				au = pair.getValue();
        //				break;
        //			}
        //		}
        au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID21");
        sau = mapper.writeValueAsString(au);
        //		System.out.println(sau);
        assertThat(testau).isEqualToNormalizingNewlines(sau);
        System.err.println("La fusion des DOG a bien eue lieu");

        String testog =
            "{\n" +
            "  \"binaryDataObjectList\" : [ {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"BinaryMaster_1\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID17.ods\"\n" +
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
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID17\",\n" +
            "    \n" +
            "  }, {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"TextContent_1\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID19.txt\"\n" +
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
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID19\",\n" +
            "    \n" +
            "  }, {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"BinaryMaster_1\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID23.pdf\"\n" +
            "    }, {\n" +
            "      \"type\" : \"DigestType\",\n" +
            "      \"elementName\" : \"MessageDigest\",\n" +
            "      \"value\" : \"559dc14b4821f78aa138bb72923214c0f3635f0262f63999ba9c78d8df7833206f5e8310dedff60e9522c502ae3a5fe4e444c8e333efffac0f9c242b8f7a27f6\",\n" +
            "      \"algorithm\" : \"SHA-512\"\n" +
            "    }, {\n" +
            "      \"type\" : \"IntegerType\",\n" +
            "      \"elementName\" : \"Size\",\n" +
            "      \"value\" : 3868571\n" +
            "    }, {\n" +
            "      \"type\" : \"FormatIdentification\",\n" +
            "      \"elementName\" : \"FormatIdentification\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatLitteral\",\n" +
            "        \"value\" : \"Acrobat PDF 1.4 - Portable Document Format\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"MimeType\",\n" +
            "        \"value\" : \"application/pdf\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatId\",\n" +
            "        \"value\" : \"fmt/18\"\n" +
            "      } ]\n" +
            "    }, {\n" +
            "      \"type\" : \"FileInfo\",\n" +
            "      \"elementName\" : \"FileInfo\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"Filename\",\n" +
            "        \"value\" : \"20160429-tuleap.pdf\"\n" +
            "      }, {\n" +
            "        \"type\" : \"DateTimeType\",\n" +
            "        \"elementName\" : \"LastModified\",\n" +
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID23\",\n" +
            "    \n" +
            "  }, {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"TextContent_1\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID24.txt\"\n" +
            "    }, {\n" +
            "      \"type\" : \"DigestType\",\n" +
            "      \"elementName\" : \"MessageDigest\",\n" +
            "      \"value\" : \"14a0a17426b8b356f7769faede46fd09391689c5362939b8b5d8559fda5908b8579072cb802a87856f172401ded5f8bcf3c0315340da415b71e6f86deef72545\",\n" +
            "      \"algorithm\" : \"SHA-512\"\n" +
            "    }, {\n" +
            "      \"type\" : \"IntegerType\",\n" +
            "      \"elementName\" : \"Size\",\n" +
            "      \"value\" : 5104\n" +
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
            "        \"value\" : \"20160429-tuleap.pdf.txt\"\n" +
            "      }, {\n" +
            "        \"type\" : \"DateTimeType\",\n" +
            "        \"elementName\" : \"LastModified\",\n" +
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID24\",\n" +
            "    \n" +
            "  }, {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"BinaryMaster_2\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID200.json\"\n" +
            "    }, {\n" +
            "      \"type\" : \"DigestType\",\n" +
            "      \"elementName\" : \"MessageDigest\",\n" +
            "      \"value\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\n" +
            "      \"algorithm\" : \"SHA-512\"\n" +
            "    }, {\n" +
            "      \"type\" : \"IntegerType\",\n" +
            "      \"elementName\" : \"Size\",\n" +
            "      \"value\" : 120\n" +
            "    }, {\n" +
            "      \"type\" : \"FormatIdentification\",\n" +
            "      \"elementName\" : \"FormatIdentification\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatLitteral\",\n" +
            "        \"value\" : \"JSON Data Interchange Format\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"MimeType\",\n" +
            "        \"value\" : \"application/json\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatId\",\n" +
            "        \"value\" : \"fmt/817\"\n" +
            "      } ]\n" +
            "    }, {\n" +
            "      \"type\" : \"FileInfo\",\n" +
            "      \"elementName\" : \"FileInfo\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"Filename\",\n" +
            "        \"value\" : \"SmallContract2.json\"\n" +
            "      }, {\n" +
            "        \"type\" : \"DateTimeType\",\n" +
            "        \"elementName\" : \"LastModified\",\n" +
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID200\",\n" +
            "    \n" +
            "  }, {\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"DataObjectVersion\",\n" +
            "      \"value\" : \"BinaryMaster_3\"\n" +
            "    }, {\n" +
            "      \"type\" : \"StringType\",\n" +
            "      \"elementName\" : \"Uri\",\n" +
            "      \"value\" : \"content/ID201.json\"\n" +
            "    }, {\n" +
            "      \"type\" : \"DigestType\",\n" +
            "      \"elementName\" : \"MessageDigest\",\n" +
            "      \"value\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\n" +
            "      \"algorithm\" : \"SHA-512\"\n" +
            "    }, {\n" +
            "      \"type\" : \"IntegerType\",\n" +
            "      \"elementName\" : \"Size\",\n" +
            "      \"value\" : 120\n" +
            "    }, {\n" +
            "      \"type\" : \"FormatIdentification\",\n" +
            "      \"elementName\" : \"FormatIdentification\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatLitteral\",\n" +
            "        \"value\" : \"JSON Data Interchange Format\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"MimeType\",\n" +
            "        \"value\" : \"application/json\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"FormatId\",\n" +
            "        \"value\" : \"fmt/817\"\n" +
            "      } ]\n" +
            "    }, {\n" +
            "      \"type\" : \"FileInfo\",\n" +
            "      \"elementName\" : \"FileInfo\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"Filename\",\n" +
            "        \"value\" : \"SmallContract3.json\"\n" +
            "      }, {\n" +
            "        \"type\" : \"DateTimeType\",\n" +
            "        \"elementName\" : \"LastModified\",\n" +
            "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
            "      } ]\n" +
            "    } ],\n" +
            "    \"inDataObjectPackageId\" : \"ID201\",\n" +
            "    \n" +
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
            "  \"logBook\" : {\n" +
            "    \"type\" : \"LogBook\",\n" +
            "    \"elementName\" : \"LogBook\",\n" +
            "    \"metadataList\" : [ {\n" +
            "      \"type\" : \"Event\",\n" +
            "      \"elementName\" : \"Event\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"EventIdentifier\",\n" +
            "        \"value\" : \"event0001\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"EventDetail\",\n" +
            "        \"value\" : \"One event\"\n" +
            "      } ]\n" +
            "    }, {\n" +
            "      \"type\" : \"Event\",\n" +
            "      \"elementName\" : \"Event\",\n" +
            "      \"metadataList\" : [ {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"EventIdentifier\",\n" +
            "        \"value\" : \"event0002\"\n" +
            "      }, {\n" +
            "        \"type\" : \"StringType\",\n" +
            "        \"elementName\" : \"EventDetail\",\n" +
            "        \"value\" : \"Two event\"\n" +
            "      } ]\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"inDataObjectPackageId\" : \"ID52\",\n" +
            "  \"onDiskPath\" : null\n" +
            "}";
        DataObjectGroup og = si
            .getArchiveTransfer()
            .getDataObjectPackage()
            .getDogInDataObjectPackageIdMap()
            .get("ID52");
        System.out.println("Value to verify=" + mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og);
        sog = sog.replaceAll("\"onDiskPath\" : .*\"", "");

        testog = testog.replaceAll("\"onDiskPath\" : .*\"", "");

        assertThat(sog).isEqualToNormalizingNewlines(testog);
        System.err.println("La fusion des DOG a bien eue lieu");
    }

    @Test
    void testRegenerateIds() throws SEDALibException, InterruptedException, JsonProcessingException {
        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/TestSip" + ".zip",
            "target/tmpJunit/TestSip.zip-tmpdir",
            null
        );
        si.doImport();
        si.getArchiveTransfer().getDataObjectPackage().regenerateContinuousIds();

        String testau =
            "{\r\n" +
            "  \"archiveUnitProfileXmlData\" : null,\r\n" +
            "  \"managementXmlData\" : null,\r\n" +
            "  \"contentXmlData\" : \"<Content>\\n                  <DescriptionLevel>Item</DescriptionLevel>\\n                  <Title>3059_KO_agencies_HTML.csv</Title>\\n                </Content>\",\r\n" +
            "  \"childrenAuList\" : {\r\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\r\n" +
            "  },\r\n" +
            "  \"dataObjectRefList\" : {\r\n" +
            "    \"inDataObjectPackageIdList\" : [ \"ID46\" ]\r\n" +
            "  },\r\n" +
            "  \"inDataObjectPackageId\" : \"ID19\",\r\n" +
            "  \"onDiskPath\" : null\r\n" +
            "}";

        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID19");
        String sau = mapper.writeValueAsString(au);
        assertThat(sau).isEqualToNormalizingNewlines(testau);
    }
}
