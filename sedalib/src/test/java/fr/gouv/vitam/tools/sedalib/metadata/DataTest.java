package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DataTest {

    @Test
    void testFileInfo() throws SEDALibException {
        // Given
        FileInfo fi = new FileInfo("TestFileName", "TestCreatingApplicationName",
                "TestCreatingApplicationVersion",
                LocalDateTime.parse("2006-05-04T18:13:51.0", ISO_DATE_TIME),
                "TestCreatingOs",
                "TestCreatingOsVersion", FileTime.fromMillis(0));

        String fiOut = fi.toString();

        // When read write in XML string format
        FileInfo fiNext = (FileInfo) SEDAMetadata.fromString(fiOut, FileInfo.class);
        String fiNextOut = fiNext.toString();

        //Then
        String testOut = "<FileInfo>\n" +
                "  <Filename>TestFileName</Filename>\n" +
                "  <CreatingApplicationName>TestCreatingApplicationName</CreatingApplicationName>\n" +
                "  <CreatingApplicationVersion>TestCreatingApplicationVersion</CreatingApplicationVersion>\n" +
                "  <DateCreatedByApplication>2006-05-04T18:13:51</DateCreatedByApplication>\n"+
                "  <CreatingOs>TestCreatingOs</CreatingOs>\n" +
                "  <CreatingOsVersion>TestCreatingOsVersion</CreatingOsVersion>\n" +
                "  <LastModified>1970-01-01T00:00:00</LastModified>\n" +
                "</FileInfo>";
        assertThat(fiNextOut).isEqualTo(testOut);
    }

    @Test
    void testFormatIdentification() throws SEDALibException {
        // Given
        FormatIdentification fi = new FormatIdentification("TestFormatLitteral", "TestMimeType", "TestFormatId", "TestEncoding");

        String fiOut = fi.toString();

        // When read write in XML string format
        FormatIdentification fiNext = (FormatIdentification) SEDAMetadata.fromString(fiOut, FormatIdentification.class);
        String fiNextOut = fiNext.toString();

        //Then
        String testOut = "<FormatIdentification>\n" +
                "  <FormatLitteral>TestFormatLitteral</FormatLitteral>\n" +
                "  <MimeType>TestMimeType</MimeType>\n" +
                "  <FormatId>TestFormatId</FormatId>\n" +
                "  <Encoding>TestEncoding</Encoding>\n" +
                "</FormatIdentification>";
        assertThat(fiNextOut).isEqualTo(testOut);
    }

    @Test
    void testPhysicalDimensions() throws SEDALibException {
        // Given
        PhysicalDimensions pd = new PhysicalDimensions();
        pd.addNewMetadata("Width",10.0,"metre");
        pd.addNewMetadata("Height",20.0,"centimetre");
        pd.addNewMetadata("Depth",1000.10,"millimetre");
        pd.addNewMetadata("Shape","rectangle");
        pd.addNewMetadata("Diameter",1.0,"metre");
        pd.addNewMetadata("Length",40.5,"centimetre");
        pd.addNewMetadata("Thickness",100.0,"millimetre");
        pd.addNewMetadata("Weight",100.0,"gram");
        pd.addNewMetadata("NumberOfPage",123);

        String pdOut = pd.toString();

        // When read write in XML string format
        PhysicalDimensions pdNext = (PhysicalDimensions) SEDAMetadata.fromString(pdOut, PhysicalDimensions.class);
        String pdNextOut = pdNext.toString();

        //Then
        String testOut = "<PhysicalDimensions>\n" +
                "  <Width unit=\"metre\">10.0</Width>\n" +
                "  <Height unit=\"centimetre\">20.0</Height>\n" +
                "  <Depth unit=\"millimetre\">1000.1</Depth>\n" +
                "  <Shape>rectangle</Shape>\n" +
                "  <Diameter unit=\"metre\">1.0</Diameter>\n" +
                "  <Length unit=\"centimetre\">40.5</Length>\n" +
                "  <Thickness unit=\"millimetre\">100.0</Thickness>\n" +
                "  <Weight unit=\"gram\">100.0</Weight>\n" +
                "  <NumberOfPage>123</NumberOfPage>\n" +
                "</PhysicalDimensions>";
        assertThat(pdNextOut).isEqualTo(testOut);
    }

    @Test
    void testMetadata() throws SEDALibException {
        // Given
        Metadata m = new Metadata();
        m.addNewMetadata("Audio","<Codec>mp3</Codec><Volume>98</Volume>");
        m.addNewMetadata("Quality","<Quality>bad</Quality>");

        String mOut = m.toString();

        // When read write in XML string format
        Metadata mNext = (Metadata) SEDAMetadata.fromString(mOut, Metadata.class);
        String mNextOut = mNext.toString();

        //Then
        String testOut = "<Metadata>\n" +
                "  <Audio>\n" +
                "    <Codec>mp3</Codec>\n" +
                "    <Volume>98</Volume>\n" +
                "  </Audio>\n" +
                "  <Quality>bad</Quality>\n" +
                "</Metadata>";
        assertThat(mNextOut).isEqualTo(testOut);
    }

}
