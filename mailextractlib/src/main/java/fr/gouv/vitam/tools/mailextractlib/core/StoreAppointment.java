package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.DateRange;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.File;
import java.io.PrintStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class StoreAppointment extends StoreElement {

    protected String uniqId;

    protected String subject, location;
    protected String from;
    protected String toAttendees;
    protected String ccAttendees;
    protected ZonedDateTime startTime;
    protected ZonedDateTime endTime;
    protected String miscNotes;
    protected String otherMiscNotes;

    protected boolean isBusy;

    protected List<StoreAttachment> attachments;

    // for appointment organized with other people
    protected int sequenceNumber;
    protected int responseStatus;

    // for recurrent appointment
    protected String recurencePattern;
    protected ZonedDateTime startRecurrenceTime;
    protected ZonedDateTime endRecurrenceTime;
    protected List<StoreAppointment> exceptions;

    // for exceptions in recurring appointment
    protected boolean isRecurrenceDeletion;

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
        String result = "appointment " + getStoreExtractor().getGlobalListCounter(this.getClass());
        if (subject != null)
            result += " [" + subject + "/";
        else
            result += " [no subject/";
        result+=getDateInDefinedTimeZone(startTime)+" - "+getDateInDefinedTimeZone(endTime)+"]";
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
    abstract public void analyzeAppointment() throws MailExtractLibException, InterruptedException;

    /**
     * Gets appointments global list name used for the csv file name construction.
     *
     * @return the global list name
     */
    static public String getGlobalListName() {
        return "appointments";
    }

    /**
     * Print the header for contacts list csv file
     *
     * @param ps the dedicated print stream
     */
    static public void printGlobalListCSVHeader(PrintStream ps) {
        ps.println("ID;Subject;Location;ToAttendees;CcAttendees;StartTime;EndTime;" +
                "isBusy;MiscNotes;uniqID;isRecurrent;RecurrencePattern;StartRecurrenceTime;EndRecurrenceTime;isExceptionFrom;isDeletion;otherMiscNotes");
    }

    /**
     * Extract to the contacts list, after initialising it if first contact, and to the contact picture file.
     * <p>The picture file is saved in a directory called "Contacts Pictures" and is named using the line number
     * listLineId as "contact_id".ext with the extension of the original file.******************************
     *
     * @param writeFlag the write flag
     */
    public void extractAppointment(boolean writeFlag, StoreAppointment father) throws InterruptedException, MailExtractLibException {
        if (writeFlag && storeFolder.getStoreExtractor().getOptions().extractObjectsLists) {
            writeToAppointmentsList(null);
            if ((attachments!=null) && (!attachments.isEmpty())) {
                ArchiveUnit attachmentNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.storeExtractor.destRootPath +
                        File.separator + storeFolder.storeExtractor.destName + File.separator + "appointments", "AppointmentAttachments#" + listLineId);
                attachmentNode.addMetadata("DescriptionLevel", "RecordGroup", true);
                attachmentNode.addMetadata("Title", "Appointment Attachments #" + listLineId, true);
                attachmentNode.addMetadata("Description", "Appointment attachments extracted for " + subject + "[" + startTime + "-" + endTime + "]", true);
                attachmentNode.addMetadata("StartDate", getDateInUTCTimeZone(startTime), true);
                attachmentNode.addMetadata("EndDate", getDateInUTCTimeZone(endTime), true);
                attachmentNode.write();
                StoreAttachment.extractAttachments(attachments, attachmentNode, writeFlag);
            }
            if (exceptions != null)
                for (StoreAppointment a : exceptions)
                    a.extractAppointment(writeFlag, this);
        }
    }

    private String getDateInUTCTimeZone(ZonedDateTime date) {
        String result;
        if (date != null)
            result = date.withZoneSameInstant(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME);
        else
            result = "Unknown";
        return result;
    }

    private String getDateInDefinedTimeZone(ZonedDateTime date) {
        String result;
        if (date != null)
            result = date.format(DateTimeFormatter.ISO_DATE_TIME);
        else
            result = "Unknown";
        return result;
    }

    private void writeToAppointmentsList(StoreAppointment father) {
        PrintStream ps = storeFolder.getStoreExtractor().getGlobalListPS(this.getClass());
        ps.format("\"%d\";", listLineId);
        ps.format("\"%s\";", filterHyphenForCsv(subject));
        ps.format("\"%s\";", filterHyphenForCsv(location));
        ps.format("\"%s\";", filterHyphenForCsv(toAttendees));
        ps.format("\"%s\";", filterHyphenForCsv(ccAttendees));
        ps.format("\"%s\";", getDateInDefinedTimeZone(startTime));
        ps.format("\"%s\";", getDateInDefinedTimeZone(endTime));
        ps.format("\"%s\";", (isBusy ? "X" : ""));
        ps.format("\"%s\";", filterHyphenForCsv(miscNotes));
        ps.format("\"%s\";", filterHyphenForCsv(uniqId));
        ps.format("\"%s\";", (recurencePattern != null ? "X" : ""));
        ps.format("\"%s\";", filterHyphenForCsv(recurencePattern));
        ps.format("\"%s\";", getDateInDefinedTimeZone(startRecurrenceTime));
        ps.format("\"%s\";", getDateInDefinedTimeZone(endRecurrenceTime));
        ps.format("\"%s\";", (father != null ? Integer.toString(father.listLineId):""));
        ps.format("\"%s\";", (isRecurrenceDeletion ? "X" : ""));
        ps.format("\"%s\"", filterHyphenForCsv(otherMiscNotes));
        ps.println("");
        ps.flush();
    }

    @Override
    public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
        analyzeAppointment();
        StoreAttachment.detectStoreAttachments(attachments);
        extractAppointment(writeFlag, null);
    }

    @Override
    public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
        analyzeAppointment();
        if (statsFlag)
            extractAppointment(false, null);
    }
}
