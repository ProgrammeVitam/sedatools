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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests of DateTimeType for all XSD temporal formats")
public class DateTimeTypeTest {

    // --- Precise formats ---
    @Test
    @DisplayName(
        "Should recognise an OFFSET_DATE_TIME with explicit timezone offset (+02:00) and produce the correct UTC date"
    )
    void testOffsetDateTime() {
        String input = "2025-10-28T14:30:15+02:00";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.OFFSET_DATE_TIME, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("2025-10-28T12:30:15Z", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise an OFFSET_DATE_TIME in Zulu format (Z) and keep the UTC time unchanged")
    void testOffsetDateTimeZulu() {
        String input = "2025-10-28T14:30:15Z";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.OFFSET_DATE_TIME, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("2025-10-28T14:30:15Z", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise a DATE_TIME without timezone and generate a valid UTC")
    void testLocalDateTime() {
        String input = "2025-10-28T14:30:15.529";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.DATE_TIME, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertTrue(dtt.getUtcDateTimeString().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
    }

    @Test
    @DisplayName("Should recognise a simple DATE format and produce the corresponding UTC instant at midnight")
    void testDate() {
        String input = "2025-10-28";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.DATE, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("2025-10-28T00:00:00Z", dtt.getUtcDateTimeString());
    }

    // --- Imprecise formats ---
    @Test
    @DisplayName("Should recognise a G_YEAR format (year only) and not produce a UTC date")
    void testGYear() {
        String input = "2025";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.G_YEAR, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise a G_YEAR_MONTH format (year + month) and not produce a UTC date")
    void testGYearMonth() {
        String input = "2025-10";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.G_YEAR_MONTH, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise a G_MONTH format (month only) and not produce a UTC date")
    void testGMonth() {
        String input = "--10";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.G_MONTH, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise a G_MONTH_DAY format (month + day without year) and not produce a UTC date")
    void testGMonthDay() {
        String input = "--10-28";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.G_MONTH_DAY, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("", dtt.getUtcDateTimeString());
    }

    @Test
    @DisplayName("Should recognise a G_DAY format (day only) and not produce a UTC date")
    void testGDay() {
        String input = "---28";
        DateTimeType dtt = new DateTimeType("Test", input);
        assertEquals(DateTimeType.DateTimeFormatType.G_DAY, dtt.getFormatTypeEnum());
        assertEquals(input, dtt.getDateTimeString());
        assertEquals("", dtt.getUtcDateTimeString());
    }

    // --- Errors and utilities ---
    @Test
    @DisplayName("Should throw an exception when an invalid date is provided")
    void testInvalidDate() {
        assertThrows(RuntimeException.class, () -> new DateTimeType("Test", "invalid-date"));
    }

    @Test
    @DisplayName("Should export a valid date in the expected CSV format")
    void testCsvExport() throws Exception {
        DateTimeType dtt = new DateTimeType("DateElement", "2025-10-28T14:30:00Z");
        var csv = dtt.toCsvList();
        assertTrue(csv.containsKey(""));
        assertEquals("2025-10-28T14:30:00Z", csv.get(""));
    }

    @Test
    @DisplayName("Should correctly handle JSON setters for the value and format")
    void testJsonSetters() {
        DateTimeType dtt = new DateTimeType();
        dtt.setDateTimeString("2025-10-28T14:30:00Z");
        assertEquals(DateTimeType.DateTimeFormatType.OFFSET_DATE_TIME, dtt.getFormatTypeEnum());
        assertEquals("2025-10-28T14:30:00Z", dtt.getDateTimeString());
        dtt.setDateTimeString("");
        assertNull(dtt.getTemporalValue());
        assertNull(dtt.getFormatTypeEnum());
    }

    // --- toLocalDateTime() tests robust against system timezone ---
    @Test
    @DisplayName("toLocalDateTime() should return the local date-time corresponding to an OffsetDateTime UTC")
    void toLocalDateTime_givenOffsetDateTime_shouldReturnLocalDateTimeWithSystemOffset() {
        DateTimeType dt = new DateTimeType("LastModified", "2025-10-29T18:30:00Z");
        LocalDateTime result = dt.toLocalDateTime();
        assertNotNull(result, "Expected non-null LocalDateTime for OFFSET_DATE_TIME");
        // Conversion UTC -> system LocalDateTime
        LocalDateTime expectedLocal =
            ((OffsetDateTime) dt.getTemporalValue()).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expectedLocal, result, "LocalDateTime must match system local time");
    }

    @Test
    @DisplayName("toLocalDateTime() should extract the local date/time part of a LocalDateTime with no timezone")
    void toLocalDateTime_givenLocalDateTime_shouldReturnSameLocalDateTime() {
        DateTimeType dt = new DateTimeType("EventTime", "2025-10-29T18:30:00");
        LocalDateTime result = dt.toLocalDateTime();
        assertNotNull(result, "Expected non-null LocalDateTime for DATE_TIME");
        assertEquals(LocalDateTime.of(2025, 10, 29, 18, 30, 0), result);
    }

    @Test
    @DisplayName("toLocalDateTime() should convert a LocalDate to a LocalDateTime at local midnight")
    void toLocalDateTime_givenLocalDate_shouldReturnLocalDateAtMidnight() {
        DateTimeType dt = new DateTimeType("CreationDate", "2025-10-29");
        LocalDateTime result = dt.toLocalDateTime();
        assertNotNull(result, "Expected non-null LocalDateTime for DATE");
        assertEquals(LocalDateTime.of(2025, 10, 29, 0, 0, 0), result);
    }

    @Test
    @DisplayName("toLocalDateTime() should return null for all imprecise formats (gYear, gMonth, etc.)")
    void toLocalDateTime_givenImpreciseFormats_shouldReturnNull() {
        assertNull(new DateTimeType("YearOnly", "2025").toLocalDateTime(), "Expected null for gYear format");
        assertNull(new DateTimeType("YearMonth", "2025-10").toLocalDateTime(), "Expected null for gYearMonth format");
        assertNull(new DateTimeType("MonthOnly", "--10").toLocalDateTime(), "Expected null for gMonth format");
        assertNull(new DateTimeType("MonthDay", "--10-29").toLocalDateTime(), "Expected null for gMonthDay format");
        assertNull(new DateTimeType("DayOnly", "---29").toLocalDateTime(), "Expected null for gDay format");
    }

    @Test
    @DisplayName("toLocalDateTime() should return null if no value is defined")
    void toLocalDateTime_givenNullValue_shouldReturnNull() {
        DateTimeType dt = new DateTimeType("Empty", (String) null);
        assertNull(dt.toLocalDateTime(), "Expected null when temporalValue is null");
    }
}
