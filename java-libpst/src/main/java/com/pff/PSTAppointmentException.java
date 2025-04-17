/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.pff;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static com.pff.PSTAppointment.apptTimeToUTCDate;

/**
 * Class containing information on exceptions to a recurring appointment
 * 
 * @author Orin Eman
 *
 * Improved using Microsoft document [MS-OXOCAL]: Appointment and Meeting Object Protocol
 * https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxocal/09861fde-c8e4-4028-9346-e7c214cfdba1
 * @author Jean-Severin Lair
 */
public class PSTAppointmentException {

    // Override flags
    static final short ARO_SUBJECT = 0x0001;
    static final short ARO_MEETINGTYPE = 0x0002;
    static final short ARO_REMINDERDELTA = 0x0004;
    static final short ARO_REMINDER = 0x0008;
    static final short ARO_LOCATION = 0x0010;
    static final short ARO_BUSYSTATUS = 0x0020;
    static final short ARO_ATTACHMENT = 0x0040;
    static final short ARO_SUBTYPE = 0x0080;
    static final short ARO_APPTCOLOR = 0x0100;

    private final short OverrideFlags;

    private String Subject;
    private int MeetingType;
    private int ReminderDelta;
    private boolean ReminderSet;
    private String Location;
    private int BusyStatus;
    private int Attachment;
    private boolean SubType;
    private int AppointmentColor;

    private final int writerVersion2;
    private Date StartDate;
    private Date EndDate;
    private Date OriginalStartDate;

    private int ChangeHighlightValue=-1;
    private PSTAppointment embeddedMessage = null;
    private final PSTAppointment appt;
    private final int length;
    private int extendedLength;

    // Access methods - return the value from the exception if
    // OverrideFlags say it's present, otherwise the value from the appointment.
    public String getSubject() {
        if ((this.OverrideFlags & 0x0001) != 0) {
            return Subject;
        }

        return this.appt.getSubject();
    }

    public int getMeetingType() {
        if ((this.OverrideFlags & 0x0002) != 0) {
            return this.MeetingType;
        }

        return this.appt.getMeetingStatus();
    }

    public int getReminderDelta() {
        if ((this.OverrideFlags & 0x0004) != 0) {
            return this.ReminderDelta;
        }

        return this.appt.getReminderDelta();
    }

    public boolean getReminderSet() {
        if ((this.OverrideFlags & 0x0008) != 0) {
            return this.ReminderSet;
        }

        return this.appt.getReminderSet();
    }

    public String getLocation() {
        if ((this.OverrideFlags & 0x0010) != 0) {
            return Location;
        }

        return this.appt.getLocation();
    }

    public int getBusyStatus() {
        if ((this.OverrideFlags & 0x0020) != 0) {
            return this.BusyStatus;
        }

        return this.appt.getBusyStatus();
    }

    public boolean isAttachmentsPresent() {
        if ((this.OverrideFlags & 0x0040) != 0 && this.Attachment == 0x00000001) {
            return true;
        }

        return false;
    }

    public boolean getSubType() {
        if ((this.OverrideFlags & 0x0080) != 0) {
            return this.SubType;
        }

        return this.appt.getSubType();
    }

    public int getColor() {
        if ((this.OverrideFlags & ARO_APPTCOLOR) != 0) {
            return this.AppointmentColor;
        }

        return this.appt.getColor();
    }

    public int getChangeHighlightValue() {
        return ChangeHighlightValue;
    }

    public String getDescription() {
        if (this.embeddedMessage != null) {
            return this.embeddedMessage.getBodyPrefix();
        }

        return null;
    }

    public Date getDTStamp() {
        Date ret = null;
        if (this.embeddedMessage != null) {
            ret = this.embeddedMessage.getOwnerCriticalChange();
        }

        if (ret == null) {
            // Use current date/time
            final Calendar c = Calendar.getInstance(PSTTimeZone.utcTimeZone);
            ret = c.getTime();
        }

        return ret;
    }

    public Date getStartDate() {
        return this.StartDate;
    }

    public Date getEndDate() {
        return this.EndDate;
    }

    public Date getOriginalStartDate() {
        return this.OriginalStartDate;
    }

    public boolean isEmbeddedMessagePresent() {
        return this.embeddedMessage != null;
    }

    //
    // Allow access to an embedded message for
    // properties that don't have access methods here.
    //
    public PSTAppointment getEmbeddedMessage() {
        return this.embeddedMessage;
    }

    PSTAppointmentException(final byte[] recurrencePattern, int offset, final int writerVersion2,
        final PSTAppointment appt) {
        this.writerVersion2 = writerVersion2;
        final int initialOffset = offset;
        this.appt = appt;
        this.embeddedMessage = null;

        PSTTimeZone tz=appt.getRecurrenceTimeZone();
        // time in minutes from 1/1/1601 but local, converted to UTC time zone reference of the original appointment start
        this.StartDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
        offset += 4;
        // time in minutes from 1/1/1601, converted to UTC time zone reference of the original appointment end
        this.EndDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
        offset += 4;
        // time in minutes from 1/1/1601 but local, converted to UTC time zone reference of the original appointment start
        this.OriginalStartDate=apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
        offset += 4;
        this.OverrideFlags = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 2);
        offset += 2;

        if ((this.OverrideFlags & ARO_SUBJECT) != 0) {
            offset += 2;
            final short SubjectLength2 = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            byte [] tmp = new byte[SubjectLength2];
            System.arraycopy(recurrencePattern, offset, tmp, 0, SubjectLength2);
            try {
                Subject=new String(tmp, "UTF-16LE");
            } catch (UnsupportedEncodingException ignored) {
            }
            offset += SubjectLength2;
        }

        if ((this.OverrideFlags & ARO_MEETINGTYPE) != 0) {
            this.MeetingType = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_REMINDERDELTA) != 0) {
            this.ReminderDelta = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_REMINDER) != 0) {
            this.ReminderSet = ((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4) != 0);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_LOCATION) != 0) {
            offset += 2;
            final short LocationLength2 = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            byte [] tmp = new byte[LocationLength2];
            System.arraycopy(recurrencePattern, offset, tmp, 0, LocationLength2);
            try {
                Location=new String(tmp, "UTF-16LE");
            } catch (UnsupportedEncodingException ignored) {
            }
            offset += LocationLength2;
        }

        if ((this.OverrideFlags & ARO_BUSYSTATUS) != 0) {
            this.BusyStatus = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_ATTACHMENT) != 0) {
            this.Attachment = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_SUBTYPE) != 0) {
            this.SubType = ((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4) != 0);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_APPTCOLOR) != 0) {
            this.AppointmentColor = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        this.length = offset - initialOffset;
    }

    void ExtendedException(final byte[] recurrencePattern, int offset) {
        final int initialOffset = offset;

        if (this.writerVersion2 >= 0x00003009) {
            final int ChangeHighlightSize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += 4;
            this.ChangeHighlightValue = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += ChangeHighlightSize;
        }

        int ReservedBlockEESize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4 + ReservedBlockEESize;

        // See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
        if ((this.OverrideFlags & (ARO_SUBJECT | ARO_LOCATION)) != 0) {
            long tzoffset;
            PSTTimeZone tz=appt.getRecurrenceTimeZone();
            // time in minutes from 1/1/1601 but local, converted to same time zone reference than the original appointment start
            this.StartDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
            offset += 4;
            // time in minutes from 1/1/1601, converted to same time zone reference than the original appointment end
            this.EndDate = apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
            offset += 4;
            // time in minutes from 1/1/1601 but local, converted to same time zone reference than the original appointment start
            this.OriginalStartDate=apptTimeToUTCDate((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),tz);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_SUBJECT) != 0) {
            int WideCharSubjectCharLength = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            byte [] WideCharSubject = new byte[WideCharSubjectCharLength * 2];
            System.arraycopy(recurrencePattern, offset, WideCharSubject, 0, WideCharSubject.length);
            try {
                Subject=new String(WideCharSubject, "UTF-16LE");
            } catch (UnsupportedEncodingException ignored) {
            }
            offset += WideCharSubject.length;
        }

        if ((this.OverrideFlags & ARO_LOCATION) != 0) {
            int WideCharLocationCharLength = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            byte [] WideCharLocation = new byte[WideCharLocationCharLength * 2];
            System.arraycopy(recurrencePattern, offset, WideCharLocation, 0, WideCharLocation.length);
            try {
                Location=new String(WideCharLocation, "UTF-16LE");
            } catch (UnsupportedEncodingException ignored) {
            }
            offset += WideCharLocation.length;
        }

        // See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
        if ((this.OverrideFlags & (ARO_SUBJECT | ARO_LOCATION)) != 0) {
            ReservedBlockEESize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4 + ReservedBlockEESize;
        }

        this.extendedLength = offset - initialOffset;
    }

    void setEmbeddedMessage(final PSTAppointment embeddedMessage) {
        this.embeddedMessage = embeddedMessage;
    }

    // Length of this ExceptionInfo structure in the PST file
    int getLength() {
        return this.length;
    }

    // Length of this ExtendedException structure in the PST file
    int getExtendedLength() {
        return this.extendedLength;
    }

    static DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public String toString(){
        String result="      OverrideFlags:"+Integer.toHexString(OverrideFlags)+"\n";

        if ((OverrideFlags & ARO_SUBJECT)!=0)
            result+="      Changed Subject: "+getSubject()+"\n";
        if ((OverrideFlags & ARO_MEETINGTYPE)!=0)
            result+="      Changed MeetingType: "+getMeetingType()+"\n";
        if ((OverrideFlags & ARO_REMINDER)!=0)
            result+="      Changed Reminder: "+getReminderSet()+"\n";
        if ((OverrideFlags & ARO_REMINDERDELTA)!=0)
            result+="      Changed ReminderDelta: "+getReminderDelta()+"\n";
        if ((OverrideFlags & ARO_LOCATION)!=0)
            result+="      Changed Location: "+getLocation()+"\n";
        if ((OverrideFlags & ARO_BUSYSTATUS)!=0)
            result+="      Changed BusyStatus: "+getBusyStatus()+"\n";
        if ((OverrideFlags & ARO_SUBTYPE)!=0)
            result+="      Changed SubType: "+getSubType()+"\n";
        if ((OverrideFlags & ARO_APPTCOLOR)!=0)
            result+="      Changed ApptColor: "+getColor()+"\n";
        result+="      StartDate: "+getStartDate()+"\n";
        result+="      EndDate: "+getEndDate()+"\n";
        result+="      StartOriginalDate: "+getOriginalStartDate()+"\n";
        if (isEmbeddedMessagePresent())
            result+="      ChangedAppointment:\n        "+getEmbeddedMessage().toString().replaceAll("\\n","\n        ");
        return result;
    }
}
