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
import jakarta.mail.internet.MimeBodyPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class x_pkcs7_signature implements DataContentHandler {

    /*
     *
     *  VARIABLES
     *
     */

    private static final ActivationDataFlavor ADF;
    private static final ActivationDataFlavor[] ADFs;

    static {
        ADF = new ActivationDataFlavor(MimeBodyPart.class, "application/x-pkcs7-signature", "Signature");
        ADFs = new ActivationDataFlavor[] { ADF };
    }

    public Object getContent(DataSource _ds) throws IOException {
        return _ds.getInputStream();
    }

    public Object getTransferData(ActivationDataFlavor _df, DataSource _ds) throws IOException {
        return HandlerUtil.getTransferData(this, ADF, _df, _ds);
    }

    public ActivationDataFlavor[] getTransferDataFlavors() {
        return ADFs;
    }

    public void writeTo(Object _obj, String _mimeType, OutputStream _os) throws IOException {
        if (_obj instanceof MimeBodyPart) {
            HandlerUtil.writeFromMimeBodyPart((MimeBodyPart) _obj, _os);
        } else if (_obj instanceof byte[]) {
            _os.write((byte[]) _obj);
        } else if (_obj instanceof InputStream) {
            HandlerUtil.writeFromInputStream((InputStream) _obj, _os);
        } else {
            throw new IOException("unknown object in writeTo " + _obj);
        }
    }
}
