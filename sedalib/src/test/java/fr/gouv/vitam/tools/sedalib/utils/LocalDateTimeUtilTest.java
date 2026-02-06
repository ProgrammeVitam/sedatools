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
package fr.gouv.vitam.tools.sedalib.utils;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SedaContextExtension.class)
class LocalDateTimeUtilTest {

    @Test
    public void parseDateTime_tests() {
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 0, 0, 0),
            LocalDateTimeUtil.parseDateTime("2024-12-25T00:00:00.000000000")
        );
        assertEquals(LocalDateTime.of(2024, 12, 25, 0, 0, 0), LocalDateTimeUtil.parseDateTime("2024-12-25T00:00:00"));
        assertEquals(LocalDateTime.of(2024, 12, 25, 0, 0, 0), LocalDateTimeUtil.parseDateTime("2024-12-25T00:00"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T00"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T"));
        assertEquals(LocalDateTime.of(2024, 12, 25, 0, 0, 0), LocalDateTimeUtil.parseDateTime("2024-12-25"));
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123456789),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.123456789")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123456780),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.12345678")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123456700),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.1234567")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123456000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.123456")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123450000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.12345")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123400000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.1234")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123000000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.123")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 120000000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.12")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56, 100000000),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.1")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56.")
        );
        assertEquals(
            LocalDateTime.of(2024, 12, 25, 12, 34, 56),
            LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56")
        );
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:5"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:"));
        assertEquals(LocalDateTime.of(2024, 12, 25, 12, 34, 0), LocalDateTimeUtil.parseDateTime("2024-12-25T12:34"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T12:3"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T12:"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T12"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T1"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-25T"));
        assertEquals(LocalDateTime.of(2024, 12, 25, 0, 0, 0), LocalDateTimeUtil.parseDateTime("2024-12-25"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.parseDateTime("2024-12-2"));
    }

    @Test
    public void getFormattedDateTime_String_tests() {
        assertEquals("2024-12-25T00:00:00.000", LocalDateTimeUtil.getFormattedDateTime("25/12/2024"));
        assertEquals("2024-12-25T00:00:00.000", LocalDateTimeUtil.getFormattedDateTime("2024-12-25"));
        assertThrows(DateTimeParseException.class, () -> LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12"));
        assertEquals("2024-12-25T12:00:00.000", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:00"));
        assertEquals("2024-12-25T12:34:00.000", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34"));
        assertEquals("2024-12-25T12:34:56.000", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56"));
        assertEquals("2024-12-25T12:34:56.000", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56PST"));
        assertEquals("2024-12-25T12:34:56.120", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56.12"));
        assertEquals("2024-12-25T12:34:56.123", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56.123Z"));
        assertEquals("2024-12-25T12:34:56.123", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56.123PST"));
    }

    @Test
    public void getFormattedDateTime_LocalDateTime_tests() {
        assertEquals(
            "2024-12-25T12:34:56.123",
            LocalDateTimeUtil.getFormattedDateTime(LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123000000))
        );
        assertEquals(
            "2024-12-25T12:34:56.000",
            LocalDateTimeUtil.getFormattedDateTime(LocalDateTime.of(2024, 12, 25, 12, 34, 56, 0))
        );
        assertEquals(
            "2024-12-25T12:34:00.000",
            LocalDateTimeUtil.getFormattedDateTime(LocalDateTime.of(2024, 12, 25, 12, 34, 0, 0))
        );
        assertEquals(
            "2024-12-25T12:00:00.000",
            LocalDateTimeUtil.getFormattedDateTime(LocalDateTime.of(2024, 12, 25, 12, 0, 0, 0))
        );
        assertEquals(
            "2024-12-25T00:00:00.000",
            LocalDateTimeUtil.getFormattedDateTime(LocalDateTime.of(2024, 12, 25, 0, 0, 0, 0))
        );
    }

    @Test
    public void getFormattedDateTime_FileTime_tests() {
        assertEquals(
            "2024-12-25T12:34:56.123",
            LocalDateTimeUtil.getFormattedDateTime(
                FileTime.from(
                    LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123000000).atZone(ZoneId.systemDefault()).toInstant()
                )
            )
        );
        assertEquals(
            "2024-12-25T12:34:56.000",
            LocalDateTimeUtil.getFormattedDateTime(
                FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 34, 56, 0).atZone(ZoneId.systemDefault()).toInstant())
            )
        );
        assertEquals(
            "2024-12-25T12:34:00.000",
            LocalDateTimeUtil.getFormattedDateTime(
                FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 34, 0, 0).atZone(ZoneId.systemDefault()).toInstant())
            )
        );
        assertEquals(
            "2024-12-25T12:00:00.000",
            LocalDateTimeUtil.getFormattedDateTime(
                FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())
            )
        );
        assertEquals(
            "2024-12-25T00:00:00.000",
            LocalDateTimeUtil.getFormattedDateTime(
                FileTime.from(LocalDateTime.of(2024, 12, 25, 0, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant())
            )
        );
    }
}
