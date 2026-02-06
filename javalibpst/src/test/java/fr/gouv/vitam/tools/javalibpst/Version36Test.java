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
package fr.gouv.vitam.tools.javalibpst;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Version36Test {

    @Test
    public final void testVersion36()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("example-2013.ost");
        PSTFile pstFile2 = new PSTFile(new File(dirUrl.toURI()));
        PSTFolder inbox = (PSTFolder)PSTObject.detectAndLoadPSTObject(pstFile2, 8578);
        Assert.assertEquals(
                "Number of emails in folder",
                inbox.getContentCount(),
                2);
        PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile2, 2097284);
        Assert.assertEquals(
                "correct email text.",
                "This is an e-mail message sent automatically by Microsoft "
                + "Outlook while testing the settings for your account.",
                msg.getBodyHTML().trim());
        //processFolder(pstFile2.getRootFolder());
    }

    int depth = -1;
    public void processFolder(PSTFolder folder)
            throws PSTException, IOException {
        depth++;
        // the root folder doesn't have a display name
        if (depth > 0) {
            printDepth();
            System.out.println("Folder: " + folder.getDescriptorNodeId() + " - " + folder.getDisplayName());
        }

        // go through the folders...
        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            depth++;
            PSTMessage email = (PSTMessage)folder.getNextChild();
            while (email != null) {
                printDepth();
                System.out.println("Email: "+ email.getDescriptorNodeId() + " - " + email.getSubject());
                email = (PSTMessage)folder.getNextChild();
            }
            depth--;
        }
        depth--;
    }

    public void printDepth() {
        for (int x = 0; x < depth-1; x++) {
            System.out.print(" | ");
        }
        System.out.print(" |- ");
    }
}
