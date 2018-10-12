package fr.gouv.vitam.tools.sedalib.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.attribute.FileTime;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class FileInfoTest {

    @Test
    void test() throws SEDALibException {
        // Given
        FileInfo fi = new FileInfo("TestFileName", "TestCreatingApplicationName",
                "TestCreatingApplicationVersion", "TestCreatingOs",
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
                "  <CreatingOs>TestCreatingOs</CreatingOs>\n" +
                "  <CreatingOsVersion>TestCreatingOsVersion</CreatingOsVersion>\n" +
                "  <LastModified>1970-01-01T00:00:00Z</LastModified>\n" +
                "</FileInfo>";
        assertThat(fiNextOut).isEqualTo(testOut);
    }

}
