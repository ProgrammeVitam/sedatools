/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL-C license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL-C license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.utils;

import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime utilities
 */
public final class LocalDateTimeUtil {

    private static final DateTimeFormatter ZONED_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss][.SSS][zz]");
    private static final DateTimeFormatter SLASHED_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * yyyy-MM-dd'T'HH:mm[:ss][.SSS][zz]
     * 2024-12-25T12:34:56.123456789
     * 2024-12-25T12:34:56.123
     * 2024-12-25T12:34:56.
     * 2024-12-25T12:34:56
     * 2024-12-25T12:34
     * 2024-12-25
     */
    static LocalDateTime parseDateTime(String dateTime) {
        LocalDateTime ldt;
        try {
            ldt = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                ldt = LocalDateTime.parse(dateTime, ZONED_DATE_TIME_FORMAT);
            } catch (DateTimeParseException ex) {
                try {
                    ldt = LocalDate.parse(dateTime, DateTimeFormatter.ISO_DATE).atTime(0, 0, 0, 0);
                } catch (DateTimeParseException exc) {
                    ldt = LocalDate.parse(dateTime, SLASHED_DATE).atTime(0, 0, 0, 0);
                }
            }
        }
        return ldt;
    }

    /**
     * 2016-09-27T12:34:56.123
     * 2016-09-27T00:00:00.000
     */
    public static String getFormattedDateTime(String dateTime) {
        LocalDateTime ldt = LocalDateTimeUtil.parseDateTime(dateTime);
        return LocalDateTimeUtil.getFormattedDateTime(ldt);
    }

    /**
     * 2016-09-27T12:34:56.123
     * 2016-09-27T00:00:00.000
     */
    public static String getFormattedDateTime(LocalDateTime date) {
        return date.format(ZONED_DATE_TIME_FORMAT);
    }

    /**
     * 2016-09-27T12:34:56.123
     * 2016-09-27T00:00:00.000
     */
    public static String getFormattedDateTime(FileTime fileTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        return LocalDateTimeUtil.getFormattedDateTime(ldt);
    }

}
