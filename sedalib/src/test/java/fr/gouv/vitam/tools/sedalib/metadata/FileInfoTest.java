package fr.gouv.vitam.tools.sedalib.metadata;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileInfoTest {

    @Test
    void test() throws SEDALibException {
        // Given
        FileInfo fi = new FileInfo("TestFileName", "TestCreatingApplicationName",
                "TestCreatingApplicationVersion",
                LocalDateTime.parse("2006-05-04T18:13:51.0", ISO_DATE_TIME),
                "TestCreatingOs",
                "TestCreatingOsVersion", FileTime.fromMillis(0));

        String fiOut = fi.toString();
//        System.out.println("Value to verify=" + fiOut);

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

}
