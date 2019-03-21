package fr.gouv.vitam.tools.sedalib.metadata;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

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
                "  <LastModified>1970-01-01T00:00:00Z</LastModified>\n" +
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


}
