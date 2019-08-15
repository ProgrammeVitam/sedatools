/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst;

import com.pff.*;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAppointment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.formattools.HTMLTextExtractor;
import fr.gouv.vitam.tools.mailextractlib.formattools.rtf.HTMLFromRTFExtractor;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreElement;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessageAttachment;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.*;

/**
 * StoreMessage sub-class for mail boxes extracted through libpst library.
 */
public class PstStoreAppointment extends StoreAppointment implements MicrosoftStoreElement {

    /**
     * The Constant EMBEDDED_MESSAGE.
     */
    static final String EMBEDDED_MESSAGE = "pst.embeddedmsg";

    /**
     * The containing PST folder
     **/
    private PstStoreFolder pstStoreFolder;

    /**
     * The contact.
     */
    private PSTAppointment appointment;

    /**
     * Instantiates a new pst store appointment.
     *
     * @param pstStoreFolder the store folder containing the contact
     * @param appointment    the appointment
     */
    protected PstStoreAppointment(PstStoreFolder pstStoreFolder, PSTAppointment appointment) {
        super(pstStoreFolder);
        this.appointment = appointment;
    }

    /**
     * Instantiates a new local non recurrent appointment, with forced values. Used for exceptions creations in recurrence analysis.
     *
     * @param storeFolder Mail box folder containing this appointment
     */
    protected PstStoreAppointment(StoreFolder storeFolder, String uniqId,
                                  int sequenceNumber,
                                  String subject,
                                  String location,
                                  String from,
                                  String toAttendees,
                                  String ccAttendees,
                                  ZonedDateTime startTime,
                                  ZonedDateTime endTime,
                                  String miscNotes,
                                  String otherMiscNotes,
                                  int messageStatus,
                                  List<StoreAttachment> attachments,
                                  boolean isRecurrenceDeletion,
                                  ZonedDateTime exceptionDate) {
        super(storeFolder);
        this.uniqId = uniqId;
        this.sequenceNumber = sequenceNumber;
        this.subject = subject;
        this.location = location;
        this.from = from;
        this.toAttendees = toAttendees;
        this.ccAttendees = ccAttendees;
        this.startTime = startTime;
        this.endTime = endTime;
        this.miscNotes = miscNotes;
        this.otherMiscNotes = otherMiscNotes;
        this.attachments = attachments;
        this.messageStatus = messageStatus;

        this.recurencePattern = "";
        this.startRecurrenceTime = null;
        this.endRecurrenceTime = null;
        this.exceptions = null;
        this.isRecurrenceDeletion = isRecurrenceDeletion;
        this.exceptionDate = exceptionDate;
    }


    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getEmbeddedMessageScheme()
     */
    @Override
    public String getEmbeddedMessageScheme() {
        return EMBEDDED_MESSAGE;
    }

    private String getUniqId() {
        String result = appointment.getCleanGlobalObjectId().toString();
        if (result == null || result.isEmpty())
            result = appointment.getGlobalObjectId().toString();
        return result;
    }

    static private String getFrom(PSTAppointment appointment) {
        String fromAddr = appointment.getSenderEmailAddress();
        String fromName = appointment.getSenderName();
        String result = "";
        if ((fromName != null) && !fromName.isEmpty())
            result = fromName + " ";
        if ((fromAddr != null) && !fromAddr.isEmpty())
            result += "<" + fromAddr + ">";
        return result.trim();
    }

    static HashMap<String,ZoneId> normalizedZoneIdMap=new HashMap<String,ZoneId>();
    private ZoneId getNormalizedZoneId(Date date,PSTTimeZone pstTimeZone) throws InterruptedException {
        ZoneId result=normalizedZoneIdMap.get(pstTimeZone.getName());
        if (result!=null)
            return result;
        try {
            result=ZoneId.of(pstTimeZone.getName());
        }
        catch (DateTimeException e) {
            result = ZoneOffset.ofTotalSeconds(pstTimeZone.getSimpleTimeZone().getOffset(date.getTime())/1000);
            logMessageWarning("mailextractlib: can't determine time zone id ["+pstTimeZone.getName()+
                    "], replace by "+result.toString()+" format", null);
        }
        normalizedZoneIdMap.put(pstTimeZone.getName(), result);
        return result;
    }

    private ZonedDateTime getZonedDateTime(Date date, PSTTimeZone pstTimeZone) throws InterruptedException {
        ZoneId zoneId;
        if (pstTimeZone == null)
            zoneId = ZoneId.of("UTC");
        else
            zoneId = getNormalizedZoneId(date,pstTimeZone);
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

    private void analyzeMiscNotes() throws InterruptedException {
        String text = appointment.getBody().trim();
        String html = appointment.getBodyHTML().trim();
        String rtf = "";
        String test;
        try {
            rtf = appointment.getRTFBody().trim();

            if (!rtf.isEmpty()) {
                HTMLFromRTFExtractor htmlExtractor = new HTMLFromRTFExtractor(rtf);

                if (htmlExtractor.isEncapsulatedTEXTinRTF()) {
                    test = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if (text.isEmpty())
                        text=test;
                    else if (!text.equals(test))
                        logMessageWarning("mailextractlib: rtf version different from text version, rtf version dropped",null);
                    rtf = "";
                } else if (htmlExtractor.isEncapsulatedHTMLinRTF()) {
                    test = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if (html.isEmpty())
                        html=test;
                    else if (!html.equals(test))
                        logMessageWarning("mailextractlib: rtf version different from html version, rtf version dropped",null);
                    rtf = "";
                }
                else {
                    test=htmlExtractor.getDeEncapsulateHTMLFromRTF().trim();
                    if (test.isEmpty())
                        rtf="";
                }
            }
        } catch (PSTException | IOException | MailExtractLibException ignored) {
        }
        test=HTMLTextExtractor.getInstance().act(html).trim();
        if (test.isEmpty())
            html="";

        miscNotes = text;
        if (!html.isEmpty())
            otherMiscNotes = html;
        else
            otherMiscNotes = rtf;
    }

    private List<StoreAttachment> getAttachments(PSTMessage message) throws InterruptedException {
        ArrayList<MicrosoftStoreMessageAttachment> nativeAttachments = new ArrayList<MicrosoftStoreMessageAttachment>(10);
        for (int i = 0; i < message.getNumberOfAttachments(); i++) {
            try {
                final PSTMessage child = message.getAttachment(i).getEmbeddedPSTMessage();
                if (child instanceof PSTAppointment)
                    continue;
            } catch (PSTException | IOException ignored) {
            }
            nativeAttachments.add(new PstStoreMessageAttachment(message, i));
        }
        if (nativeAttachments.size()==0)
            return new ArrayList<StoreAttachment>(0);
        return MicrosoftStoreElement.getAttachments(this, nativeAttachments.toArray(new MicrosoftStoreMessageAttachment[0]));
    }

    private int getMessageStatus() {
        String messageClass=appointment.getMessageClass();
        if (messageClass.contains("Appointment"))
            return MESSAGE_STATUS_LOCAL;
        else if (messageClass.contains("Meeting.Request"))
            return MESSAGE_STATUS_REQUEST;
        else if (messageClass.contains("Meeting.Resp.Pos"))
            return MESSAGE_STATUS_RESPONSE_YES;
        else if (messageClass.contains("Meeting.Resp.Tent"))
            return MESSAGE_STATUS_RESPONSE_MAY;
        else if (messageClass.contains("Meeting.Resp.Neg"))
            return MESSAGE_STATUS_RESPONSE_NO;
        else
            return MESSAGE_STATUS_UNKNOWN;
    }

    private StoreAppointment generateDeletionAppointment(ZonedDateTime zdt) {
        PstStoreAppointment result= new PstStoreAppointment(storeFolder,
                uniqId, sequenceNumber, subject, location, from, toAttendees, ccAttendees,
                null, null, "", "",messageStatus, null,
                true, zdt);
        result.listLineId=getStoreExtractor().incGlobalListCounter(this.getClass());
        return result;
    }

    private String getChangedFrom(PSTAppointmentException exception) {
        if (exception.getEmbeddedMessage() == null)
            return this.from;
        String result = getFrom(exception.getEmbeddedMessage());
        return result;
    }

    private String getChangedToAttendees(PSTAppointmentException exception) {
        if (exception.getEmbeddedMessage() == null)
            return this.toAttendees;
        String result = exception.getEmbeddedMessage().getToAttendees();
        return result;
    }

    private String getChangedCCAttendees(PSTAppointmentException exception) {
        if (exception.getEmbeddedMessage() == null)
            return this.ccAttendees;
        String result = exception.getEmbeddedMessage().getCCAttendees();
        return result;
    }

    private ZonedDateTime getChangedZonedDateTime(ZonedDateTime zdt, Date d) {
        ZonedDateTime result = ZonedDateTime.ofInstant(d.toInstant(), zdt.getZone());
        return result;
    }

    private StoreAppointment generateExceptionAppointment(ZonedDateTime zdt, PSTAppointmentException exception) throws InterruptedException {
        PstStoreAppointment result = new PstStoreAppointment(storeFolder,
                uniqId, sequenceNumber,
                exception.getSubject(),
                exception.getLocation(),
                getChangedFrom(exception),
                getChangedToAttendees(exception),
                getChangedCCAttendees(exception),
                getChangedZonedDateTime(startTime, exception.getStartDate()),
                getChangedZonedDateTime(startTime, exception.getEndDate()),
                "", "",
                messageStatus,
                null,
                false,
                zdt);
        // special treatment when embedded message
        if (exception.getEmbeddedMessage() != null) {
            result.appointment = exception.getEmbeddedMessage();
            result.analyzeMiscNotes();
            result.attachments=getAttachments(result.appointment);
        }
        result.listLineId=getStoreExtractor().incGlobalListCounter(this.getClass());
        return result;
    }

    @Override
    public void analyzeAppointment() throws MailExtractLibException, InterruptedException {
        uniqId = getUniqId();

        subject = appointment.getSubject();
        location = appointment.getLocation();
        from = getFrom(appointment);
        toAttendees = appointment.getToAttendees();
        ccAttendees = appointment.getCCAttendees();
        startTime = getZonedDateTime(appointment.getStartTime(), appointment.getStartTimeZone());
        endTime = getZonedDateTime(appointment.getEndTime(), appointment.getEndTimeZone());
        analyzeMiscNotes();
        attachments = getAttachments(appointment);
        sequenceNumber = appointment.getAppointmentSequence();
        modificationTime=getZonedDateTime(appointment.getLastModificationTime(),null);
        messageStatus = getMessageStatus();
        if (appointment.isRecurring()) {
            PSTAppointmentRecurrence par = new PSTAppointmentRecurrence(appointment.getRecurrenceStructure(), appointment,
                    appointment.getRecurrenceTimeZone());
            recurencePattern = appointment.getRecurrencePattern();
            startRecurrenceTime = getZonedDateTime(par.getStartDate(), appointment.getRecurrenceTimeZone());
            endRecurrenceTime = getZonedDateTime(par.getEndDate(), appointment.getRecurrenceTimeZone());
            exceptions = new ArrayList<StoreAppointment>(10);
            for (Date d : par.getDeletedInstanceDates()) {
                ZonedDateTime zdt=getZonedDateTime(d, par.getTimeZone());
                exceptions.add(generateDeletionAppointment(zdt));
            }
            for (int i = 0; i < par.getExceptionCount(); i++) {
                ZonedDateTime zdt=getZonedDateTime(par.getModifiedInstanceDates()[i], par.getTimeZone());
                exceptions.add(generateExceptionAppointment(zdt, par.getException(i)));
            }
        } else {
            recurencePattern = "";
            startRecurrenceTime = null;
            endRecurrenceTime = null;
            exceptions = new ArrayList<StoreAppointment>(0);
        }
        isRecurrenceDeletion = false;
    }
}
