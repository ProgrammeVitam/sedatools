package com.pff;

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
    public final void testGetDistList()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        PSTAppointment appt = (PSTAppointment) PSTObject.detectAndLoadPSTObject(pstFile, 2097348);
        PSTAppointmentRecurrence r = new PSTAppointmentRecurrence(
                appt.getRecurrenceStructure(), appt, appt.getRecurrenceTimeZone());


        Assert.assertEquals(
                "Has 3 deleted items (1 removed, 2 changed)",
                3,
                r.getDeletedInstanceDates().length);

        Assert.assertEquals(
                "Number of Exceptions",
                2,
                r.getExceptionCount());

        String d = r.getException(0).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the appointment at 9", d);

        LocalDateTime ldt = LocalDateTime.ofInstant(
                r.getException(0).getStartDate().toInstant(),
                ZoneId.of("US/Pacific"));
        Assert.assertEquals(
                "First exception correct hour",
                9,
                ldt.getHour());

        d = r.getException(1).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the one at 10", d);

        ldt = LocalDateTime.ofInstant(
                r.getException(1).getStartDate().toInstant(),
                ZoneId.of("US/Pacific"));
        Assert.assertEquals(
                "Second exception correct hour",
                10,
                ldt.getHour());

        //System.out.println(r.getExceptionCount());
        //System.out.println(r.getException(0).getDTStamp());

        //for (int x = 0; x < r.getDeletedInstanceDates().length; x++) {
        //    System.out.println(r.getDeletedInstanceDates()[x]);
        //}
    }
}

