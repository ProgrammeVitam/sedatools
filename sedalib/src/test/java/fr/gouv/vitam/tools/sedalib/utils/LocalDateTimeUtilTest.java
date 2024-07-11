package fr.gouv.vitam.tools.sedalib.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(LocalDateTime.of(2024, 12, 25, 12, 34, 56), LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56."));
        assertEquals(LocalDateTime.of(2024, 12, 25, 12, 34, 56), LocalDateTimeUtil.parseDateTime("2024-12-25T12:34:56"));
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
        assertEquals("2024-12-25T12:34:56.123", LocalDateTimeUtil.getFormattedDateTime("2024-12-25T12:34:56.123PST")
        );
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
                LocalDateTimeUtil.getFormattedDateTime(FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 34, 56, 123000000).atZone(ZoneId.systemDefault()).toInstant()))
        );
        assertEquals(
                "2024-12-25T12:34:56.000",
                LocalDateTimeUtil.getFormattedDateTime(FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 34, 56, 0).atZone(ZoneId.systemDefault()).toInstant()))
        );
        assertEquals(
                "2024-12-25T12:34:00.000",
                LocalDateTimeUtil.getFormattedDateTime(FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 34, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))
        );
        assertEquals(
                "2024-12-25T12:00:00.000",
                LocalDateTimeUtil.getFormattedDateTime(FileTime.from(LocalDateTime.of(2024, 12, 25, 12, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))
        );
        assertEquals(
                "2024-12-25T00:00:00.000",
                LocalDateTimeUtil.getFormattedDateTime(FileTime.from(LocalDateTime.of(2024, 12, 25, 0, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant()))
        );
    }

}
