package fr.gouv.vitam.tools.mailextractlib.store.javamail.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.activation.ActivationDataFlavor;
import jakarta.activation.DataContentHandler;
import jakarta.activation.DataSource;


class HandlerUtil
{

       static void writeFromInputStream(InputStream obj, OutputStream os)
        throws IOException
    {
        int         b;
        InputStream in = obj;

        if (!(in instanceof BufferedInputStream))
        {
            in = new BufferedInputStream(in);
        }

        while ((b = in.read()) >= 0)
        {
            os.write(b);
        }

        in.close();
    }

    static void writeFromBarrInputStream(Object obj, OutputStream os)
            throws IOException
    {
        if(obj instanceof byte[])
        {
            os.write((byte[])obj);
        }
        else if (obj instanceof InputStream)
        {
            writeFromInputStream((InputStream)obj, os);
        }
        else
        {
            throw new IOException("unknown object in writeTo " + obj);
        }
    }

    static void writeFromMimeBodyPart(MimeBodyPart obj, OutputStream os)
            throws IOException
    {
        try
        {
            obj.writeTo(os);
        }
        catch (MessagingException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    static Object getTransferData(DataContentHandler handler, ActivationDataFlavor adf, ActivationDataFlavor df, DataSource ds)
            throws IOException
    {
        if (adf.equals(df))
        {
            return handler.getContent(ds);
        }
        else
        {
            return null;
        }
    }
}
