/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.mailextractlib.store.javamail.handlers;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import jakarta.activation.ActivationDataFlavor;
import jakarta.activation.DataContentHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

public class multipart_signed
    implements DataContentHandler
{
    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(MimeMultipart.class, "multipart/signed", "Multipart Signed");
    private static final ActivationDataFlavor[] DFS = new ActivationDataFlavor[]{ADF};

    public Object getContent(DataSource ds)
        throws IOException
    {
        try
        {
            return new MimeMultipart(ds);
        }
        catch (MessagingException ex)
        {
            return null;
        }
    }

    public Object getTransferData(ActivationDataFlavor df, DataSource ds)
        throws IOException
    {
        return HandlerUtil.getTransferData(this, ADF, df, ds);
    }

    public ActivationDataFlavor[] getTransferDataFlavors()
    {
        return DFS;
    }

    public void writeTo(Object obj, String _mimeType, OutputStream os)
        throws IOException
    {

        if (obj instanceof MimeMultipart)
        {
            try
            {
                outputBodyPart(os, obj);
            }
            catch (MessagingException ex)
            {
                throw new IOException(ex.getMessage());
            }
        }
        else
        {
            HandlerUtil.writeFromBarrInputStream(obj, os);
        }
    }

    /*
     * Output the mulitpart as a collection of leaves to make sure preamble text is not included.
     */
    private void outputBodyPart(
        OutputStream out,
        Object bodyPart)
        throws MessagingException, IOException
    {
        if (bodyPart instanceof Multipart)
        {
            Multipart mp = (Multipart)bodyPart;
            ContentType contentType = new ContentType(mp.getContentType());
            String boundary = "--" + contentType.getParameter("boundary");

            LineOutputStream lOut = new LineOutputStream(out);

            for (int i = 0; i < mp.getCount(); i++)
            {
                lOut.writeln(boundary);
                outputBodyPart(out, mp.getBodyPart(i));
                lOut.writeln();       // CRLF terminator
            }

            lOut.writeln(boundary + "--");
            return;
        }

        MimeBodyPart mimePart = (MimeBodyPart)bodyPart;

        if (mimePart.isMimeType("multipart/*"))
        {
            Object content = mimePart.getContent();

            if (content instanceof Multipart)
            {
                Multipart mp = (Multipart)content;
                ContentType contentType = new ContentType(mp.getContentType());
                String boundary = "--" + contentType.getParameter("boundary");

                LineOutputStream lOut = new LineOutputStream(out);

                Enumeration headers = mimePart.getAllHeaderLines();
                while (headers.hasMoreElements())
                {
                    lOut.writeln((String)headers.nextElement());
                }

                lOut.writeln();      // CRLF separator

                outputPreamble(lOut, mimePart, boundary);

                outputBodyPart(out, mp);
                return;
            }
        }

        mimePart.writeTo(out);
    }

    /**
     * internal preamble is generally included in signatures, while this is technically wrong,
     * if we find internal preamble we include it by default.
     */
    static void outputPreamble(LineOutputStream lOut, MimeBodyPart part, String boundary)
        throws MessagingException, IOException
    {
        InputStream in;

        try
        {
            in = part.getRawInputStream();
        }
        catch (MessagingException e)
        {
            return;            // no underlying content, rely on default generation
        }

        String line;

        while ((line = readLine(in)) != null)
        {
            if (line.equals(boundary))
            {
                break;
            }

            lOut.writeln(line);
        }

        in.close();

        if (line == null)
        {
            throw new MessagingException("no boundary found");
        }
    }

    /*
     * read a line of input stripping of the tailing \r\n
     */
    private static String readLine(InputStream in)
        throws IOException
    {
        StringBuffer b = new StringBuffer();

        int ch;
        while ((ch = in.read()) >= 0 && ch != '\n')
        {
            if (ch != '\r')
            {
                b.append((char)ch);
            }
        }

        if (ch < 0)
        {
            return null;
        }

        return b.toString();
    }

    private static class LineOutputStream
        extends FilterOutputStream
    {
        private static byte newline[];

        public LineOutputStream(OutputStream outputstream)
        {
            super(outputstream);
        }

        public void writeln(String s)
            throws MessagingException
        {
            try
            {
                byte abyte0[] = s.getBytes(StandardCharsets.UTF_8);
                super.out.write(abyte0);
                super.out.write(newline);
            }
            catch (Exception exception)
            {
                throw new MessagingException("IOException", exception);
            }
        }

        public void writeln()
            throws MessagingException
        {
            try
            {
                super.out.write(newline);
            }
            catch (Exception exception)
            {
                throw new MessagingException("IOException", exception);
            }
        }

        static
        {
            newline = new byte[2];
            newline[0] = 13;
            newline[1] = 10;
        }
    }
}
