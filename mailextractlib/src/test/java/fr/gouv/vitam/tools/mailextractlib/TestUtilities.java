package fr.gouv.vitam.tools.mailextractlib;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.OtherMimeCharsetProvider;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.RFC822Headers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class TestUtilities implements AllTests{
    @Test
    public void testRFC2047() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testUtilities");
        String qencoded = "Notification \n" +
                "\td'=?unicode-1-1-utf-7?Q?+AOk-tat \n" +
                "\tde \n" +
                "\tremise \n" +
                "\t(+AOk-chec)?=";
        String bencoded ="=?iso-8859-1?B?Tm90aWZpY2F0aW9uIGQn6XRhdCBkZSByZW1pc2UgKOljaGVjKQ?=";

        OtherMimeCharsetProvider otm=new OtherMimeCharsetProvider();
        //when
        String qdecoded= RFC822Headers.decodeRfc2047Flexible(qencoded);
        String bdecoded= RFC822Headers.decodeRfc2047Flexible(bencoded);

        //then
        assertThat(qdecoded).isEqualTo("Notification d'état de remise (échec)");
        assertThat(bdecoded).isEqualTo("Notification d'état de remise (échec)");
    }

    @Test
    public void testCharsets() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testCharsets");

        OtherMimeCharsetProvider otm=new OtherMimeCharsetProvider();
        //when/then
        assertThatCode(() -> {
            Charset cs=Charset.forName("'us-ascii");
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Charset cs=Charset.forName("'usascii");
        }).isInstanceOf(IllegalCharsetNameException.class);

    }
}
