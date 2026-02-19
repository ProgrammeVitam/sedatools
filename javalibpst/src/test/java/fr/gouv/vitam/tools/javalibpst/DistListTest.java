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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Tests for {@link PSTDistList}.
 *
 * @author Richard Johnson
 */
@RunWith(JUnit4.class)
public class DistListTest {

    /**
     * Test we can retrieve distribution lists from the PST.
     */
    @Test
    public final void testGetDistList() throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        PSTDistList obj = (PSTDistList) PSTObject.detectAndLoadPSTObject(pstFile, 2097188);
        Object[] members = obj.getDistributionListMembers();
        Assert.assertEquals("Correct number of members", members.length, 3);
        int numberOfContacts = 0;
        int numberOfOneOffRecords = 0;
        HashSet<String> emailAddresses = new HashSet<String>();
        HashSet<String> displayNames = new HashSet<String>();
        for (Object member : members) {
            if (member instanceof PSTContact) {
                PSTContact contact = (PSTContact) member;
                Assert.assertEquals(
                    "Contact email address",
                    contact.getEmail1EmailAddress(),
                    "contact1@rjohnson.id.au"
                );
                numberOfContacts++;
            } else {
                PSTDistList.OneOffEntry entry = (PSTDistList.OneOffEntry) member;
                emailAddresses.add(entry.getEmailAddress());
                displayNames.add(entry.getDisplayName());
                numberOfOneOffRecords++;
            }
        }
        Assert.assertEquals("Correct number of members", members.length, 3);
        Assert.assertEquals(
            "Contains all display names",
            displayNames,
            new HashSet<String>(Arrays.asList(new String[] { "dist name 2", "dist name 1" }))
        );
        Assert.assertEquals(
            "Contains all email addresses",
            emailAddresses,
            new HashSet<String>(Arrays.asList(new String[] { "dist1@rjohnson.id.au", "dist2@rjohnson.id.au" }))
        );
    }
}
