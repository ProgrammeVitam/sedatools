package fr.gouv.vitam.tools.mailextractlib.store.javamail.handlers;

import jakarta.activation.ActivationDataFlavor;
import jakarta.mail.internet.MimeBodyPart;

public class x_pkcs7_mime 
    extends SignatureContentHandler
{
    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(MimeBodyPart.class, "application/x-pkcs7-mime", "Encrypted Data");
    private static final ActivationDataFlavor[]         DFS = new ActivationDataFlavor[] { ADF };
    
    public x_pkcs7_mime()
    {
        super(ADF, DFS);
    }
}
