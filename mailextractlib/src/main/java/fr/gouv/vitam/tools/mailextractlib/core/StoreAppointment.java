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
package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Formatter;
import java.util.List;

import static fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor.ISO_8601;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;

/**
 * Abstract class representing a store element that is an appointment.
 * <p>
 * It defines all information (descriptive metadata and objects) to be collected from
 * an appointment, as well as the general concepts for appointment management. Each subclass
 * must be capable of extracting these generic pieces of information from a native appointment format.
 * <p>
 * Metadata to be collected according to Vitam guidelines for appointment extraction:
 * <ul>
 * <li>Appointment title (Title metadata),</li>
 * <li>Location where the appointment takes place (Location metadata),</li>
 * <li>Organizer/sender address (Organizer metadata),</li>
 * <li>List of required attendees (ToAttendees metadata),</li>
 * <li>List of optional attendees (CcAttendees metadata),</li>
 * <li>Start date and time (StartDate metadata),</li>
 * <li>End date and time (EndDate metadata),</li>
 * <li>General notes on the appointment for metadata enrichment,</li>
 * <li>Last modification time (ModificationTime metadata),</li>
 * <li>Attachments associated with the appointment,</li>
 * <li>Status of the appointment invitation (e.g., request, response accepted/declined, etc.),</li>
 * <li>Recurrence information (patterns, exceptions, deleted occurrences).</li>
 * </ul>
 * <p>
 * Additional extracted content:
 * <ul>
 * <li>List of exceptions for recurring appointments,</li>
 * <li>Attachments associated with the appointment,</li>
 * <li>Specific data according to the native format.</li>
 * </ul>
 * <p>
 * All values may be null if they are not defined for this appointment.
 * <p>
 * Subclasses can further extend this abstract class to provide specific methods for a particular
 * format or protocol (e.g., importing from PST files, iCal, etc.).
 **/
public abstract class StoreAppointment extends StoreElement {

    /**
     * Constants representing the status of an appointment's message.
     */
    public static final int MESSAGE_STATUS_UNKNOWN = 0;
    public static final int MESSAGE_STATUS_LOCAL = 1;
    public static final int MESSAGE_STATUS_REQUEST = 2;
    public static final int MESSAGE_STATUS_RESPONSE_YES = 3;
    public static final int MESSAGE_STATUS_RESPONSE_MAY = 4;
    public static final int MESSAGE_STATUS_RESPONSE_NO = 5;

    /**
     * Human-readable text for the statuses defined in {@link #MESSAGE_STATUS_UNKNOWN},
     * {@link #MESSAGE_STATUS_LOCAL}, etc.
     */
    public static final String[] MESSAGE_STATUS_TEXT = {"Unknown", "Local", "Request", "Resp.Yes", "Resp.May", "Resp.No"};

    /**
     * A unique identifier for the appointment (used to differentiate it from other elements).
     */
    protected String uniqId;

    /**
     * The title or subject of the appointment.
     */
    protected String subject;

    /**
     * The physical or virtual location where the appointment takes place.
     */
    protected String location;

    /**
     * The organizer of the appointment (usually a name or email address).
     */
    protected String from;

    /**
     * Comma-separated list of required attendees for the appointment.
     */
    protected String toAttendees;

    /**
     * Comma-separated list of optional attendees for the appointment.
     */
    protected String ccAttendees;

    /**
     * The date and time when the appointment starts.
     */
    protected ZonedDateTime startTime;

    /**
     * The date and time when the appointment ends.
     */
    protected ZonedDateTime endTime;

    /**
     * General notes or description added to the appointment.
     */
    protected String miscNotes;

    /**
     * Additional extended notes added to the appointment, if applicable.
     */
    protected String otherMiscNotes;

    /**
     * The last recorded modification time of the appointment.
     */
    protected ZonedDateTime modificationTime;

    /**
     * Attachments associated with the appointment.
     */
    protected List<StoreAttachment> attachments;

    /**
     * The sequence number used for appointments organized with other people.
     * This may indicate versioning or scheduling iterations.
     */
    protected int sequenceNumber;

    /**
     * Indicates the current status of the appointment's message
     * (e.g., local, request, response - see {@link #MESSAGE_STATUS_TEXT} for statuses).
     */
    protected int messageStatus;

    /**
     * Recurrence pattern for recurring appointments (e.g., weekly, yearly).
     */
    protected String recurencePattern;

    /**
     * The date and time when the recurrence starts.
     */
    protected ZonedDateTime startRecurrenceTime;

    /**
     * The date and time when the recurrence ends.
     */
    protected ZonedDateTime endRecurrenceTime;

    /**
     * List of exceptions (modifications or cancellations) in the recurring appointment.
     */
    protected List<StoreAppointment> exceptions;

    /**
     * Indicates whether this is a deleted occurrence in a recurring appointment.
     */
    protected boolean isRecurrenceDeletion;

    /**
     * The specific date of an exception in a recurring appointment series.
     * <p>
     * This represents the date when a particular occurrence in a recurring pattern
     * has been altered or canceled, ensuring proper tracking of changes in the series.
     */
    protected ZonedDateTime exceptionDate;

    /**
     * Instantiates a new appointment.
     *
     * @param storeFolder Mail box folder containing this appointment
     */
    protected StoreAppointment(StoreFolder storeFolder) {
        super(storeFolder);
    }

    @Override
    public String getLogDescription() {
        String result = "appointment " + listLineId;
        if (subject != null)
            result += " [" + subject + "/";
        else
            result += " [no subject/";
        result += getDateInDefinedTimeZone(startTime) + " - " + getDateInDefinedTimeZone(endTime) + "]";
        return result;
    }

    /**
     * Analyze contact to collect appointment information (protocol
     * specific).
     * <p>
     * This is the method for sub classes, where all appointment
     * information has to be extracted in standard representation out of the
     * inner native representation.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public abstract void analyzeAppointment() throws MailExtractLibException, InterruptedException;

    /**
     * Gets element name used for the csv file name construction.
     *
     * @return the element name
     */
    public static String getElementName() {
        return "appointments";
    }

    /**
     * Print the header for contacts list csv file
     *
     * @param ps the dedicated print stream
     */
    public static void printGlobalListCSVHeader(PrintStream ps) {
        synchronized (ps) {
            ps.println("ID;Subject;Location;From;ToAttendees;CcAttendees;StartTime;EndTime;" +
                    "MiscNotes;uniqID;SequenceNumber;ModificationTime;Folder;MessageStatus;" +
                    "isRecurrent;RecurrencePattern;StartRecurrenceTime;EndRecurrenceTime;" +
                    "ExceptionFromId;ExceptionDate;isDeletion;hasAttachment");
        }
    }

    /**
     * Extract to the appointments list and all associated attachements and exceptions
     *
     * @param writeFlag the write flag
     * @param father    the father
     * @throws InterruptedException    the interrupted exception
     * @throws MailExtractLibException the mail extract lib exception
     */
    public void extractAppointment(boolean writeFlag, StoreAppointment father) throws InterruptedException, MailExtractLibException {
        if (writeFlag) {
            if (storeFolder.getStoreExtractor().getOptions().extractElementsList)
                writeToAppointmentsList(father);
            if (storeFolder.getStoreExtractor().getOptions().extractElementsContent) {
                if ((attachments != null) && (!attachments.isEmpty())) {
                    ArchiveUnit attachmentNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.storeExtractor.destRootPath +
                            File.separator + storeFolder.storeExtractor.destName + File.separator + "appointments", "AppointmentAttachments#" + listLineId);
                    attachmentNode.addMetadata("DescriptionLevel", "RecordGrp", true);
                    attachmentNode.addMetadata("Title", "Appointment Attachments #" + listLineId, true);
                    attachmentNode.addMetadata("Description", "Appointment attachments extracted for " + subject + "[" + startTime + "-" + endTime + "]", true);
                    attachmentNode.addMetadata("StartDate", getDateInUTCTimeZone(startTime), true);
                    attachmentNode.addMetadata("EndDate", getDateInUTCTimeZone(endTime), true);
                    attachmentNode.write();
                    StoreAttachment.extractAttachments(attachments, attachmentNode, writeFlag);
                }
            }
            if (exceptions != null)
                for (StoreAppointment a : exceptions)
                    a.extractAppointment(writeFlag, this);
        }
    }

    private String getDateInUTCTimeZone(ZonedDateTime date) {
        String result;
        if (date != null)
            result = date.withZoneSameInstant(ZoneId.of("UTC")).format(ISO_8601);
        else
            result = "";
        return result;
    }

    private String getDateInDefinedTimeZone(ZonedDateTime date) {
        String result;
        if (date != null)
            result = date.format(ISO_8601);
        else
            result = "";
        return result;
    }

    private String normalizeUniqId(String uniqId) {
        String result = uniqId;
        byte[] hash;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(MD5);
            hash = md.digest(uniqId.getBytes(StandardCharsets.US_ASCII));
            Formatter formatter = new Formatter();
            for (final byte b : hash) {
                formatter.format("%02x", b);
            }
            result = formatter.toString();
            formatter.close();
        } catch (NoSuchAlgorithmException ignored) {
            //ignore
        }
        return result;
    }

    private void writeToAppointmentsList(StoreAppointment father) {
        PrintStream ps = storeFolder.getStoreExtractor().getGlobalListPS(this.getClass());
        synchronized (ps) {
            ps.format("\"%d\";", listLineId);
            ps.format("\"%s\";", filterHyphenForCsv(subject));
            ps.format("\"%s\";", filterHyphenForCsv(location));
            ps.format("\"%s\";", filterHyphenForCsv(from));
            ps.format("\"%s\";", filterHyphenForCsv(toAttendees));
            ps.format("\"%s\";", filterHyphenForCsv(ccAttendees));
            ps.format("\"%s\";", getDateInDefinedTimeZone(startTime));
            ps.format("\"%s\";", getDateInDefinedTimeZone(endTime));
            ps.format("\"%s\";", filterHyphenForCsv(miscNotes));
            ps.format("\"%s\";", normalizeUniqId(uniqId));
            ps.format("\"%d\";", sequenceNumber);
            ps.format("\"%s\";", getDateInDefinedTimeZone(modificationTime));
            ps.format("\"%s\";", filterHyphenForCsv(storeFolder.getFullName()));
            ps.format("\"%s\";", MESSAGE_STATUS_TEXT[messageStatus]);
            ps.format("\"%s\";", ((recurencePattern != null) && (!recurencePattern.isEmpty()) ? "X" : ""));
            ps.format("\"%s\";", filterHyphenForCsv(recurencePattern));
            ps.format("\"%s\";", getDateInDefinedTimeZone(startRecurrenceTime));
            ps.format("\"%s\";", getDateInDefinedTimeZone(endRecurrenceTime));
            ps.format("\"%s\";", (father != null ? father.listLineId : ""));
            ps.format("\"%s\";", getDateInDefinedTimeZone(exceptionDate));
            ps.format("\"%s\";", (isRecurrenceDeletion ? "X" : ""));
            ps.format("\"%s\"", ((attachments != null) && (!attachments.isEmpty()) ? Integer.toString(attachments.size()) : ""));
            ps.println("");
            ps.flush();
        }
    }

    @Override
    public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        if (storeFolder.getStoreExtractor().getOptions().extractAppointments) {
            listLineId = storeFolder.getStoreExtractor().incElementCounter(this.getClass());
            analyzeAppointment();
            StoreAttachment.detectStoreAttachments(attachments);
            extractAppointment(writeFlag, null);
        }
    }

    @Override
    public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException {
        if (storeFolder.getStoreExtractor().getOptions().extractAppointments) {
            listLineId = storeFolder.getStoreExtractor().incElementCounter(this.getClass());
            analyzeAppointment();
            if (statsFlag)
                extractAppointment(false, null);
        }
    }
}
