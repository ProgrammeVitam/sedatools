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
package fr.gouv.vitam.tools.mailextractlib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class for automatic computation of start and end date of folders.
 * <p>
 * The start and end date of a folder are the min and max dates of all messages
 * and all its descendants contain, or, what is the same, of all the messages
 * it directly contains and of the start and end dates of all its subfolders.
 */
public class DateRange {

    private Date start, end;

    /**
     * Instantiates a new date range.
     */
    public DateRange() {
        start = null;
        end = null;
    }

    private static SimpleDateFormat writeformat;

    static {
        writeformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        writeformat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get a compliant ISO date form of a Date.
     * <p>
     * YYYY-MM-DD'T'HH:mm:ss'Z' (eg 1997-07-16T19:20:30Z)
     * <p>
     * where:
     * <ul>
     * <li>YYYY = four-digit year</li>
     * <li>MM = two-digit month (01=January, etc.)</li>
     * <li>DD = two-digit day of month (01 through 31)</li>
     * <li>hh = two digits of hour (00 through 23) (am/pm NOT allowed)</li>
     * <li>mm = two digits of minute (00 through 59)</li>
     * <li>ss = two digits of second (00 through 59)</li>
     * </ul>
     * The ending Z is the O hour offset time zone designator in UTC.
     *
     * @param date
     *            Date
     * @return the ISO date string
     */
    public static String getISODateString(Date date) {
        if (date == null) {
            return null;
        } else {
            return writeformat.format(date);
        }
    }

    /**
     * Checks if the date range is defined.
     *
     * @return true, if is defined
     */
    public boolean isDefined() {
        return (start != null) && (end != null);
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public Date getStart() {
        return start;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public Date getEnd() {
        return end;
    }

    /**
     * Take into account a new date, and extend the current date range if
     * outside.
     *
     * @param date
     *            Date
     */
    public void extendRange(Date date) {
        if (date != null) {
            if (start == null) {
                start = date;
                end = date;
            } else {
                if (start.after(date)) start = date;
                else if (end.before(date)) end = date;
            }
        }
    }

    /**
     * Take into account an other date range, and extend the current date range
     * if outside.
     *
     * @param dateRange
     *            Date range
     */
    public void extendRange(DateRange dateRange) {
        if (dateRange.start != null) {
            if (start == null) {
                start = dateRange.start;
                end = dateRange.end;
            } else {
                if (start.after(dateRange.start)) start = dateRange.start;
                if (end.before(dateRange.end)) end = dateRange.end;
            }
        }
    }
}
