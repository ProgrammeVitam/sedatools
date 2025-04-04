package fr.gouv.vitam.tools.mailextractlib.store.javamail.handlers;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.activation.ActivationDataFlavor;
import jakarta.activation.DataContentHandler;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeBodyPart;

public class SignatureContentHandler
    implements DataContentHandler 
{
    private final ActivationDataFlavor _adf;
    private final ActivationDataFlavor[]         _dfs;
    
    SignatureContentHandler(
        ActivationDataFlavor adf,
        ActivationDataFlavor[]         dfs)
    {
        _adf = adf;
        _dfs = dfs;
    }

    public Object getContent(
        DataSource ds)
        throws IOException
    {
        return ds.getInputStream();
    }
    
    public Object getTransferData(
            ActivationDataFlavor df,
        DataSource ds) 
        throws IOException 
    {
        return HandlerUtil.getTransferData(this, _adf, df, ds);
    }
    
    public ActivationDataFlavor[] getTransferDataFlavors()
    {
        return _dfs;
    }
    
    public void writeTo(
        Object obj, 
        String mimeType,
        OutputStream os) 
        throws IOException 
    {
        if (obj instanceof MimeBodyPart) 
        {
            HandlerUtil.writeFromMimeBodyPart((MimeBodyPart)obj, os);
        }
        else
        {
            HandlerUtil.writeFromBarrInputStream(obj, os);
        }
    }
}
