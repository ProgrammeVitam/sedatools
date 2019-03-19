package fr.gouv.vitam.tools.sedalib.metadata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.time.LocalDateTime;
import java.util.Date;

class ContentTest {

    @Test
    // Test Content and ComplexListType subclass
    void test() throws SEDALibException {
        // Given
        Content c = new Content();

        // When loaded with all different kind of metadata

        //Test StringType metadata
        c.addNewMetadata("DescriptionLevel", "File");
        c.addNewMetadata("DescriptionLevel", "RecordGrp"); // verify that uniq metadata is overwritten
        StringType dl = new StringType("DescriptionLevel", "Series");
        c.addMetadata(dl);

        // Test TextType metadata
        c.addNewMetadata("Description", "TestDescription");
        c.addNewMetadata("Title", "TestTitle");
        c.addNewMetadata("Title", "TestTitleLang", "fr"); // verify that many values are accumulated
        TextType d = new TextType("Description", "TestDescription EN", "en");
        c.addMetadata(d);

        // Test AgentType metadata
        c.addNewMetadata("Recipient", "TestFirstName1", "TestBirthName1");
        c.addNewMetadata("Recipient", "TestFirstName2", "TestBirthName2", "TestIdentifier2");
        AgentType writer = new AgentType("Writer");
        writer.addNewMetadata("FirstName", "TestPrenom");
        writer.addNewMetadata("BirthName", "TestNom");
        writer.addNewMetadata("Identifier", "ID1");
        writer.addNewMetadata("Identifier", "ID2");
        writer.addNewMetadata("Function", "F1");
        c.addMetadata(writer);

        // Test GenericXMLBlock metadata
        c.addNewMetadata("XMLTest", "<XMLTest><TestTag>TestValue</TestTag></XMLTest>");

        // Test Event metadata
        Event event=new Event();
        event.addNewMetadata("EventDateTime",LocalDateTime.of(1970,1,1,1,0));
        event.addNewMetadata("Outcome","OK");
        c.addMetadata(event);

        // Test Signature metadata
        Signature signature=new Signature();
        signature.addNewMetadata("Signer","Paul","Dupont",LocalDateTime.of(1970,1,1,1,0));
        signature.addNewMetadata("Signer","Martin Durant",LocalDateTime.of(1970,1,2,1,0));
        signature.addNewMetadata("Signer","Emilie","Martin","emilie.martin@corp.fr",LocalDateTime.of(1970,1,2,1,0));
        signature.addNewMetadata("Validator" ,"The corporation",LocalDateTime.of(1970,1,3,1,0));
        signature.addNewMetadata("ReferencedObject","<ReferencedObject>BinaryMaster_1</ReferencedObject>");
        c.addMetadata(signature);

        // Test Coverage metadata
        Coverage coverage=new Coverage();
        coverage.addNewMetadata("Spatial","TestSpatial1");
        coverage.addNewMetadata("Spatial","TestSpatial2");
        coverage.addNewMetadata("Spatial","TestSpatial3");
        coverage.addNewMetadata("Temporal","TestTemporal1");
        coverage.addNewMetadata("Temporal","TestTemporal2");
        coverage.addNewMetadata("Juridictional","TestJuridictional1");
        c.addMetadata(coverage);

        // Test CustodialHistory metadata
        c.addNewMetadata("CustodialHistory", "TestItem1", "TestItem2", "TestItem3", "TestItem4");

        // Test Keyword metadata
        c.addNewMetadata("Keyword", "TestKeywordContent", "TestKeywordReference", "subject");

        // Test Keyword metadata with wrong CodeKeywordType
        assertThatThrownBy(()->c.addNewMetadata("Keyword", "TestKeywordContent", "TestKeywordReference", "notconvenient"))
                .hasMessageContaining("Valeur interdite");

        // Test GPS metadata
        c.addNewMetadata("Gps", "TestVersion",-100000,"-TestLatitude","-TestLongitude","TestDateStamp");

        // Test GenericXMLBlock metadata
        c.addNewMetadata("XMLTest", "<XMLTest><TestTag>TestValue</TestTag></XMLTest>");

        // Test free content import and automatic sorting in ComplexListType
        c.addSedaXmlFragments("  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" +
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

        String cOut = c.toString();
//        System.out.println("Value to verify=" + cOut);

        // Test read write in XML string format
        Content cNext = (Content) SEDAMetadata.fromString(cOut, Content.class);
        String cNextOut = cNext.toString();

        // Then

        String testOut = "<Content>\n" +
                "  <DescriptionLevel>Series</DescriptionLevel>\n" +
                "  <Title>TestTitle</Title>\n" +
                "  <Title xml:lang=\"fr\">TestTitleLang</Title>\n" +
                "  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" +
                "  <Description>TestDescription</Description>\n" +
                "  <Description xml:lang=\"en\">TestDescription EN</Description>\n" +
                "  <CustodialHistory>\n" +
                "    <CustodialHistoryItem>TestItem1</CustodialHistoryItem>\n" +
                "    <CustodialHistoryItem>TestItem2</CustodialHistoryItem>\n" +
                "    <CustodialHistoryItem>TestItem3</CustodialHistoryItem>\n" +
                "    <CustodialHistoryItem>TestItem4</CustodialHistoryItem>\n" +
                "  </CustodialHistory>\n" +
                "  <Keyword>\n" +
                "    <KeywordContent>TestKeywordContent</KeywordContent>\n" +
                "    <KeywordReference>TestKeywordReference</KeywordReference>\n" +
                "    <KeywordType>subject</KeywordType>\n" +
                "  </Keyword>\n" +
                "  <Coverage>\n" +
                "    <Spatial>TestSpatial1</Spatial>\n" +
                "    <Spatial>TestSpatial2</Spatial>\n" +
                "    <Spatial>TestSpatial3</Spatial>\n" +
                "    <Temporal>TestTemporal1</Temporal>\n" +
                "    <Temporal>TestTemporal2</Temporal>\n" +
                "    <Juridictional>TestJuridictional1</Juridictional>\n" +
                "  </Coverage>\n" +
                "  <Writer>\n" +
                "    <FirstName>TestPrenom</FirstName>\n" +
                "    <BirthName>TestNom</BirthName>\n" +
                "    <Identifier>ID1</Identifier>\n" +
                "    <Identifier>ID2</Identifier>\n" +
                "    <Function>F1</Function>\n" +
                "  </Writer>\n" +
                "  <Recipient>\n" +
                "    <FirstName>TestFirstName1</FirstName>\n" +
                "    <BirthName>TestBirthName1</BirthName>\n" +
                "  </Recipient>\n" +
                "  <Recipient>\n" +
                "    <FirstName>TestFirstName2</FirstName>\n" +
                "    <BirthName>TestBirthName2</BirthName>\n" +
                "    <Identifier>TestIdentifier2</Identifier>\n" +
                "  </Recipient>\n" +
                "  <RegisteredDate>2104-05-13T00:00:00</RegisteredDate>\n" +
                "  <Event>\n" +
                "    <EventDateTime>1970-01-01T01:00:00</EventDateTime>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "  </Event>\n" +
                "  <Event>\n" +
                "    <EventIdentifier>AUT-234452</EventIdentifier>\n" +
                "    <EventTypeCode>Autorisation</EventTypeCode>\n" +
                "    <EventDateTime>2104-05-31T01:00:00</EventDateTime>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "  </Event>\n" +
                "  <Signature>\n" +
                "    <Signer>\n" +
                "      <FirstName>Paul</FirstName>\n" +
                "      <BirthName>Dupont</BirthName>\n" +
                "      <SigningTime>1970-01-01T01:00:00</SigningTime>\n" +
                "    </Signer>\n" +
                "    <Signer>\n" +
                "      <FullName>Martin Durant</FullName>\n" +
                "      <SigningTime>1970-01-02T01:00:00</SigningTime>\n" +
                "    </Signer>\n" +
                "    <Signer>\n" +
                "      <FirstName>Emilie</FirstName>\n" +
                "      <BirthName>Martin</BirthName>\n" +
                "      <Identifier>emilie.martin@corp.fr</Identifier>\n" +
                "      <SigningTime>1970-01-02T01:00:00</SigningTime>\n" +
                "    </Signer>\n" +
                "    <Validator>\n" +
                "      <Corpname>The corporation</Corpname>\n" +
                "      <ValidationTime>1970-01-03T01:00:00</ValidationTime>\n" +
                "    </Validator>\n" +
                "    <ReferencedObject>BinaryMaster_1</ReferencedObject>\n" +
                "  </Signature>\n" +
                "  <Gps>\n" +
                "    <GpsVersionID>TestVersion</GpsVersionID>\n" +
                "    <GpsLatitude>TestLatitude</GpsLatitude>\n" +
                "    <GpsLatitudeRef>S</GpsLatitudeRef>\n" +
                "    <GpsLongitude>TestLongitude</GpsLongitude>\n" +
                "    <GpsLongitudeRef>O</GpsLongitudeRef>\n" +
                "    <GpsDateStamp>TestDateStamp</GpsDateStamp>\n" +
                "  </Gps>\n" +
                "  <XMLTest>\n" +
                "    <TestTag>TestValue</TestTag>\n" +
                "  </XMLTest>\n" +
                "  <XMLTest>\n" +
                "    <TestTag>TestValue</TestTag>\n" +
                "  </XMLTest>\n" +
                "  <Frog>\n" +
                "    <CommonName>Rouge à laser</CommonName>\n" +
                "    <ScientificName>Rubra Rana Laseri</ScientificName>\n" +
                "  </Frog>\n" +
                "  <Frog>\n" +
                "    <CommonName>Verte à détente</CommonName>\n" +
                "    <ScientificName>Viridi Rana Elasticis</ScientificName>\n" +
                "  </Frog>\n" +
                "  <Requirer>\n" +
                "    <Identifier>REF-16F14A19BF22</Identifier>\n" +
                "    <FirstName>Edouard</FirstName>\n" +
                "    <BirthName>AFINA</BirthName>\n" +
                "  </Requirer>\n" +
                "</Content>";
        assertThat(cNextOut).isEqualTo(testOut);
    }

    @Test
    void testSomeExceptions() {
        // Given
        Content c = new Content();

        // When loaded with all different kind of metadata

        //Test wrong args in addNewMetadata
        assertThatThrownBy(() -> c.addNewMetadata("DescriptionLevel", "Test1","Test2"))
                .hasMessageContaining("Impossible de construire"); // for StringType
        assertThatThrownBy(() -> c.addNewMetadata("Description", "Test1","Test2","Test3"))
                .hasMessageContaining("Impossible de construire"); // for TextType
        assertThatThrownBy(() -> c.addNewMetadata("Recipient", "Test"))
                .hasMessageContaining("Impossible de construire"); // for AgentType
        assertThatThrownBy(() -> c.addNewMetadata("XMLTest", new Date(0)))
                .hasMessageContaining("Impossible de construire"); // for GenericXMLBlock
        Event event=new Event();
        assertThatThrownBy(() -> event.addNewMetadata("EventDateTime", "Date"))
                .hasMessageContaining("Impossible de construire"); // for DateTimeType
     }
}
