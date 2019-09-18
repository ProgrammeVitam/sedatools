package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToDiskExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SIPImportTest implements UseTestFiles {

    @Test
    public void TestSIPOKImport() throws Exception {

        // do import of test directory
        TestUtilities.eraseAll("target/tmpJunit/SipOK.zip-tmpdir");
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/SIP_OK.zip", "target/tmpJunit/SipOK.zip-tmpdir",null);
        si.doImport();

        // assert macro results
        assertEquals(28, si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(4, si.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert one dataObjectGroup using serialization
        String testog = "{\n" +
                "  \"binaryDataObjectList\" : [ {\n" +
                "    \"compressed\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectVersion\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"BinaryMaster_1\"\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"Montparnasse.txt\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"CreatingApplicationName\",\n" +
                "        \"value\" : \"CreatingApplicationName0\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"CreatingApplicationVersion\",\n" +
                "        \"value\" : \"CreatingApplicationVersion0\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"dateTimeString\" : \"2006-05-04T18:13:51Z\",\n" +
                "        \"elementName\" : \"DateCreatedByApplication\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"CreatingOs\",\n" +
                "        \"value\" : \"CreatingOs0\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"CreatingOsVersion\",\n" +
                "        \"value\" : \"CreatingOsVersion0\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"dateTimeString\" : \"2017-04-04T08:07:06.487Z\",\n" +
                "        \"elementName\" : \"LastModified\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"formatIdentification\" : {\n" +
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
                "    },\n" +
                "    \"inDataObjectPackageId\" : \"ID13\",\n" +
                "    \"messageDigest\" : {\n" +
                "      \"type\" : \"DigestType\",\n" +
                "      \"algorithm\" : \"SHA-512\",\n" +
                "      \"elementName\" : \"MessageDigest\",\n" +
                "      \"value\" : \"86c0bc701ef6b5dd21b080bc5bb2af38097baa6237275da83a52f092c9eae3e4e4b0247391620bd732fe824d18bd3bb6c37e62ec73a8cf3585c6a799399861b1\"\n" +
                "    },\n" +
                "    \"metadata\" : null,\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\SipOK.zip-tmpdir\\\\Content\\\\ID13.txt\",\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"size\" : {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 6\n" +
                "    },\n" +
                "    \"uri\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"Uri\",\n" +
                "      \"value\" : \"Content/ID13.txt\"\n" +
                "    }\n" +
                "  } ],\n" +
                "  \"inDataObjectPackageId\" : \"ID12\",\n" +
                "  \"logBook\" : null,\n" +
                "  \"onDiskPath\" : null,\n" +
                "  \"physicalDataObjectList\" : [ {\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectVersion\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"PhysicalMaster_1\"\n" +
                "    },\n" +
                "    \"inDataObjectPackageId\" : \"ID1000\",\n" +
                "    \"onDiskPath\" : null,\n" +
                "    \"otherDimensionsAbstractXml\" : [ ],\n" +
                "    \"physicalDimensions\" : {\n" +
                "      \"type\" : \"PhysicalDimensions\",\n" +
                "      \"elementName\" : \"PhysicalDimensions\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Height\",\n" +
                "        \"unit\" : \"centimetre\",\n" +
                "        \"value\" : 21.0\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Length\",\n" +
                "        \"unit\" : \"centimetre\",\n" +
                "        \"value\" : 29.7\n" +
                "      }, {\n" +
                "        \"type\" : \"Weight\",\n" +
                "        \"elementName\" : \"Weight\",\n" +
                "        \"unit\" : \"kilogram\",\n" +
                "        \"value\" : 1.0\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"physicalId\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"PhysicalId\",\n" +
                "      \"value\" : \"12345\"\n" +
                "    },\n" +
                "    \"relationshipsXmlData\" : [ ]\n" +
                "  } ]\n" +
                "}";

        DataObjectGroup og = si.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID12");
        String sog = mapper.writeValueAsString(og);
        //System.out.println("Value to verify="+sog);
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*\"");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", ""));

        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertTrue(sogpath & testogpath);
        assertThat(sog).isEqualTo(testog);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "  \"archiveUnitProfileXmlData\" : null,\n" +
                "  \"childrenAuList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID6\" ]\n" +
                "  },\n" +
                "  \"contentXmlData\" : \"<Content>\\n\\t\\t\\t\\t\\t<DescriptionLevel>RecordGrp</DescriptionLevel>\\n\\t\\t\\t\\t\\t<Title>1_Saint Denis Université (ligne 13)</Title>\\n\\t\\t\\t\\t    <FilePlanPosition>13.1.</FilePlanPosition>\\n\\t\\t\\t\\t    <FilePlanPosition>RATP.13.1.</FilePlanPosition>\\n\\t\\t\\t\\t    <OriginatingSystemId>123456</OriginatingSystemId>\\n\\t\\t\\t\\t    <OriginatingSystemId>AZERTY</OriginatingSystemId>\\n\\t\\t\\t\\t    <ArchivalAgencyArchiveUnitIdentifier>20170045/1</ArchivalAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t    <ArchivalAgencyArchiveUnitIdentifier>AMN.X/12</ArchivalAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t    <OriginatingAgencyArchiveUnitIdentifier>7890</OriginatingAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t    <OriginatingAgencyArchiveUnitIdentifier>QWERTY</OriginatingAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t    <TransferringAgencyArchiveUnitIdentifier>Toto1</TransferringAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t    <TransferringAgencyArchiveUnitIdentifier>1Otot</TransferringAgencyArchiveUnitIdentifier>\\n\\t\\t\\t\\t\\t<Description>Cette ArchiveUnit n'a aucune règle propre, mais récupère du ManagementMetadata du SIP un NeedAuthorization à true, une StorageRule avec un FinalAction à Copy, une AppraisalRule avec un FinalAction à Keep, une AccessRule avec la Rule ACC-00002 dont la StartDate est 01/01/2000 et une ClassificationRule dont la Rule est CLASS-00001 avec pour StartDate 03/06/2015, Confidentiel Défense pour ClassificationLevel, ClassificationOwner0 pour ClassificationOwner, ClassificationAudience0 pour ClassificationAudience, 03/06/2016 comme ClassificationReassasingDate et un NeedReassessingAuthorization à true</Description>\\n\\t\\t\\t\\t    <CustodialHistory>\\n\\t\\t\\t\\t        <CustodialHistoryItem>Saint-Denis - Université est une station de la ligne 13 du métro de Paris, située au nord de la commune de Saint-Denis, en limite de celle de Pierrefitte-sur-Seine, dans le département de la Seine-Saint-Denis. C'est le terminus de la ligne 13 sur sa branche nord-est</CustodialHistoryItem>\\n\\t\\t\\t\\t        <CustodialHistoryItem>La station est ouverte le 25 mai 1998. Son nom vient de sa proximité immédiate de l'université de Paris VIII à Saint-Denis.</CustodialHistoryItem>\\n\\t\\t\\t\\t    </CustodialHistory>\\n\\t\\t\\t\\t    <Type>Information de représentation</Type>\\n\\t\\t\\t\\t    <DocumentType>Plan</DocumentType>\\n\\t\\t\\t\\t    <Language>fr</Language>\\n\\t\\t\\t\\t    <DescriptionLanguage>fr</DescriptionLanguage>\\n\\t\\t\\t\\t    <Status>Original</Status>\\n\\t\\t\\t\\t    <Version>2</Version>\\n\\t\\t\\t\\t    <Tag>station</Tag>\\n\\t\\t\\t\\t    <Tag>métropolitain</Tag>\\n\\t\\t\\t\\t    <Keyword>\\n\\t\\t\\t\\t        <KeywordContent>Transport en commun</KeywordContent>\\n\\t\\t\\t\\t        <KeywordReference>ark:/67717/T1-1273</KeywordReference>\\n\\t\\t\\t\\t        <KeywordType>subject</KeywordType>\\n\\t\\t\\t\\t    </Keyword>\\n\\t\\t\\t\\t    <Keyword>\\n\\t\\t\\t\\t        <KeywordContent>Saint-Denis</KeywordContent>\\n\\t\\t\\t\\t        <KeywordReference>93066</KeywordReference>\\n\\t\\t\\t\\t        <KeywordType>geogname</KeywordType>\\n\\t\\t\\t\\t    </Keyword>\\n\\t\\t\\t\\t    <Coverage>\\n\\t\\t\\t\\t        <Spatial>Saint-Denis</Spatial>\\n\\t\\t\\t\\t        <Temporal>20e siècle</Temporal>\\n\\t\\t\\t\\t        <Juridictional>Communauté de communes de Plaine Commune</Juridictional>\\n\\t\\t\\t\\t    </Coverage>\\n\\t\\t\\t\\t    <OriginatingAgency>\\n\\t\\t\\t\\t        <Identifier>RATP</Identifier>\\n\\t\\t\\t\\t    </OriginatingAgency>\\n\\t\\t\\t\\t    <SubmissionAgency>\\n\\t\\t\\t\\t        <Identifier>RATP</Identifier>\\n\\t\\t\\t\\t    </SubmissionAgency>\\n\\t\\t\\t\\t\\t<AuthorizedAgent>\\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </AuthorizedAgent>\\n\\t\\t\\t\\t    <AuthorizedAgent>\\n\\t\\t\\t\\t        <Corpname>Compagnie du chemin de fer métropolitain de Paris</Corpname>\\n\\t\\t\\t\\t        <Identifier>CMP</Identifier>\\n\\t\\t\\t\\t        <Function>Transport</Function>\\n\\t\\t\\t\\t        <Activity>Transport en commun</Activity>\\n\\t\\t\\t\\t        <Position>Direction</Position>\\n\\t\\t\\t\\t        <Role>Direction</Role>\\n\\t\\t\\t\\t        <Mandate>Statuts de 1899</Mandate>\\n\\t\\t\\t\\t    </AuthorizedAgent>\\n\\t\\t\\t\\t    <Writer>\\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </Writer>\\n\\t\\t\\t\\t    <Addressee>\\t\\t\\t\\t        \\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </Addressee>\\n\\t\\t\\t\\t    <Recipient>\\t\\t\\t\\t        \\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </Recipient>\\n\\t\\t\\t\\t    <Transmitter>\\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </Transmitter>\\n\\t\\t\\t\\t    <Sender>\\n\\t\\t\\t\\t        <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t        <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t        <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t        <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t        <Gender>M</Gender>\\n\\t\\t\\t\\t        <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t        <BirthPlace>\\n\\t\\t\\t\\t            <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t            <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t            <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t            <City>Uzel</City>\\n\\t\\t\\t\\t            <Region>Bretagne</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </BirthPlace>\\n\\t\\t\\t\\t        <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t        <DeathPlace>\\n\\t\\t\\t\\t            <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t            <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t            <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t            <City>Paris</City>\\n\\t\\t\\t\\t            <Region>Ile de France</Region>\\n\\t\\t\\t\\t            <Country>France</Country>\\n\\t\\t\\t\\t        </DeathPlace>\\n\\t\\t\\t\\t        <Nationality>Française</Nationality>\\n\\t\\t\\t\\t        <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t        <Function>Ingénierie</Function>\\n\\t\\t\\t\\t        <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t        <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t        <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t        <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t    </Sender>\\n\\t\\t\\t\\t    <Source>Wikipedia</Source>\\n\\t\\t\\t\\t    <RelatedObjectReference>\\n\\t\\t\\t\\t        <IsVersionOf>\\n\\t\\t\\t\\t            <ArchiveUnitRefId>ID14</ArchiveUnitRefId>\\n\\t\\t\\t\\t        </IsVersionOf>\\n\\t\\t\\t\\t        <Replaces>\\n\\t\\t\\t\\t            <DataObjectReference>\\n\\t\\t\\t\\t                <DataObjectGroupReferenceId>ID12</DataObjectGroupReferenceId>\\n\\t\\t\\t\\t            </DataObjectReference>\\n\\t\\t\\t\\t        </Replaces>\\n\\t\\t\\t\\t        <Requires>\\n\\t\\t\\t\\t            <RepositoryArchiveUnitPID>19850526/6</RepositoryArchiveUnitPID>\\n\\t\\t\\t\\t        </Requires>\\n\\t\\t\\t\\t        <IsPartOf>\\n\\t\\t\\t\\t            <RepositoryObjectPID>12345676890</RepositoryObjectPID>\\n\\t\\t\\t\\t        </IsPartOf>\\n\\t\\t\\t\\t        <References>\\n\\t\\t\\t\\t            <ExternalReference>4-LK18-3389</ExternalReference>\\n\\t\\t\\t\\t        </References>\\n\\t\\t\\t\\t    </RelatedObjectReference>\\n\\t\\t\\t\\t    <CreatedDate>2017-01-01</CreatedDate>\\n\\t\\t\\t\\t    <TransactedDate>2017-01-01</TransactedDate>\\n\\t\\t\\t\\t    <AcquiredDate>2017-01-01</AcquiredDate>\\n\\t\\t\\t\\t    <SentDate>2017-01-01</SentDate>\\n\\t\\t\\t\\t    <ReceivedDate>2017-01-01</ReceivedDate>\\n\\t\\t\\t\\t    <RegisteredDate>2017-01-01</RegisteredDate>\\n\\t\\t\\t\\t    <StartDate>2017-04-04T08:07:06</StartDate>\\n\\t\\t\\t\\t\\t<EndDate>2017-04-04T08:07:06</EndDate>\\n\\t\\t\\t\\t    <Event>\\n\\t\\t\\t\\t        <EventIdentifier>123456</EventIdentifier>\\n\\t\\t\\t\\t        <EventTypeCode>Ligne_ouverture</EventTypeCode>\\n\\t\\t\\t\\t        <EventType>Ouverture</EventType>\\n\\t\\t\\t\\t        <EventDateTime>1998-05-25T08:07:06</EventDateTime>\\n\\t\\t\\t\\t        <EventDetail>Ouverture de la station à l'exploitation</EventDetail>\\n\\t\\t\\t\\t        <Outcome>OK</Outcome>\\n\\t\\t\\t\\t        <OutcomeDetail>Ligne_Ouverture_OK</OutcomeDetail>\\n\\t\\t\\t\\t        <OutcomeDetailMessage>Ouverture de la station effectuée avec succès</OutcomeDetailMessage>\\n\\t\\t\\t\\t        <EventDetailData>500 personnes présentes</EventDetailData>\\n\\t\\t\\t\\t    </Event>\\n\\t\\t\\t\\t    <Signature>\\n\\t\\t\\t\\t        <Signer>\\n\\t\\t\\t\\t            <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t            <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t            <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t            <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t            <Gender>M</Gender>\\n\\t\\t\\t\\t            <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t            <BirthPlace>\\n\\t\\t\\t\\t                <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t                <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t                <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t                <City>Uzel</City>\\n\\t\\t\\t\\t                <Region>Bretagne</Region>\\n\\t\\t\\t\\t                <Country>France</Country>\\n\\t\\t\\t\\t            </BirthPlace>\\n\\t\\t\\t\\t            <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t            <DeathPlace>\\n\\t\\t\\t\\t                <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t                <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t                <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t                <City>Paris</City>\\n\\t\\t\\t\\t                <Region>Ile de France</Region>\\n\\t\\t\\t\\t                <Country>France</Country>\\n\\t\\t\\t\\t            </DeathPlace>\\n\\t\\t\\t\\t            <Nationality>Française</Nationality>\\n\\t\\t\\t\\t            <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t            <SigningTime>1998-04-04T08:07:06</SigningTime>\\n\\t\\t\\t\\t            <Function>Ingénierie</Function>\\n\\t\\t\\t\\t            <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t            <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t            <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t            <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t        </Signer>\\n\\t\\t\\t\\t        <Validator>\\n\\t\\t\\t\\t            <FirstName>Fulgence Marie Auguste</FirstName>\\n\\t\\t\\t\\t            <BirthName>Bienvenüe</BirthName>\\n\\t\\t\\t\\t            <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\\n\\t\\t\\t\\t            <GivenName>Le père du métro</GivenName>\\n\\t\\t\\t\\t            <Gender>M</Gender>\\n\\t\\t\\t\\t            <BirthDate>1852-01-27</BirthDate>\\n\\t\\t\\t\\t            <BirthPlace>\\n\\t\\t\\t\\t                <Geogname>Uzel</Geogname>\\n\\t\\t\\t\\t                <Address>Place de la Mairie</Address>\\n\\t\\t\\t\\t                <PostalCode>22460</PostalCode>\\n\\t\\t\\t\\t                <City>Uzel</City>\\n\\t\\t\\t\\t                <Region>Bretagne</Region>\\n\\t\\t\\t\\t                <Country>France</Country>\\n\\t\\t\\t\\t            </BirthPlace>\\n\\t\\t\\t\\t            <DeathDate>1936-08-03</DeathDate>\\n\\t\\t\\t\\t            <DeathPlace>\\n\\t\\t\\t\\t                <Geogname>Paris</Geogname>\\n\\t\\t\\t\\t                <Address>Hôpital Boucicaut</Address>\\n\\t\\t\\t\\t                <PostalCode>75015</PostalCode>\\n\\t\\t\\t\\t                <City>Paris</City>\\n\\t\\t\\t\\t                <Region>Ile de France</Region>\\n\\t\\t\\t\\t                <Country>France</Country>\\n\\t\\t\\t\\t            </DeathPlace>\\n\\t\\t\\t\\t            <Nationality>Française</Nationality>\\n\\t\\t\\t\\t            <Identifier>0000 0000 5488 9547</Identifier>\\n\\t\\t\\t\\t            <ValidationTime>1998-04-04T08:07:06</ValidationTime>\\n\\t\\t\\t\\t            <Function>Ingénierie</Function>\\n\\t\\t\\t\\t            <Activity>Conception de lignes de métro</Activity>\\n\\t\\t\\t\\t            <Position>Chef de l'inspection des Ponts et Chaussées</Position>\\n\\t\\t\\t\\t            <Role>Directeur des travaux</Role>\\n\\t\\t\\t\\t            <Mandate>Loi du 30 mars 1898</Mandate>\\n\\t\\t\\t\\t        </Validator>\\n\\t\\t\\t\\t        <ReferencedObject>\\n\\t\\t\\t\\t            <SignedObjectId>ID13</SignedObjectId>\\n\\t\\t\\t\\t            <SignedObjectDigest algorithm=\\\"SHA-512\\\">86c0bc701ef6b5dd21b080bc5bb2af38097baa6237275da83a52f092c9eae3e4e4b0247391620bd732fe824d18bd3bb6c37e62ec73a8cf3585c6a799399861b1</SignedObjectDigest>\\n\\t\\t\\t\\t        </ReferencedObject>\\n\\t\\t\\t\\t    </Signature>\\n\\t\\t\\t\\t    <Gps>\\n\\t\\t\\t\\t        <GpsVersionID>Système géodésique WGS 84</GpsVersionID>\\n\\t\\t\\t\\t        <GpsAltitude>36</GpsAltitude>\\n\\t\\t\\t\\t        <GpsAltitudeRef>0</GpsAltitudeRef>\\n\\t\\t\\t\\t        <GpsLatitude>48 56 45.395</GpsLatitude>\\n\\t\\t\\t\\t        <GpsLatitudeRef>N</GpsLatitudeRef>\\n\\t\\t\\t\\t        <GpsLongitude>2 21 49.964</GpsLongitude>\\n\\t\\t\\t\\t        <GpsLongitudeRef>E</GpsLongitudeRef>\\n\\t\\t\\t\\t        <GpsDateStamp>2018-03-01T11:04:00</GpsDateStamp>\\n\\t\\t\\t\\t    </Gps>\\n\\t\\t\\t\\t\\t<Arrangement>Hiérarchique</Arrangement>\\n\\t\\t\\t\\t</Content>\",\n" +
                "  \"dataObjectRefList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ ]\n" +
                "  },\n" +
                "  \"inDataObjectPackageId\" : \"ID4\",\n" +
                "  \"managementXmlData\" : null,\n" +
                "  \"onDiskPath\" : null\n" +
                "}";

        Pattern pau = Pattern.compile("\"onDiskPath\" : .*\"");
        Matcher mtestau = pau.matcher(testau);
        boolean testaupath = mtestau.find();
        testau = TestUtilities.LineEndNormalize(testau.replaceAll("\"onDiskPath\" : .*\"", ""));

        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID4");
        String sau = mapper.writeValueAsString(au);
        //System.out.println(sau);
        Matcher msau = pau.matcher(sau);
        boolean saupath = msau.find();
        sau = TestUtilities.LineEndNormalize(sau.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertThat(saupath).isEqualTo(testaupath);
        assertThat(sau).isEqualTo(testau);

        // test decoding of complex content metadata on this ArchiveUnit
        String testContent="<Content>\n" +
                "  <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "  <Title>1_Saint Denis Université (ligne 13)</Title>\n" +
                "  <FilePlanPosition>13.1.</FilePlanPosition>\n" +
                "  <FilePlanPosition>RATP.13.1.</FilePlanPosition>\n" +
                "  <OriginatingSystemId>123456</OriginatingSystemId>\n" +
                "  <OriginatingSystemId>AZERTY</OriginatingSystemId>\n" +
                "  <ArchivalAgencyArchiveUnitIdentifier>20170045/1</ArchivalAgencyArchiveUnitIdentifier>\n" +
                "  <ArchivalAgencyArchiveUnitIdentifier>AMN.X/12</ArchivalAgencyArchiveUnitIdentifier>\n" +
                "  <OriginatingAgencyArchiveUnitIdentifier>7890</OriginatingAgencyArchiveUnitIdentifier>\n" +
                "  <OriginatingAgencyArchiveUnitIdentifier>QWERTY</OriginatingAgencyArchiveUnitIdentifier>\n" +
                "  <TransferringAgencyArchiveUnitIdentifier>Toto1</TransferringAgencyArchiveUnitIdentifier>\n" +
                "  <TransferringAgencyArchiveUnitIdentifier>1Otot</TransferringAgencyArchiveUnitIdentifier>\n" +
                "  <Description>Cette ArchiveUnit n'a aucune règle propre, mais récupère du ManagementMetadata du SIP un NeedAuthorization à true, une StorageRule avec un FinalAction à Copy, une AppraisalRule avec un FinalAction à Keep, une AccessRule avec la Rule ACC-00002 dont la StartDate est 01/01/2000 et une ClassificationRule dont la Rule est CLASS-00001 avec pour StartDate 03/06/2015, Confidentiel Défense pour ClassificationLevel, ClassificationOwner0 pour ClassificationOwner, ClassificationAudience0 pour ClassificationAudience, 03/06/2016 comme ClassificationReassasingDate et un NeedReassessingAuthorization à true</Description>\n" +
                "  <CustodialHistory>\n" +
                "    <CustodialHistoryItem>Saint-Denis - Université est une station de la ligne 13 du métro de Paris, située au nord de la commune de Saint-Denis, en limite de celle de Pierrefitte-sur-Seine, dans le département de la Seine-Saint-Denis. C'est le terminus de la ligne 13 sur sa branche nord-est</CustodialHistoryItem>\n" +
                "    <CustodialHistoryItem>La station est ouverte le 25 mai 1998. Son nom vient de sa proximité immédiate de l'université de Paris VIII à Saint-Denis.</CustodialHistoryItem>\n" +
                "  </CustodialHistory>\n" +
                "  <Type>Information de représentation</Type>\n" +
                "  <DocumentType>Plan</DocumentType>\n" +
                "  <Language>fr</Language>\n" +
                "  <DescriptionLanguage>fr</DescriptionLanguage>\n" +
                "  <Status>Original</Status>\n" +
                "  <Version>2</Version>\n" +
                "  <Tag>station</Tag>\n" +
                "  <Tag>métropolitain</Tag>\n" +
                "  <Keyword>\n" +
                "    <KeywordContent>Transport en commun</KeywordContent>\n" +
                "    <KeywordReference>ark:/67717/T1-1273</KeywordReference>\n" +
                "    <KeywordType>subject</KeywordType>\n" +
                "  </Keyword>\n" +
                "  <Keyword>\n" +
                "    <KeywordContent>Saint-Denis</KeywordContent>\n" +
                "    <KeywordReference>93066</KeywordReference>\n" +
                "    <KeywordType>geogname</KeywordType>\n" +
                "  </Keyword>\n" +
                "  <Coverage>\n" +
                "    <Spatial>Saint-Denis</Spatial>\n" +
                "    <Temporal>20e siècle</Temporal>\n" +
                "    <Juridictional>Communauté de communes de Plaine Commune</Juridictional>\n" +
                "  </Coverage>\n" +
                "  <OriginatingAgency>\n" +
                "    <Identifier>RATP</Identifier>\n" +
                "  </OriginatingAgency>\n" +
                "  <SubmissionAgency>\n" +
                "    <Identifier>RATP</Identifier>\n" +
                "  </SubmissionAgency>\n" +
                "  <AuthorizedAgent>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </AuthorizedAgent>\n" +
                "  <AuthorizedAgent>\n" +
                "    <Corpname>Compagnie du chemin de fer métropolitain de Paris</Corpname>\n" +
                "    <Identifier>CMP</Identifier>\n" +
                "    <Function>Transport</Function>\n" +
                "    <Activity>Transport en commun</Activity>\n" +
                "    <Position>Direction</Position>\n" +
                "    <Role>Direction</Role>\n" +
                "    <Mandate>Statuts de 1899</Mandate>\n" +
                "  </AuthorizedAgent>\n" +
                "  <Writer>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </Writer>\n" +
                "  <Addressee>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </Addressee>\n" +
                "  <Recipient>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </Recipient>\n" +
                "  <Transmitter>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </Transmitter>\n" +
                "  <Sender>\n" +
                "    <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "    <BirthName>Bienvenüe</BirthName>\n" +
                "    <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "    <GivenName>Le père du métro</GivenName>\n" +
                "    <Gender>M</Gender>\n" +
                "    <BirthDate>1852-01-27</BirthDate>\n" +
                "    <BirthPlace>\n" +
                "      <Geogname>Uzel</Geogname>\n" +
                "      <Address>Place de la Mairie</Address>\n" +
                "      <PostalCode>22460</PostalCode>\n" +
                "      <City>Uzel</City>\n" +
                "      <Region>Bretagne</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </BirthPlace>\n" +
                "    <DeathDate>1936-08-03</DeathDate>\n" +
                "    <DeathPlace>\n" +
                "      <Geogname>Paris</Geogname>\n" +
                "      <Address>Hôpital Boucicaut</Address>\n" +
                "      <PostalCode>75015</PostalCode>\n" +
                "      <City>Paris</City>\n" +
                "      <Region>Ile de France</Region>\n" +
                "      <Country>France</Country>\n" +
                "    </DeathPlace>\n" +
                "    <Nationality>Française</Nationality>\n" +
                "    <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "    <Function>Ingénierie</Function>\n" +
                "    <Activity>Conception de lignes de métro</Activity>\n" +
                "    <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "    <Role>Directeur des travaux</Role>\n" +
                "    <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "  </Sender>\n" +
                "  <Source>Wikipedia</Source>\n" +
                "  <RelatedObjectReference>\n" +
                "    <IsVersionOf>\n" +
                "      <ArchiveUnitRefId>ID14</ArchiveUnitRefId>\n" +
                "    </IsVersionOf>\n" +
                "    <Replaces>\n" +
                "      <DataObjectReference>\n" +
                "        <DataObjectGroupReferenceId>ID12</DataObjectGroupReferenceId>\n" +
                "      </DataObjectReference>\n" +
                "    </Replaces>\n" +
                "    <Requires>\n" +
                "      <RepositoryArchiveUnitPID>19850526/6</RepositoryArchiveUnitPID>\n" +
                "    </Requires>\n" +
                "    <IsPartOf>\n" +
                "      <RepositoryObjectPID>12345676890</RepositoryObjectPID>\n" +
                "    </IsPartOf>\n" +
                "    <References>\n" +
                "      <ExternalReference>4-LK18-3389</ExternalReference>\n" +
                "    </References>\n" +
                "  </RelatedObjectReference>\n" +
                "  <CreatedDate>2017-01-01T00:00:00</CreatedDate>\n" +
                "  <TransactedDate>2017-01-01T00:00:00</TransactedDate>\n" +
                "  <AcquiredDate>2017-01-01T00:00:00</AcquiredDate>\n" +
                "  <SentDate>2017-01-01T00:00:00</SentDate>\n" +
                "  <ReceivedDate>2017-01-01T00:00:00</ReceivedDate>\n" +
                "  <RegisteredDate>2017-01-01T00:00:00</RegisteredDate>\n" +
                "  <StartDate>2017-04-04T08:07:06</StartDate>\n" +
                "  <EndDate>2017-04-04T08:07:06</EndDate>\n" +
                "  <Event>\n" +
                "    <EventIdentifier>123456</EventIdentifier>\n" +
                "    <EventTypeCode>Ligne_ouverture</EventTypeCode>\n" +
                "    <EventType>Ouverture</EventType>\n" +
                "    <EventDateTime>1998-05-25T08:07:06</EventDateTime>\n" +
                "    <EventDetail>Ouverture de la station à l'exploitation</EventDetail>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "    <OutcomeDetail>Ligne_Ouverture_OK</OutcomeDetail>\n" +
                "    <OutcomeDetailMessage>Ouverture de la station effectuée avec succès</OutcomeDetailMessage>\n" +
                "    <EventDetailData>500 personnes présentes</EventDetailData>\n" +
                "  </Event>\n" +
                "  <Signature>\n" +
                "    <Signer>\n" +
                "      <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "      <BirthName>Bienvenüe</BirthName>\n" +
                "      <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "      <GivenName>Le père du métro</GivenName>\n" +
                "      <Gender>M</Gender>\n" +
                "      <BirthDate>1852-01-27</BirthDate>\n" +
                "      <BirthPlace>\n" +
                "        <Geogname>Uzel</Geogname>\n" +
                "        <Address>Place de la Mairie</Address>\n" +
                "        <PostalCode>22460</PostalCode>\n" +
                "        <City>Uzel</City>\n" +
                "        <Region>Bretagne</Region>\n" +
                "        <Country>France</Country>\n" +
                "      </BirthPlace>\n" +
                "      <DeathDate>1936-08-03</DeathDate>\n" +
                "      <DeathPlace>\n" +
                "        <Geogname>Paris</Geogname>\n" +
                "        <Address>Hôpital Boucicaut</Address>\n" +
                "        <PostalCode>75015</PostalCode>\n" +
                "        <City>Paris</City>\n" +
                "        <Region>Ile de France</Region>\n" +
                "        <Country>France</Country>\n" +
                "      </DeathPlace>\n" +
                "      <Nationality>Française</Nationality>\n" +
                "      <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "      <SigningTime>1998-04-04T08:07:06</SigningTime>\n" +
                "      <Function>Ingénierie</Function>\n" +
                "      <Activity>Conception de lignes de métro</Activity>\n" +
                "      <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "      <Role>Directeur des travaux</Role>\n" +
                "      <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "    </Signer>\n" +
                "    <Validator>\n" +
                "      <FirstName>Fulgence Marie Auguste</FirstName>\n" +
                "      <BirthName>Bienvenüe</BirthName>\n" +
                "      <FullName>Fulgence Marie Auguste Bienvenüe</FullName>\n" +
                "      <GivenName>Le père du métro</GivenName>\n" +
                "      <Gender>M</Gender>\n" +
                "      <BirthDate>1852-01-27</BirthDate>\n" +
                "      <BirthPlace>\n" +
                "        <Geogname>Uzel</Geogname>\n" +
                "        <Address>Place de la Mairie</Address>\n" +
                "        <PostalCode>22460</PostalCode>\n" +
                "        <City>Uzel</City>\n" +
                "        <Region>Bretagne</Region>\n" +
                "        <Country>France</Country>\n" +
                "      </BirthPlace>\n" +
                "      <DeathDate>1936-08-03</DeathDate>\n" +
                "      <DeathPlace>\n" +
                "        <Geogname>Paris</Geogname>\n" +
                "        <Address>Hôpital Boucicaut</Address>\n" +
                "        <PostalCode>75015</PostalCode>\n" +
                "        <City>Paris</City>\n" +
                "        <Region>Ile de France</Region>\n" +
                "        <Country>France</Country>\n" +
                "      </DeathPlace>\n" +
                "      <Nationality>Française</Nationality>\n" +
                "      <Identifier>0000 0000 5488 9547</Identifier>\n" +
                "      <ValidationTime>1998-04-04T08:07:06</ValidationTime>\n" +
                "      <Function>Ingénierie</Function>\n" +
                "      <Activity>Conception de lignes de métro</Activity>\n" +
                "      <Position>Chef de l'inspection des Ponts et Chaussées</Position>\n" +
                "      <Role>Directeur des travaux</Role>\n" +
                "      <Mandate>Loi du 30 mars 1898</Mandate>\n" +
                "    </Validator>\n" +
                "    <ReferencedObject>\n" +
                "      <SignedObjectId>ID13</SignedObjectId>\n" +
                "      <SignedObjectDigest algorithm=\"SHA-512\">86c0bc701ef6b5dd21b080bc5bb2af38097baa6237275da83a52f092c9eae3e4e4b0247391620bd732fe824d18bd3bb6c37e62ec73a8cf3585c6a799399861b1</SignedObjectDigest>\n" +
                "    </ReferencedObject>\n" +
                "  </Signature>\n" +
                "  <Gps>\n" +
                "    <GpsVersionID>Système géodésique WGS 84</GpsVersionID>\n" +
                "    <GpsAltitude>36</GpsAltitude>\n" +
                "    <GpsAltitudeRef>0</GpsAltitudeRef>\n" +
                "    <GpsLatitude>48 56 45.395</GpsLatitude>\n" +
                "    <GpsLatitudeRef>N</GpsLatitudeRef>\n" +
                "    <GpsLongitude>2 21 49.964</GpsLongitude>\n" +
                "    <GpsLongitudeRef>E</GpsLongitudeRef>\n" +
                "    <GpsDateStamp>2018-03-01T11:04:00</GpsDateStamp>\n" +
                "  </Gps>\n" +
                "  <Arrangement>Hiérarchique</Arrangement>\n" +
                "</Content>";
        String content=au.getContent().toString();

        assertThat(content).isEqualTo(testContent);

        // test decoding of complex management metadata on this ArchiveUnit
        au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID8");
        String testManagement="<Management>\n" +
                "  <StorageRule>\n" +
                "    <Rule>STO-00001</Rule>\n" +
                "    <StartDate>2000-01-01</StartDate>\n" +
                "    <FinalAction>Copy</FinalAction>\n" +
                "  </StorageRule>\n" +
                "  <AppraisalRule>\n" +
                "    <Rule>APP-00001</Rule>\n" +
                "    <StartDate>2001-01-01</StartDate>\n" +
                "    <FinalAction>Destroy</FinalAction>\n" +
                "  </AppraisalRule>\n" +
                "  <DisseminationRule>\n" +
                "    <Rule>DIS-00001</Rule>\n" +
                "    <StartDate>2000-01-01</StartDate>\n" +
                "  </DisseminationRule>\n" +
                "  <ReuseRule>\n" +
                "    <Rule>REU-00001</Rule>\n" +
                "    <StartDate>2000-01-01</StartDate>\n" +
                "  </ReuseRule>\n" +
                "</Management>";
        String management=au.getManagement().toString();

        assertThat(management).isEqualTo(testManagement);

    }

    @Test
    public void TestSipWrongDogReferences() throws Exception {

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSipWrongDogReferences.zip", "target/tmpJunit/TestSipWrongDogReferences.zip-tmpdir", null);
        si.doImport();

        ArchiveTransferToDiskExporter atde = new ArchiveTransferToDiskExporter(si.getArchiveTransfer(), null);
        TestUtilities.eraseAll("target/tmpJunit/SWLMV2");
        atde.doExport("target/tmpJunit/SWLMV2");

        // do import of test directory
        DiskToArchiveTransferImporter dai1;
        dai1 = new DiskToArchiveTransferImporter("target/tmpJunit/SWLMV2", null);

        dai1.addIgnorePattern("Thumbs.db");
        dai1.addIgnorePattern("pagefile.sys");
        dai1.doImport();

        //fix date for future test
        dai1.getArchiveTransfer().getGlobalMetadata().date = "2018-09-30T14:33:24";
        dai1.getArchiveTransfer().getGlobalMetadata().setNowFlag(false);

        ArchiveTransferToSIPExporter attse;
        attse = new ArchiveTransferToSIPExporter(dai1.getArchiveTransfer(), null);
        attse.doExportToSEDAXMLManifest("target/tmpJunit/SWLMV2.xml", true, true);

        // assert macro results
        assertEquals(dai1.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size(), 22);
        assertEquals(dai1.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size(), 11);

        // do export of test directory
        ArchiveTransferToDiskExporter adi;
        adi = new ArchiveTransferToDiskExporter(dai1.getArchiveTransfer(), null);
        TestUtilities.eraseAll("target/tmpJunit/SWLMV2.1");
        adi.doExport("target/tmpJunit/SWLMV2.1");

        // do reimport of test directory
        DiskToArchiveTransferImporter dai2;
        dai2 = new DiskToArchiveTransferImporter("target/tmpJunit/SWLMV2.1", null);

        dai2.addIgnorePattern("Thumbs.db");
        dai2.addIgnorePattern("pagefile.sys");
        dai2.doImport();

        attse = new ArchiveTransferToSIPExporter(dai2.getArchiveTransfer(), null);
        attse.doExportToSEDAXMLManifest("target/tmpJunit/SWLMV2.1.xml", true, true);

        String gm1 = dai1.getArchiveTransfer().getGlobalMetadata().toSedaXmlFragments();
        String gm2 = dai2.getArchiveTransfer().getGlobalMetadata().toSedaXmlFragments();

        assertEquals(gm1, gm2);

        assertTrue(FileUtils.contentEquals(new File("target/tmpJunit/SWLMV2.1.xml"), new File("target/tmpJunit/SWLMV2.xml")));
    }
}
