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

import jakarta.activation.ActivationDataFlavor;
import jakarta.activation.DataContentHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class HandlerUtil {

    static void writeFromInputStream(InputStream obj, OutputStream os) throws IOException {
        int b;
        InputStream in = obj;

        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }

        while ((b = in.read()) >= 0) {
            os.write(b);
        }

        in.close();
    }

    static void writeFromBarrInputStream(Object obj, OutputStream os) throws IOException {
        if (obj instanceof byte[]) {
            os.write((byte[]) obj);
        } else if (obj instanceof InputStream) {
            writeFromInputStream((InputStream) obj, os);
        } else {
            throw new IOException("unknown object in writeTo " + obj);
        }
    }

    static void writeFromMimeBodyPart(MimeBodyPart obj, OutputStream os) throws IOException {
        try {
            obj.writeTo(os);
        } catch (MessagingException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    static Object getTransferData(
        DataContentHandler handler,
        ActivationDataFlavor adf,
        ActivationDataFlavor df,
        DataSource ds
    ) throws IOException {
        if (adf.equals(df)) {
            return handler.getContent(ds);
        } else {
            return null;
        }
    }
}
