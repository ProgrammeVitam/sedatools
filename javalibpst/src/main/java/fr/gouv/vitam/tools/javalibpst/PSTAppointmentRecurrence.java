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

/*
 * import java.text.SimpleDateFormat;
 * /
 **/

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;

import static fr.gouv.vitam.tools.javalibpst.PSTAppointment.apptTimeToUTCDate;

/**
 * Class containing recurrence information for a recurring appointment
 *
 * @author Orin Eman
 * <p>
 * Improved using Microsoft document [MS-OXOCAL]: Appointment and Meeting Object Protocol
 * https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxocal/09861fde-c8e4-4028-9346-e7c214cfdba1
 * @author Jean-Severin Lair
 */

public class PSTAppointmentRecurrence {

    private final Date StartDate;
    private final Date EndDate;
    private final short RecurFrequency;
    private final short PatternType;
    private final short CalendarType;
    private final Date FirstDateTime;
    private final int Period;
    private final int SlidingFlag;
    private int PatternSpecific;
    private int PatternSpecificNth;
    private final int EndType;
    private final int OccurrenceCount;
    private final int FirstDOW;
    private final int DeletedInstanceCount;
    private Date[] DeletedInstanceDates = null;
    private final int ModifiedInstanceCount;
    private Date[] ModifiedInstanceDates = null;
    private final int writerVersion2;
    private final int StartTimeOffset;
    private final int EndTimeOffset;
    private final short ExceptionCount;
    private PSTAppointmentException[] Exceptions = null;
    private PSTTimeZone RecurrenceTimeZone = null;

    // Access methods

    public short getExceptionCount() {
        return this.ExceptionCount;
    }

    public PSTAppointmentException getException(final int i) {
        if (i < 0 || i >= this.ExceptionCount) {
            return null;
        }
        return this.Exceptions[i];
    }

    public Date[] getDeletedInstanceDates() {
        return this.DeletedInstanceDates;
    }

    public Date[] getModifiedInstanceDates() {
        return this.ModifiedInstanceDates;
    }

    public short getCalendarType() {
        return this.CalendarType;
    }

    public short getPatternType() {
        return this.PatternType;
    }

    public int getPeriod() {
        return this.Period;
    }

    public int getPatternSpecific() {
        return this.PatternSpecific;
    }

    public int getFirstDOW() {
        return this.FirstDOW;
    }

    public int getPatternSpecificNth() {
        return this.PatternSpecificNth;
    }

    public Date getFirstDateTime() {
        return this.FirstDateTime;
    }

    public int getEndType() {
        return this.EndType;
    }

    public int getOccurrenceCount() {
        return this.OccurrenceCount;
    }

    public Date getEndDate() {
        return this.EndDate;
    }

    public int getStartTimeOffset() {
        return this.StartTimeOffset;
    }

    public PSTTimeZone getTimeZone() {
        return this.RecurrenceTimeZone;
    }

    public int getRecurFrequency() {
        return this.RecurFrequency;
    }

    public int getSlidingFlag() {
        return this.SlidingFlag;
    }

    public Date getStartDate() {
        return this.StartDate;
    }

    public int getEndTimeOffset() {
        return this.EndTimeOffset;
    }

    public PSTAppointmentRecurrence(final byte[] recurrencePattern, final PSTAppointment appt, final PSTTimeZone tz) {
        this.RecurrenceTimeZone = tz;
        final SimpleTimeZone stz;
        if (tz==null)
            stz=PSTTimeZone.utcTimeZone;
        else
            stz=this.RecurrenceTimeZone.getSimpleTimeZone();

        // Read the structure
        this.RecurFrequency = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 4, 6);
        this.PatternType = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 6, 8);
        this.CalendarType = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 8, 10);
        this.FirstDateTime = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 10, 14),
                this.RecurrenceTimeZone);
        this.Period = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 14, 18);
        this.SlidingFlag = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 18, 22);
        int offset = 22;
        if (this.PatternType != 0) {
            this.PatternSpecific = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                    offset + 4);
            offset += 4;
            if (this.PatternType == 0x0003 || this.PatternType == 0x000B) {
                this.PatternSpecificNth = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                        offset + 4);
                offset += 4;
            }
        }
        this.EndType = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.OccurrenceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.FirstDOW = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;

        this.DeletedInstanceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
        offset += 4;
        this.DeletedInstanceDates = new Date[this.DeletedInstanceCount];
        for (int i = 0; i < this.DeletedInstanceCount; ++i) {
            this.DeletedInstanceDates[i] = apptTimeToUTCDate(
                    (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                    this.RecurrenceTimeZone);
            offset += 4;
        }

        this.ModifiedInstanceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
        offset += 4;
        this.ModifiedInstanceDates = new Date[this.ModifiedInstanceCount];
        for (int i = 0; i < this.ModifiedInstanceCount; ++i) {
            this.ModifiedInstanceDates[i] = apptTimeToUTCDate(
                    (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                    this.RecurrenceTimeZone);
            offset += 4;
        }

        this.StartDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                this.RecurrenceTimeZone);
        offset += 4;
        this.EndDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                this.RecurrenceTimeZone);
        offset += 4 + 4; // Skip ReaderVersion2

        this.writerVersion2 = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;

        this.StartTimeOffset = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.EndTimeOffset = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.ExceptionCount = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 2);
        offset += 2;

        // Read exceptions
        this.Exceptions = new PSTAppointmentException[this.ExceptionCount];
        for (int i = 0; i < this.ExceptionCount; ++i) {
            this.Exceptions[i] = new PSTAppointmentException(recurrencePattern, offset, this.writerVersion2, appt);
            offset += this.Exceptions[i].getLength();
        }

        if ((offset + 4) <= recurrencePattern.length) {
            final int ReservedBlock1Size = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                    offset + 4);
            offset += 4 + (ReservedBlock1Size * 4);
        }

        // Read extended exception info
        for (int i = 0; i < this.ExceptionCount; ++i) {
            this.Exceptions[i].ExtendedException(recurrencePattern, offset);
            offset += this.Exceptions[i].getExtendedLength();
        }

        // Ignore any extra data - see
        // http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx

        PSTAppointment embeddedMessage = null;
        HashMap<String, PSTAppointmentException> modifiedDateMap = new HashMap<String, PSTAppointmentException>();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        f.setTimeZone(stz);
        final Calendar c = Calendar.getInstance(stz);

        for (int i = 0; i < this.ExceptionCount; ++i)
            modifiedDateMap.put(f.format(ModifiedInstanceDates[i].getTime()), Exceptions[i]);

        for (int i = 0; i < appt.getNumberOfAttachments(); i++) {
            try {
                final PSTMessage message = appt.getAttachment(i).getEmbeddedPSTMessage();
                if (!(message instanceof PSTAppointment)) {
                    continue;
                }
                embeddedMessage = (PSTAppointment) message;
                c.setTime(embeddedMessage.getRecurrenceBase());
                PSTAppointmentException modifiedException = modifiedDateMap.get(f.format(c.getTime()));
                if (modifiedException == null)
                    continue;
                modifiedException.setEmbeddedMessage(embeddedMessage);

            } catch (Exception ignored) {
            }
        }
    }


    @Override
    public String toString() {
        String result;
        result = "  Start date:" + getStartDate();
        result += "\n  End date:" + getEndDate();
        result += "\n  Changes:";
        result += "\n    " + getExceptionCount() + " exceptions" ;
        result += "\n    " + getDeletedInstanceDates().length + " delete";
        for (Date d : getDeletedInstanceDates())
            result += "\n      " + d.toString();
        result += "\n    " + getModifiedInstanceDates().length + " modified";
        int excount = 0;
        for (Date d : getModifiedInstanceDates()) {
            result += "\n      " + d.toString();
            result += "\n"+ getException(excount++);
        }
        result+="\n";
        return result;
    }
}
