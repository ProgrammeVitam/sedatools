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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;

/**
 * PSTAppointment is for Calendar items
 *
 * @author Richard Johnson
 */
public class PSTAppointment extends PSTMessage {

    PSTAppointment(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
            throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    PSTAppointment(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
                   final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    public boolean getSendAsICAL() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008200, PSTFile.PSETID_Appointment)));
    }

    public int getBusyStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008205, PSTFile.PSETID_Appointment));
    }

    public boolean getShowAsBusy() {
        return this.getBusyStatus() == 2;
    }

    public String getLocation() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008208, PSTFile.PSETID_Appointment));
    }

    public Date getStartTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820d, PSTFile.PSETID_Appointment));
    }

    public LocalDateTime getLocalStartTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820d, PSTFile.PSETID_Appointment))
                .toInstant().atZone(getStartTimeZone().getSimpleTimeZone().toZoneId()).toLocalDateTime();
    }

    public PSTTimeZone getStartTimeZone() {
        return this.getTimeZoneItem(this.pstFile.getNameToIdMapItem(0x0000825e, PSTFile.PSETID_Appointment));
    }

    public Date getEndTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820e, PSTFile.PSETID_Appointment));
    }

    public LocalDateTime getLocalEndTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820e, PSTFile.PSETID_Appointment))
                .toInstant().atZone(getStartTimeZone().getSimpleTimeZone().toZoneId()).toLocalDateTime();
    }

    public PSTTimeZone getEndTimeZone() {
        return this.getTimeZoneItem(this.pstFile.getNameToIdMapItem(0x0000825f, PSTFile.PSETID_Appointment));
    }

    public PSTTimeZone getRecurrenceTimeZone() {
        String desc = this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008234, PSTFile.PSETID_Appointment));
        final byte[] tzData = this
                .getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008233, PSTFile.PSETID_Appointment));
        if ((desc == null) || desc.isEmpty())
            desc = "Unknown";
        if (tzData != null && tzData.length != 0) {
            return new PSTTimeZone(desc, tzData);
        }
        return null;
    }

    public int getDuration() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008213, PSTFile.PSETID_Appointment));
    }

    public int getColor() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008214, PSTFile.PSETID_Appointment));
    }

    public boolean getSubType() {
        return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008215, PSTFile.PSETID_Appointment)) != 0);
    }

    public int getMeetingStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008217, PSTFile.PSETID_Appointment));
    }

    public int getResponseStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008218, PSTFile.PSETID_Appointment));
    }

    public boolean isRecurring() {
        return this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008223, PSTFile.PSETID_Appointment));
    }

    public Date getRecurrenceBase() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008228, PSTFile.PSETID_Appointment));
    }

    public int getRecurrenceType() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008231, PSTFile.PSETID_Appointment));
    }

    public String getRecurrencePattern() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008232, PSTFile.PSETID_Appointment));
    }

    public byte[] getRecurrenceStructure() {
        return this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008216, PSTFile.PSETID_Appointment));
    }

    public byte[] getTimezone() {
        return this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008233, PSTFile.PSETID_Appointment));
    }

    public String getAllAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008238, PSTFile.PSETID_Appointment));
    }

    public String getToAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x0000823b, PSTFile.PSETID_Appointment));
    }

    public String getCCAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x0000823c, PSTFile.PSETID_Appointment));
    }

    public int getAppointmentSequence() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008201, PSTFile.PSETID_Appointment));
    }

    // online meeting properties
    public boolean isOnlineMeeting() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008240, PSTFile.PSETID_Appointment)));
    }

    public int getNetMeetingType() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008241, PSTFile.PSETID_Appointment));
    }

    public String getNetMeetingServer() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008242, PSTFile.PSETID_Appointment));
    }

    public String getNetMeetingOrganizerAlias() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008243, PSTFile.PSETID_Appointment));
    }

    public boolean getNetMeetingAutostart() {
        return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008245, PSTFile.PSETID_Appointment)) != 0);
    }

    public boolean getConferenceServerAllowExternal() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008246, PSTFile.PSETID_Appointment)));
    }

    public String getNetMeetingDocumentPathName() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008247, PSTFile.PSETID_Appointment));
    }

    public String getNetShowURL() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008248, PSTFile.PSETID_Appointment));
    }

    public Date getAttendeeCriticalChange() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00000001, PSTFile.PSETID_Meeting));
    }

    public Date getOwnerCriticalChange() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000001a, PSTFile.PSETID_Meeting));
    }

    public String getConferenceServerPassword() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008249, PSTFile.PSETID_Appointment));
    }

    public boolean getAppointmentCounterProposal() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008257, PSTFile.PSETID_Appointment)));
    }

    public boolean isSilent() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00000004, PSTFile.PSETID_Meeting)));
    }

    public String getRequiredAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00000006, PSTFile.PSETID_Meeting));
    }

    public int getLocaleId() {
        return this.getIntItem(0x3ff1);
    }

    public PSTGlobalObjectId getGlobalObjectId() {

        byte[] tmp = this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00000003, PSTFile.PSETID_Meeting));
        if (tmp != null)
            return new PSTGlobalObjectId(tmp);
        else
            return null;
    }

    public PSTGlobalObjectId getCleanGlobalObjectId() {
        byte[] tmp = this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00000023, PSTFile.PSETID_Meeting));
        if (tmp != null)
            return new PSTGlobalObjectId(tmp);
        else
            return null;
    }

    /**
     * Appt time to utc date.
     *
     * @param minutes the minute since 1/1/1601 in local time
     * @param tz      the timezone
     * @return the date in UTC time
     */
    public static Date apptTimeToUTCDate(final int minutes, final PSTTimeZone tz) {
        // Must convert minutes since 1/1/1601 in local time to UTC
        final long ms_since_16010101 = minutes * (60 * 1000L);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        final long tzOffset;
        if (tz == null)
            tzOffset = 0;
        else
            tzOffset = tz.getSimpleTimeZone().getOffset(ms_since_19700101);

        Date utcDate = new Date(ms_since_19700101 - tzOffset);
        return utcDate;
    }

    @Override
    public String toString() {
        String result = "IDs:\n  GlobalObjectID: " + getGlobalObjectId() + "\n  CleanGlobalID: " + getCleanGlobalObjectId() + "\n" +
                "  LocaleID: " + getLocaleId() + "\n";
        result += "Info:\n  Subject: " + getSubject() + "\n  Location: " + getLocation() + "\n";
        result += "Peoples:\n  Required attendees: " + getRequiredAttendees() + "\n  All attendees: " + getAllAttendees() +
                "\n  To attendees: " + getToAttendees() + "\n  CC attendees: " + getCCAttendees() + "\n";
        result += "Flags:\n  Busy:" + getShowAsBusy() + "\n  MeetingStatus: " + getMeetingStatus() + "\n  ResponseStatus: " + getResponseStatus() + "\n  " +
                "isRecursed: " + isRecurring() + "\n";
        result += "Time:\n  Start: " + getStartTime() + " [TZ=" + (getStartTimeZone()==null?"Unknown":getStartTimeZone().getSimpleTimeZone()) + "]\n  End: " + getEndTime() + " [TZ=" + (getEndTimeZone()==null?"Unknown":getEndTimeZone().getSimpleTimeZone()) + "]\n  " +
                "Duration: " + getDuration() + "\n";
        result += "Recurrence:\n  Base: " + getRecurrenceBase() + "\n  Type: " + getRecurrenceType() + "\n  Pattern: " + getRecurrencePattern() + "\n";
        if (isRecurring()) {
            PSTAppointmentRecurrence par = new PSTAppointmentRecurrence(getRecurrenceStructure(), this, getRecurrenceTimeZone());
            result += par.toString() + "\n";
        }
        result += "Others\n  Color: " + getColor() + "\n";
        return result;
    }
}
