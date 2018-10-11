package fr.gouv.vitam.tools.sedalib.metadata;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.metadata.namedtype.PersonType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class ContentTest {

	@Test
	void test() throws SEDALibException {
		Content c = new Content();

		c.addNewMetadata("DescriptionLevel", "File");
		c.addNewMetadata("DescriptionLevel", "RecordGrp");
		StringType dl=new StringType("DescriptionLevel", "Series");
		c.addMetadata(dl);
		c.addNewMetadata("Description", "TestDescription");
		c.addNewMetadata("Title", "TestTitle");
		c.addNewMetadata("Title", "TestTitleLang", "fr");
		c.addNewMetadata("EndDate", "TestDate");

		c.addSedaXmlFragments("  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" + 
				"  <RegisteredDate>2104-05-13T00:00:00</RegisteredDate>\n" + 
				"  <Event>\n" + 
				"    <EventIdentifier>AUT-234452</EventIdentifier>\n" + 
				"    <EventTypeCode>Autorisation</EventTypeCode>\n" + 
				"    <EventDateTime>2104-05-31T01:00:00</EventDateTime>\n" + 
				"    <OutCome>OK</OutCome>\n" + 
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
		
		PersonType writer=new PersonType("Writer");
		writer.addNewMetadata("FirstName", "TestPrenom");
		writer.addNewMetadata("BirthName", "TestNom");
		writer.addNewMetadata("Identifier", "ID1");
		writer.addNewMetadata("Identifier", "ID2");
		
		c.addMetadata(writer);
		
		String cOut = c.toString();
		System.out.println(cOut);
		Content cNext=(Content)SEDAMetadata.fromString(cOut, Content.class);
		String cNextOut = cNext.toString();
		System.out.println(cNextOut);
		
		String testOut="<Content>\n" + 
				"  <DescriptionLevel>Series</DescriptionLevel>\n" + 
				"  <Title>TestTitle</Title>\n" + 
				"  <Title xml:lang=\"fr\">TestTitleLang</Title>\n" + 
				"  <OriginatingSystemId>Cerfa-1244771-ID10000</OriginatingSystemId>\n" + 
				"  <Description>TestDescription</Description>\n" + 
				"  <Writer>\n" + 
				"    <FirstName>TestPrenom</FirstName>\n" + 
				"    <BirthName>TestNom</BirthName>\n" + 
				"    <Identifier>ID1</Identifier>\n" + 
				"    <Identifier>ID2</Identifier>\n" + 
				"  </Writer>\n" + 
				"  <RegisteredDate>2104-05-13T00:00:00</RegisteredDate>\n" + 
				"  <EndDate>TestDate</EndDate>\n" + 
				"  <Event>\n" + 
				"    <EventIdentifier>AUT-234452</EventIdentifier>\n" + 
				"    <EventTypeCode>Autorisation</EventTypeCode>\n" + 
				"    <EventDateTime>2104-05-31T01:00:00</EventDateTime>\n" + 
				"    <OutCome>OK</OutCome>\n" + 
				"  </Event>\n" + 
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
		
		assertEquals(testOut,cNextOut);
	}

}
