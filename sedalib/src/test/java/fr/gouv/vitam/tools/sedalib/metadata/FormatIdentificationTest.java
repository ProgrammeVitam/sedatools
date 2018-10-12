package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.nio.file.attribute.FileTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FormatIdentificationTest {

    @Test
    void test() throws SEDALibException {
        // Given
        FormatIdentification fi = new FormatIdentification("TestFormatLitteral", "TestMimeType", "TestFormatId", "TestEncoding");

        String fiOut = fi.toString();
 //       System.out.println("Value to verify=" + fiOut);

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