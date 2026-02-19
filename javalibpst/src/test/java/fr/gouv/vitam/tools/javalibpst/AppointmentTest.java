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
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Tests for {@link PSTAppointment}.
 *
 * @author Richard Johnson
 */
@RunWith(JUnit4.class)
public class AppointmentTest {

    /**
     * Test we can access appointments from the PST.
     */
    @Test
    public final void testGetDistList() throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        PSTAppointment appt = (PSTAppointment) PSTObject.detectAndLoadPSTObject(pstFile, 2097348);
        PSTAppointmentRecurrence r = new PSTAppointmentRecurrence(
            appt.getRecurrenceStructure(),
            appt,
            appt.getRecurrenceTimeZone()
        );

        Assert.assertEquals("Has 3 deleted items (1 removed, 2 changed)", 3, r.getDeletedInstanceDates().length);

        Assert.assertEquals("Number of Exceptions", 2, r.getExceptionCount());

        String d = r.getException(0).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the appointment at 9", d);

        LocalDateTime ldt = LocalDateTime.ofInstant(
            r.getException(0).getStartDate().toInstant(),
            ZoneId.of("US/Pacific")
        );
        Assert.assertEquals("First exception correct hour", 9, ldt.getHour());

        d = r.getException(1).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the one at 10", d);

        ldt = LocalDateTime.ofInstant(r.getException(1).getStartDate().toInstant(), ZoneId.of("US/Pacific"));
        Assert.assertEquals("Second exception correct hour", 10, ldt.getHour());
        //System.out.println(r.getExceptionCount());
        //System.out.println(r.getException(0).getDTStamp());

        //for (int x = 0; x < r.getDeletedInstanceDates().length; x++) {
        //    System.out.println(r.getDeletedInstanceDates()[x]);
        //}
    }
}
