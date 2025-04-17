/**
 * Copyright 2010 Richard Johnson & Orin Eman
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * ---
 * <p>
 * This file is part of java-libpst.
 * <p>
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pff;

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
