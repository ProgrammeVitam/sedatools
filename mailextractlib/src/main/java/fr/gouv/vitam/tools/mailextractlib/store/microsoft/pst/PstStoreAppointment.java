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
import fr.gouv.vitam.tools.mailextractlib.core.StoreContact;
import fr.gouv.vitam.tools.mailextractlib.formattools.rtf.HTMLFromRTFExtractor;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * StoreMessage sub-class for mail boxes extracted through libpst library.
 */
public class PstStoreAppointment extends StoreAppointment {

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
    public PstStoreAppointment(PstStoreFolder pstStoreFolder, PSTAppointment appointment) {
        super(pstStoreFolder);
        this.appointment = appointment;
    }

    private String getUniqId() {
        String result = appointment.getCleanGlobalObjectId().toString();
        if (result == null || result.isEmpty())
            result = appointment.getGlobalObjectId().toString();
        return result;
    }

    private String getFrom() {
        String fromAddr = appointment.getSenderEmailAddress();
        String fromName = appointment.getSenderName();
        String result = "";
        if ((fromName != null) && !fromName.isEmpty())
            result = fromName + " ";
        if ((fromAddr != null) && !fromAddr.isEmpty())
            result +=<" + fromAddr + " > ";
        return result.trim();
    }

    private ZonedDateTime getZonedDateTime(Date date, PSTTimeZone pstTimeZone) {
        ZoneId zoneId;
        if (pstTimeZone == null)
            zoneId = ZoneId.of("UTC");
        else
            zoneId = pstTimeZone.getSimpleTimeZone().toZoneId();
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

    private void analyzeMiscNotes() {
        String text = appointment.getBody();
        String html = appointment.getBodyHTML();
        String rtf = appointment.getRTFBody();

        if (!rtf.isEmpty()) {
            HTMLFromRTFExtractor htmlExtractor = new HTMLFromRTFExtractor(rtf);
            if (htmlExtractor.isEncapsulatedTEXTinRTF()) {
                text = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                rtf=null;
            } else if (htmlExtractor.isEncapsulatedHTMLinRTF())
                html = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                rtf=null;
        }
    }

    @Override
    public void analyzeAppointment() throws MailExtractLibException, InterruptedException {
        uniqId = getUniqId();

        subject = appointment.getSubject();
        location = appointment.getLocation();
        from = getFrom();
        toAttendees = appointment.getToAttendees();
        ccAttendees = appointment.getCCAttendees();
        startTime = getZonedDateTime(appointment.getStartTime(), appointment.getStartTimeZone());
        startTime = getZonedDateTime(appointment.getStartTime(), appointment.getEndTimeZone());
        miscNotes = appointment.getBody();

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


        fullName = getFullName();
        givenName = contact.getGivenName();
        lastName = contact.getSurname();
        miscNotes = contact.getBody();
        companyName = contact.getCompanyName();
        departmentName = contact.getDepartmentName();
        title = getTitle();
        postalAddress = contact.getPostalAddress();
        smtpAddress = contact.getSMTPAddress();
        primaryTelephoneNumber = contact.getPrimaryTelephoneNumber();
        mobileTelephoneNumbers = getMobileTelephoneNumbers();
        businessHomePage = contact.getBusinessHomePage();
        businessLocation = contact.getOfficeLocation();
        businessTelephoneNumbers = getBusinessTelephoneNumbers();
        businessAddress = getBusinessAddress();
        customerId = contact.getCustomerId();
        otherMailAddresses = getOtherMailAddresses();
        otherTelephoneNumbers = getOtherTelephoneNumbers();
        assistantName = contact.getAssistant();
        assistantTelephoneNumber = contact.getAssistantTelephoneNumber();
        personalHomePage = contact.getPersonalHomePage();
        homeLocation = contact.getLocation();
        homeTelephoneNumbers = getHomeTelephoneNumbers();
        homeAddress = getHomeAddress();
        nickName = contact.getNickname();

        // are not extracted the following values from java-libpst
        // getCallbackTelephoneNumber, getInitials, getKeyword, getLanguage, getMhsCommonName,
        // getOrganizationalIdNumber, getMiddleName, getDisplayNamePrefix, getPreferredByName,
        // getSpouseName, getComputerNetworkName, getTtytddPhoneNumber, getChildrensNames,
        // getTransmittableDisplayName, getTelexNumber, getHobbies, getOriginalDisplayName,
        // getOtherAddressCity, getOtherAddressCountry, getOtherAddressStateOrProvince,
        // getOtherAddressStreet, getOtherAddressPostOfficeBox, getOriginalDisplayName,
        // getFtpSite, getManagerName
    }

    @Override
    public void analyzeContactPicture() throws MailExtractLibException, InterruptedException {
        pictureFileName = null;
        pictureData = null;
        if (contact.getNumberOfAttachments() == 0)
            return;

        for (int i = 0; i < contact.getNumberOfAttachments(); i++) {
            PSTAttachment attachment;
            try {
                attachment = contact.getAttachment(i);
                if (attachment.isContactPhoto()) {
                    pictureFileName = attachment.getLongFilename();
                    try {
                        InputStream is = attachment.getFileInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buf)) != -1) {
                            baos.write(buf, 0, bytesRead);
                        }
                        pictureData = baos.toByteArray();
                        break;
                    } catch (PSTException | IOException e) {
                        pictureFileName = null;
                    }
                }
            } catch (PSTException | IOException ignored) {
            }
        }
    }
}
