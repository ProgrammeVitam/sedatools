package fr.gouv.vitam.tools.mailextractlib.store.javamail.handlers;

import jakarta.activation.ActivationDataFlavor;
import jakarta.mail.internet.MimeBodyPart;

public class pgp
    extends SignatureContentHandler
{
    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(MimeBodyPart.class, "application/pgp-signature", "PGP Signature");
    private static final ActivationDataFlavor[]         DFS = new ActivationDataFlavor[] { ADF };

    public pgp()
    {
        super(ADF, DFS);
    }
}
