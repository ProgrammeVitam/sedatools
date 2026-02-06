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
package fr.gouv.vitam.tools.sedalib.core.seda;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.LinkingAgentIdentifier;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@ExtendWith(SedaContextExtension.class)
public class SedaVersionConverterTest {

    private SEDALibProgressLogger logger;
    private SedaVersionConverter converter;

    @BeforeEach
    public void setUp() {
        logger = mock(SEDALibProgressLogger.class);
        converter = new SedaVersionConverter(logger);
    }

    @Test
    public void testConvert_successful() throws Exception {
        // Given
        final String xmlFragments =
            "<DataObjectPackage>\n" +
            "  <DescriptiveMetadata>\n" +
            "    <ArchiveUnit id=\"ID10\">\n" +
            "      <Content>\n" +
            "        <DescriptionLevel>RecordGrp</DescriptionLevel>\n" +
            "        <Title>Nouvelle ArchiveUnit</Title>\n" +
            "        <Description>Ce test est pour voir ce qui se passe après 20 espaces</Description>\n" +
            "        <Event>\n" +
            "          <EventType>TyE</EventType>\n" +
            "          <EventDateTime>2022-05-10T00:00:00.000</EventDateTime>\n" +
            "          <EventDetail>DetE</EventDetail>\n" +
            "          <LinkingAgentIdentifier>\n" +
            "            <LinkingAgentIdentifierType>ty</LinkingAgentIdentifierType>\n" +
            "            <LinkingAgentIdentifierValue>va</LinkingAgentIdentifierValue>\n" +
            "            <LinkingAgentRole>ro</LinkingAgentRole>\n" +
            "          </LinkingAgentIdentifier>\n" +
            "          <Other>123</Other>\n" +
            "        </Event>\n" +
            "      </Content>\n" +
            "    </ArchiveUnit>\n" +
            "  </DescriptiveMetadata>\n" +
            "  <ManagementMetadata>\n" +
            "    <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n" +
            "    <LegalStatus>Public Archive</LegalStatus>\n" +
            "    <OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n" +
            "    <SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n" +
            "  </ManagementMetadata>\n" +
            "</DataObjectPackage>";

        DataObjectPackage dop;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlFragments.getBytes(StandardCharsets.UTF_8));
            SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais)) {

            xmlReader.nextUsefullEvent(); // skip StartDocument
            dop = DataObjectPackage.fromSedaXml(xmlReader, "", logger);
        }

        // When
        SedaVersionConverter converter = new SedaVersionConverter(logger);
        DataObjectPackage convertedDop = converter.convert(dop, SedaVersion.V2_1, SedaVersion.V2_2);

        // Then
        SedaContext.setVersion(SedaVersion.V2_2); // Required to use correct metadata definitions
        Content content = convertedDop.getArchiveUnitById("ID10").getContent();
        boolean foundLinking = false;

        for (SEDAMetadata metadata : content.getMetadataList()) {
            if (metadata instanceof Event) {
                Event event = (Event) metadata;
                for (SEDAMetadata subMetadata : event.getMetadataList()) {
                    if (subMetadata instanceof LinkingAgentIdentifier) {
                        foundLinking = true;
                        break;
                    }
                }
            }
        }

        assertThat(foundLinking)
            .as("LinkingAgentIdentifierType should be present after conversion")
            .isTrue();
    }

    @Test
    public void testConvert_sameVersion_throwsException() {
        DataObjectPackage original = new DataObjectPackage();

        SEDALibException thrown = assertThrows(SEDALibException.class, () -> {
            converter.convert(original, SedaVersion.V2_2, SedaVersion.V2_2);
        });

        assertTrue(thrown.getMessage().contains("identical"));
    }

    @Test
    public void testConvert_interruptedDuringSerialization_propagates() throws Exception {
        // Given a DataObjectPackage that throws InterruptedException during toSedaXml
        DataObjectPackage mockPackage = mock(DataObjectPackage.class);
        doThrow(new InterruptedException("interrupted")).when(mockPackage).toSedaXml(any(), eq(true), any());

        // When + Then
        assertThrows(InterruptedException.class, () -> {
            converter.convert(mockPackage, SedaVersion.V2_1, SedaVersion.V2_3);
        });
    }

    @Test
    public void shouldFailWhenSourceVersionAndContextVersionAreDifferent() {
        DataObjectPackage original = new DataObjectPackage();

        /**
         * @see SedaContextExtension initial SEDA version is SedaVersion.V2_1
         */
        SEDALibException thrown = assertThrows(SEDALibException.class, () -> {
            converter.convert(original, SedaVersion.V2_2, SedaVersion.V2_3);
        });

        assertTrue(thrown.getMessage().contains("different"));
    }
}