package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.*;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SEDA2VersionTest {
    @Test
        // Test the SEDA2.1 and 2.2 construct from xml string (fromSedaXML)
    void testSeda2VersionComplianceTest() throws SEDALibException {
        // Given
        SEDA2Version.setSeda2Version(1);
        Content c = new Content();

        String xmlFragments="  <Event>\n" +
                "    <EventIdentifier>AUT-234452</EventIdentifier>\n" +
                "    <EventTypeCode>Autorisation</EventTypeCode>\n" +
                "    <EventDateTime>2104-05-31T01:00:00.000</EventDateTime>\n" +
                "    <Outcome>OK</Outcome>\n" +
                "    <AnyThing>OK</AnyThing>\n" +
                "    <LinkingAgentIdentifier>\n" +
                "      <LinkingAgentIdentifierType>Matricule</LinkingAgentIdentifierType>\n" +
                "      <LinkingAgentIdentifierValue>123456789</LinkingAgentIdentifierValue>\n" +
                "      <LinkingAgentRole>Archiviste</LinkingAgentRole>\n" +
                "    </LinkingAgentIdentifier>\n" +
                "  </Event>\n";
        c.addSedaXmlFragments(xmlFragments);

        // Test XML export depending on Seda2 version

        String cOut = c.toString();

        // Then in Seda2.1 version the LinkingAgentIdentifier is in expansion field and order is kept the same

        String testOut = "<Content>\n" +
                xmlFragments +
                "</Content>";
        assertThat(cOut).isEqualTo(testOut);

        // But in Seda2.2 version the LinkingAgentIdentifier is a recognized XML element and re-ordered before the
        // expansion field
        SEDA2Version.setSeda2Version(2);
        c = new Content();
        c.addSedaXmlFragments(xmlFragments);
        cOut = c.toString();
        assertThat(cOut).isNotEqualTo(testOut);
        SEDA2Version.setSeda2Version(1);
    }

    @Test
        // Test the SEDA2.1 to and from 2.2 conversion
    void testSeda2VersionConversionTest() throws SEDALibException {
        // Given
        SEDA2Version.setSeda2Version(1);
        String xmlFragments="    <DataObjectPackage>\n" +
                "    <DescriptiveMetadata>\n" +
                "      <ArchiveUnit id=\"ID10\">\n" +
                "        <Content>\n" +
                "          <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
                "          <Title>Nouvelle ArchiveUnit</Title>\n" +
                "          <Description>Ce test est pour voir ce qui se passe\n" +
                "                  après 20 espaces</Description>\n" +
                "          <Event>\n" +
                "            <EventType>TyE</EventType>\n" +
                "            <EventDateTime>2022-05-10T00:00:00.000</EventDateTime>\n" +
                "            <EventDetail>DetE</EventDetail>\n" +
                "            <LinkingAgentIdentifier>\n" +
                "              <LinkingAgentIdentifierType>ty</LinkingAgentIdentifierType>\n" +
                "              <LinkingAgentIdentifierValue>va</LinkingAgentIdentifierValue>\n" +
                "              <LinkingAgentRole>ro</LinkingAgentRole>\n" +
                "            </LinkingAgentIdentifier>\n" +
                "            <Other>123</Other>\n" +
                "          </Event>\n" +
                "        </Content>\n" +
                "      </ArchiveUnit>\n" +
                "    </DescriptiveMetadata>\n" +
                "    <ManagementMetadata>\n" +
                "      <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n" +
                "      <LegalStatus>Public Archive</LegalStatus>\n" +
                "      <OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n" +
                "      <SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n" +
                "    </ManagementMetadata>\n" +
                "  </DataObjectPackage>";

        // Test the import in Seda2.1

        DataObjectPackage dop=null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlFragments.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais)) {
            xmlReader.nextUsefullEvent();
            dop=DataObjectPackage.fromSedaXml(xmlReader, "", null);
        } catch (XMLStreamException | IOException | InterruptedException e) {
            assertThat(e).isNull();
        }

        Content c=dop.getArchiveUnitById("ID10").getContent();
        boolean foundLinking=false;
        for (SEDAMetadata sm : c.metadataList) {
            if (sm.getXmlElementName().equals("Event")){
                Event event=(Event) sm;
                for (SEDAMetadata osm : event.metadataList){
                    if (osm instanceof LinkingAgentIdentifierType) {
                        foundLinking = true;
                        break;
                    }
                }
            }
        }
        assertThat(foundLinking).isFalse();


        // Then the Event in ArchiveUnit has a LinkingAgentIdentifierType metadata

        try {
            dop= SEDA2Version.convertToSeda2Version(dop,2,null);
        } catch (InterruptedException ignored) {
        }
        SEDA2Version.setSeda2Version(2);

        c=dop.getArchiveUnitById("ID10").getContent();
        foundLinking=false;
        for (SEDAMetadata sm : c.metadataList) {
            if (sm.getXmlElementName().equals("Event")){
                Event event=(Event) sm;
                for (SEDAMetadata osm : event.metadataList){
                    if (osm instanceof LinkingAgentIdentifierType) {
                        foundLinking = true;
                        break;
                    }
                }
            }
        }
        assertThat(foundLinking).isTrue();
        SEDA2Version.setSeda2Version(1);
    }
}
