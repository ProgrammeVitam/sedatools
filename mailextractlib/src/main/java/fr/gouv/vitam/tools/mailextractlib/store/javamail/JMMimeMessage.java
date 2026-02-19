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
package fr.gouv.vitam.tools.mailextractlib.store.javamail;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;

/**
 * JavaMail Message for for RFC822 uniq message file.
 * <p>
 * This class is created to bypass the protection of the specific constructor in
 * MimeMessage. It as the only action to use the general parse method of
 * MimeMessage on the InputStream constructed in RFC822Folder
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class JMMimeMessage extends MimeMessage {

    /**
     * Instantiates a new mime message in Javamail context.
     *
     * @param folder
     *            JavaMail folder
     * @param is
     *            InputStream containing the message raw smtp content
     * @param msgno
     *            Message number in folder message list
     * @throws MessagingException
     *             Messaging exception from inner JavaMail calls
     */
    public JMMimeMessage(Folder folder, InputStream is, int msgno) throws MessagingException {
        super(folder, is, msgno);
    }
}
