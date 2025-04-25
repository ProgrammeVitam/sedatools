package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.metadata.content.*;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ContentTest {

    @Test
        // Test Content and ComplexListType subclass
    void testConstructors() throws SEDALibException, FileNotFoundException {
        // Given
        Content content = new Content();

        // When loaded with all different kind of metadata

        //Test StringType metadata
        StringType stringType = new StringType("DescriptionLevel", "Series");
        content.addMetadata(stringType);

        // Test TextType metadata
        TextType textType = new TextType("Description", "TestDescription EN", "en");
        content.addMetadata(textType);

        // Test AgencyType metadata
        AgencyType agencyType = new AgencyType("OriginatingAgency", "Agency1");
        content.addMetadata(agencyType);

        // Test AgentType and PlaceType metadata
        AgentType writer = new AgentType("Writer");
        writer.addNewMetadata("FirstName", "TestPrenom");
        writer.addNewMetadata("BirthName", "TestNom");
        writer.addNewMetadata("Identifier", "ID1");
        writer.addNewMetadata("Identifier", "ID2");
        writer.addNewMetadata("Function", "F1");
        writer.addNewMetadata("BirthDate", LocalDate.of(2019, 7, 14));
        PlaceType birthPlace = new PlaceType("BirthPlace");
        birthPlace.addNewMetadata("Geogname", "Location1");
        birthPlace.addNewMetadata("Country", "Country1");
        writer.addMetadata(birthPlace);
        writer.addNewMetadata("DeathDate", LocalDate.of(2099, 7, 14));
        content.addMetadata(writer);

        // Test AnyXMLType metadata
        AnyXMLType anyXMLType = new AnyXMLType("XMLTest", "<XMLTest><TestTag>TestValue</TestTag></XMLTest>");
        content.addMetadata(anyXMLType);

        // Test IntegerType metadata and expandability
        IntegerType integerType = new IntegerType("UniversalNumber", 42);
        content.addMetadata(integerType);

        // Test Signe metadata
        Signer signer = new Signer("Laura Tremoulet", LocalDateTime.of(1970, 1, 1, 2, 0));
        signer.addNewMetadata("BirthPlace", "TestLocation1");

        // Test Signature metadata
        Signature signature = new Signature();
        signature.addNewMetadata("Signer", "Paul", "Dupont", LocalDateTime.of(1970, 1, 1, 1, 0));
        signature.addNewMetadata("Signer", "Martin Durant", LocalDateTime.of(1970, 1, 2, 1, 0));
        signature.addNewMetadata("Signer", "Emilie", "Martin", LocalDateTime.of(1970, 1, 2, 1, 0), "emilie.martin@corp.fr");
        signature.addNewMetadata("Validator", "The corporation", LocalDateTime.of(1970, 1, 3, 1, 0));
        signature.addNewMetadata("ReferencedObject", "TestID1", "TestDigest1", "TestSHA");
        signature.addMetadata(signer);
        content.addMetadata(signature);

        // Test Coverage metadata
        Coverage coverage = new Coverage();
        coverage.addNewMetadata("Spatial", "TestSpatial1");
        coverage.addNewMetadata("Spatial", "TestSpatial2");
        coverage.addNewMetadata("Spatial", "TestSpatial3");
        coverage.addNewMetadata("Temporal", "TestTemporal1");
        coverage.addNewMetadata("Temporal", "TestTemporal2");
        coverage.addNewMetadata("Juridictional", "TestJuridictional1");
        content.addMetadata(coverage);

        // Test CustodialHistory metadata
        CustodialHistory custodialHistory = new CustodialHistory("TestItem1", "TestItem2", "TestItem3", "TestItem4");
        content.addMetadata(custodialHistory);

        // Test Event metadata and expandability
        Event event = new Event();
        event.addNewMetadata("EventDateTime", LocalDateTime.of(1970, 1, 1, 1, 0));
        event.addNewMetadata("Outcome", "OK");
        event.addNewMetadata("Information", "<Information>OK</Information>");
        content.addMetadata(event);

        // Test Keyword and KeywordType metadata
        Keyword keyword = new Keyword();
        EnumType keywordType = new EnumType("KeywordType","subject");
        keyword.addMetadata(keywordType);
        content.addMetadata(keyword);

        // Test RelatedObjectReference
        RelatedObjectReference relatedObjectReference = new RelatedObjectReference();
        relatedObjectReference.addNewMetadata("IsVersionOf", "DogRefId");
        DataObjectOrArchiveUnitReferenceType isVersionOf = new DataObjectOrArchiveUnitReferenceType("IsVersionOf");
        isVersionOf.addNewMetadata("ArchiveUnitRefId", "AURefId");
        DataObjectOrArchiveUnitReferenceType replaces = new DataObjectOrArchiveUnitReferenceType("Replaces");
        replaces.addNewMetadata("DataObjectReference", "DoRefId", null);
        DataObjectOrArchiveUnitReferenceType requires = new DataObjectOrArchiveUnitReferenceType("Requires");
        requires.addNewMetadata("RepositoryArchiveUnitPID", "AuPid");
        DataObjectOrArchiveUnitReferenceType isPartOf = new DataObjectOrArchiveUnitReferenceType("IsPartOf");
        isPartOf.addNewMetadata("RepositoryObjectPID", "DoPid");
        DataObjectOrArchiveUnitReferenceType references = new DataObjectOrArchiveUnitReferenceType("References");
        references.addNewMetadata("ExternalReference", "ExtrenalRef");
        relatedObjectReference.addMetadata(isVersionOf);
        relatedObjectReference.addMetadata(replaces);
        relatedObjectReference.addMetadata(requires);
        relatedObjectReference.addMetadata(isPartOf);
        relatedObjectReference.addMetadata(references);
        content.addMetadata(relatedObjectReference);

        // Test GPS metadata
        content.addNewMetadata("Gps", "TestVersion", 10000, "-TestLatitude", "-TestLongitude", "TestDateStamp");

        // Test AnyXMLType metadata
        content.addNewMetadata("XMLTest", "<XMLTest><TestTag>TestValue</TestTag></XMLTest>");

        // Test free content import and automatic sorting in ComplexListType
        content.addSedaXmlFragments("  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" +
                "  <RegisteredDate>2104-05-13T00:00:00</RegisteredDate>\n" +
                "  <Event>\n" +
                "    <EventIdentifier>AUT-234452</EventIdentifier>\n" +
                "    <EventTypeCode>Autorisation</EventTypeCode>\n" +
                "    <EventDateTime>2104-05-31T01:00:00</EventDateTime>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "  </Event>\n" +
                "  <Frog>\n" +
                "    <CommonName>Rouge à laser</CommonName>\n" +
                "    <ScientificName>Rubra Rana Laseri</ScientificName>\n" +
                "  </Frog>\n" +
                "  <Requirer>\n" +
                "    <Identifier>REF-16F14A19BF22</Identifier>\n" +
                "    <FirstName>Edouard</FirstName>\n" +
                "    <BirthName>AFINA</BirthName>\n" +
                "  </Requirer>\n" +
                "  <Frog>\n" +
                "    <CommonName>Verte à détente</CommonName>\n" +
                "    <ScientificName>Viridi Rana Elasticis</ScientificName>\n" +
                "  </Frog>");

        String cOut = content.toString();

        // Test read write in XML string format
        Content cNext = (Content) SEDAMetadata.fromString(cOut, Content.class);
        String cNextOut = cNext.toString();

        // Then
        assertThat(cNextOut).isNotEqualToIgnoringWhitespace(ResourceUtils.getResourceAsString("metadata/content_01.xml"));

    }

    @Test
    void testCsvList() throws SEDALibException, FileNotFoundException {
        // Given
        String contentXmlData = ResourceUtils.getResourceAsString("metadata/content_01.xml");
        Content content = (Content) Content.fromString(contentXmlData, Content.class);

        // When the csv list is generated
        LinkedHashMap<String, String> csvList = content.toCsvList();
        StringBuilder tmp = new StringBuilder();
        for (String header : csvList.keySet()) {
            tmp.append(header).append(": ").append(csvList.get(header)).append("\n");
        }

        // Then
        assertThat(csvList).hasSize(68);
        assertThat(tmp.toString()).isEqualTo(ResourceUtils.getResourceAsString("metadata/content_01_csv_list.txt"));
    }

    @Test
    void testFilteredCsvList() throws SEDALibException, FileNotFoundException {
        // Given
        List<String> filter = Arrays.asList("Description", "Title", "Event", "Frog");

        Content content = (Content) Content.fromString(ResourceUtils.getResourceAsString("metadata/content_01.xml"),
                Content.class);

        // When the csv list is generated
        LinkedHashMap<String, String> csvList = content.externToCsvList(filter);
        StringBuilder tmp = new StringBuilder();
        for (String header : csvList.keySet()) {
            tmp.append(header).append(": ").append(csvList.get(header)).append("\n");
        }

        // Then
        assertThat(csvList).hasSize(11);
        assertThat(tmp.toString()).isEqualTo(ResourceUtils.getResourceAsString("metadata/content_04_csv_list.txt"));
    }

    @Test
    void testFilteredToString() throws SEDALibException, FileNotFoundException {
        // Given
        List<String> filter = Arrays.asList("Description", "Title", "Event", "Frog");

        Content content = (Content) Content.fromString(ResourceUtils.getResourceAsString("metadata/content_01.xml"),
                Content.class);

        // When the csv list is generated
        String tmp = content.filteredToString(filter);

        // Then
        assertThat(tmp).isEqualTo(ResourceUtils.getResourceAsString("metadata/content_02.xml"));
    }


    @Test
    void testSpecificMetadata() throws SEDALibException {
        // Given
        Content c = new Content();

        // When loaded with all metadata with specific kind of creation
        c.addNewMetadata("Gps", null, -100000, "TestLatitude", "TestLongitude", null);
        c.addNewMetadata("Keyword", "TestKeywordContent", null, null);

        String cOut = c.toString();

        // Then
        String testOut = "<Content>\n" +
                "  <Keyword>\n" +
                "    <KeywordContent>TestKeywordContent</KeywordContent>\n" +
                "  </Keyword>\n" +
                "  <Gps>\n" +
                "    <GpsLatitude>TestLatitude</GpsLatitude>\n" +
                "    <GpsLatitudeRef>N</GpsLatitudeRef>\n" +
                "    <GpsLongitude>TestLongitude</GpsLongitude>\n" +
                "    <GpsLongitudeRef>E</GpsLongitudeRef>\n" +
                "  </Gps>\n" +
                "</Content>";
        assertThat(cOut).isEqualTo(testOut);
    }

    @Test
    void testSeda2Version() throws SEDALibException {
        // Given
        Content c = new Content();

        // When loaded with all metadata with specific kind of creation
        c.addNewMetadata("Gps", null, -100000, "TestLatitude", "TestLongitude", null);
        c.addNewMetadata("Keyword", "TestKeywordContent", null, null);

        // and add Agent in SEDA 2.1 version then
        assertThatThrownBy(() -> c.addNewMetadata("Agent", "TestFirstName", "TestBirthName", "TestIdentifier"))
                .isInstanceOf(SEDALibException.class).hasStackTraceContaining("Pas de constructeur de l'élément [Agent]");

        // and add Agent in SEDA 2.1 version then
        SEDA2Version.setSeda2Version(2);
        c.addNewMetadata("Agent", "TestFirstName", "TestBirthName", "TestIdentifier");
        String cOut = c.toString();
        String testOut = "<Content>\n" +
                "  <Keyword>\n" +
                "    <KeywordContent>TestKeywordContent</KeywordContent>\n" +
                "  </Keyword>\n" +
                "  <Agent>\n" +
                "    <FirstName>TestFirstName</FirstName>\n" +
                "    <BirthName>TestBirthName</BirthName>\n" +
                "    <Identifier>TestIdentifier</Identifier>\n" +
                "  </Agent>\n" +
                "  <Gps>\n" +
                "    <GpsLatitude>TestLatitude</GpsLatitude>\n" +
                "    <GpsLatitudeRef>N</GpsLatitudeRef>\n" +
                "    <GpsLongitude>TestLongitude</GpsLongitude>\n" +
                "    <GpsLongitudeRef>E</GpsLongitudeRef>\n" +
                "  </Gps>\n" +
                "</Content>";
        assertThat(cOut).isEqualTo(testOut);
        SEDA2Version.setSeda2Version(1);
    }


    @Test
    void testSomeExceptions() {
        // Given
        Content c = new Content();

        // When loaded with all different kind of metadata

        //Test wrong args in addNewMetadata
        assertThatThrownBy(() -> c.addNewMetadata("DescriptionLevel", "Test1", "Test2"))
                .hasStackTraceContaining("Pas de constructeur"); // for StringType
        assertThatThrownBy(() -> c.addNewMetadata("Description", "Test1", "Test2", "Test3"))
                .hasStackTraceContaining("Pas de constructeur"); // for TextType
        assertThatThrownBy(() -> c.addNewMetadata("Recipient", "Test", 1))
                .hasStackTraceContaining("Pas de constructeur"); // for AgentType
        assertThatThrownBy(() -> c.addNewMetadata("XMLTest", new Date(0)))
                .hasStackTraceContaining("Pas de constructeur"); // for GenericXMLBlock
        Event event = new Event();
        assertThatThrownBy(() -> event.addNewMetadata("EventDateTime", "Date"))
                .hasStackTraceContaining("Impossible de construire"); // for DateTimeType
        // Test Keyword metadata with wrong KeywordType
        assertThatThrownBy(() -> c.addNewMetadata("Keyword", "TestKeywordContent", "TestKeywordReference", "notconvenient"))
                .hasStackTraceContaining("Impossible de construire l'élément [Keyword]");

    }

    @Test
    void testAddNewMetadata() throws SEDALibException, FileNotFoundException {
        // Given
        Content content = new Content();

        // When loaded with all different kind of metadata (in random order)

        // Test expandability
        AgentType beneficiary = new AgentType("Beneficiary", "TestFirstName", "TestBirthName", "TestIdentifier");
        content.addMetadata(beneficiary);

        // Test Signer metadata
        Signature signature = new Signature();
        signature.addNewMetadata("Signer", "TestFullName", LocalDateTime.of(1970, 1, 1, 1, 0));
        signature.addNewMetadata("Signer", "TestFirstName", "TestBirthName", LocalDateTime.of(1970, 1, 1, 1, 0));
        signature.addNewMetadata("Signer", "TestFirstName", "TestBirthName", LocalDateTime.of(1970, 1, 1, 1, 0), "TestIdentifier");
        signature.addNewMetadata("Validator", "TestFullName", LocalDateTime.of(1970, 1, 1, 1, 0));
        signature.addNewMetadata("ReferencedObject", "TestSystemID", "TestDigest");
        content.addMetadata(signature);

        // Test Keyword metadata
        content.addNewMetadata("Keyword", "TestKeywordContent", "TestKeywordReference", "corpname");

        // Test Keyword metadata
        content.addNewMetadata("Keyword", "TestKeywordContent2", null, null);

        // Test GPS metadata
        content.addNewMetadata("Gps", "TestVersion", 10000, "-TestLatitude", "-TestLongitude", "TestDateStamp");

        // Test Event metadata
        content.addNewMetadata("Event", "TestEventIdentifier2", null, null, null);
        content.addNewMetadata("Event", "TestEventIdentifier", "TestEventType",
                LocalDateTime.of(1970, 1, 1, 1, 0), "TestOutcome");

        // Test CustodialHistory metadata
        content.addNewMetadata("CustodialHistory", "TestItem1", "TestItem2", "TestItem3", "TestItem4");

        // Test Coverage metadata
        Coverage coverage = new Coverage();
        coverage.addNewMetadata("Spatial", "TestSpatial1");
        coverage.addNewMetadata("Spatial", "TestSpatial2");
        coverage.addNewMetadata("Spatial", "TestSpatial3");
        coverage.addNewMetadata("Temporal", "TestTemporal1");
        coverage.addNewMetadata("Temporal", "TestTemporal2");
        coverage.addNewMetadata("Juridictional", "TestJuridictional1");
        content.addMetadata(coverage);

        // Test AgencyType metadata
        content.addNewMetadata("OriginatingAgency", "TestAgencyIdentifier");

        // Test AgentType metadata
        content.addNewMetadata("Writer", "TestFullName");
        content.addNewMetadata("Writer", "TestFirstName", "TestBirthName");
        content.addNewMetadata("Writer", "TestFirstName", "TestBirthName", "TestIdentifier");
        AgentType writer = new AgentType("Writer", "TestFullName");
        writer.addNewMetadata("BirthPlace", "TestGeogname");
        content.addMetadata(writer);

        // Test DateTimeType metadata
        content.addNewMetadata("CreatedDate", LocalDateTime.of(2019, 1, 1, 1, 0));

        // Test TextType metadata
        content.addNewMetadata("Description", "TestDescription");
        content.addNewMetadata("Title", "TestTitle");
        content.addNewMetadata("Title", "TestTitleLang", "de"); // verify that many values are accumulated

        //Test StringType metadata
        content.addNewMetadata("DescriptionLevel", "File");
        content.addNewMetadata("DescriptionLevel", "RecordGrp"); // verify that uniq metadata is overwritten

        // Test AnyXML and expandability
        content.addSedaXmlFragments("  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" +
                "  <RegisteredDate>2104-05-13T00:00:00</RegisteredDate>\n" +
                "  <Event>\n" +
                "    <EventIdentifier>AUT-234452</EventIdentifier>\n" +
                "    <EventTypeCode>Autorisation</EventTypeCode>\n" +
                "    <EventDateTime>2104-05-31T01:00:00</EventDateTime>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "  </Event>\n" +
                "  <Frog>\n" +
                "    <CommonName>Rouge à laser</CommonName>\n" +
                "    <ScientificName>Rubra Rana Laseri</ScientificName>\n" +
                "  </Frog>\n" +
                "  <Requirer>\n" +
                "    <Identifier>REF-16F14A19BF22</Identifier>\n" +
                "    <FirstName>Edouard</FirstName>\n" +
                "    <BirthName>AFINA</BirthName>\n" +
                "  </Requirer>\n" +
                "  <Frog>\n" +
                "    <CommonName>Verte à détente</CommonName>\n" +
                "    <ScientificName>Viridi Rana Elasticis</ScientificName>\n" +
                "  </Frog>");

        String cOut = content.toString();

        // Test read write in XML string format
        Content cNext = (Content) SEDAMetadata.fromString(cOut, Content.class);
        String cNextOut = cNext.toString();

        // Then
        assertThat(cNextOut).isEqualTo(ResourceUtils.getResourceAsString("metadata/content_03.xml"));
    }
}
