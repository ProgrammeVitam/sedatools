package fr.gouv.vitam.tools.sedalib.metadata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.PersonType;
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

        // Test PersonType metadata
        c.addNewMetadata("Recipient", "TestFirstName1", "TestBirthName1");
        c.addNewMetadata("Recipient", "TestFirstName2", "TestBirthName2", "TestIdentifier2");
        PersonType writer = new PersonType("Writer");
        writer.addNewMetadata("FirstName", "TestPrenom");
        writer.addNewMetadata("BirthName", "TestNom");
        writer.addNewMetadata("Identifier", "ID1");
        writer.addNewMetadata("Identifier", "ID2");
        c.addMetadata(writer);

        // Test GenericXMLBlock metadata
        c.addNewMetadata("XMLTest", "<XMLTest><TestTag>TestValue</TestTag></XMLTest>");

        // Test Event metadata
        Event event=new Event();
        event.addNewMetadata("EventDateTime",LocalDateTime.of(1970,1,1,1,0));
        event.addNewMetadata("Outcome","OK");
        c.addMetadata(event);

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
                "  <Writer>\n" +
                "    <FirstName>TestPrenom</FirstName>\n" +
                "    <BirthName>TestNom</BirthName>\n" +
                "    <Identifier>ID1</Identifier>\n" +
                "    <Identifier>ID2</Identifier>\n" +
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
                .hasMessageContaining("Impossible de construire"); // for PersonType
        assertThatThrownBy(() -> c.addNewMetadata("XMLTest", new Date(0)))
                .hasMessageContaining("Impossible de construire"); // for GenericXMLBlock
        Event event=new Event();
        assertThatThrownBy(() -> event.addNewMetadata("EventDateTime", "Date"))
                .hasMessageContaining("Impossible de construire"); // for DateTimeType
        assertThatThrownBy(() -> event.addNewMetadata("NewElement", "<NewElement></NewElement>"))
                .hasMessageContaining("Impossible d'étendre"); // for DateTimeType
        assertThatThrownBy(() -> event.addSedaXmlFragments("<NewElement>Test</NewElement>"))
                .hasMessageContaining("Impossible d'étendre"); // for DateTimeType
    }
}
