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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class DataTest {

    @Test
    void testFileInfo() throws SEDALibException, FileNotFoundException {
        // Given
        FileInfo fi = new FileInfo(
            "TestFileName",
            "TestCreatingApplicationName",
            "TestCreatingApplicationVersion",
            LocalDateTime.parse("2006-05-04T18:13:51.0", ISO_DATE_TIME),
            "TestCreatingOs",
            "TestCreatingOsVersion",
            FileTime.fromMillis(0)
        );

        String fiOut = fi.toString();

        // When read write in XML string format
        FileInfo fiNext = (FileInfo) SEDAMetadata.fromString(fiOut, FileInfo.class);
        String fiNextOut = fiNext.toString();

        //Then
        assertThat(fiNextOut).isEqualTo(ResourceUtils.getResourceAsString("metadata/file_info_01.xml"));
    }

    @Test
    void testFormatIdentification() throws SEDALibException {
        // Given
        FormatIdentification fi = new FormatIdentification(
            "TestFormatLitteral",
            "TestMimeType",
            "TestFormatId",
            "TestEncoding"
        );

        String fiOut = fi.toString();

        // When read write in XML string format
        FormatIdentification fiNext = (FormatIdentification) SEDAMetadata.fromString(fiOut, FormatIdentification.class);
        String fiNextOut = fiNext.toString();

        //Then
        String testOut =
            "<FormatIdentification>\n" +
            "  <FormatLitteral>TestFormatLitteral</FormatLitteral>\n" +
            "  <MimeType>TestMimeType</MimeType>\n" +
            "  <FormatId>TestFormatId</FormatId>\n" +
            "  <Encoding>TestEncoding</Encoding>\n" +
            "</FormatIdentification>";
        assertThat(fiNextOut).isEqualTo(testOut);
    }

    @Test
    void testPhysicalDimensions() throws SEDALibException, FileNotFoundException {
        // Given
        PhysicalDimensions pd = new PhysicalDimensions();
        pd.addNewMetadata("Width", 10.0, "metre");
        pd.addNewMetadata("Height", 20.0, "centimetre");
        pd.addNewMetadata("Depth", 1000.10, "millimetre");
        pd.addNewMetadata("Shape", "rectangle");
        pd.addNewMetadata("Diameter", 1.0, "metre");
        pd.addNewMetadata("Length", 40.5, "centimetre");
        pd.addNewMetadata("Thickness", 100.0, "millimetre");
        pd.addNewMetadata("Weight", 100.0, "gram");
        pd.addNewMetadata("NumberOfPage", 123);

        String pdOut = pd.toString();

        // When read write in XML string format
        PhysicalDimensions pdNext = (PhysicalDimensions) SEDAMetadata.fromString(pdOut, PhysicalDimensions.class);
        String pdNextOut = pdNext.toString();

        //Then
        assertThat(pdNextOut).isEqualTo(ResourceUtils.getResourceAsString("metadata/physical_dimension_01.xml"));
    }

    @Test
    void testMetadata() throws SEDALibException {
        // Given
        Metadata m = new Metadata();
        m.addNewMetadata("Audio", "<Codec>mp3</Codec><Volume>98</Volume>");
        m.addNewMetadata("Quality", "<Quality>bad</Quality>");

        String mOut = m.toString();

        // When read write in XML string format
        Metadata mNext = (Metadata) SEDAMetadata.fromString(mOut, Metadata.class);
        String mNextOut = mNext.toString();

        //Then
        String testOut =
            "<Metadata>\n" +
            "  <Audio>\n" +
            "    <Codec>mp3</Codec>\n" +
            "    <Volume>98</Volume>\n" +
            "  </Audio>\n" +
            "  <Quality>bad</Quality>\n" +
            "</Metadata>";
        assertThat(mNextOut).isEqualTo(testOut);
    }
}
