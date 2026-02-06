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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * The {@code DateTimeType} class represents a SEDA metadata element containing a temporal value.
 * <p>
 * It supports all XML Schema Definition (XSD) temporal types:
 * <ul>
 *     <li>date</li>
 *     <li>dateTime</li>
 *     <li>dateTime with timezone (OffsetDateTime)</li>
 *     <li>gYear</li>
 *     <li>gYearMonth</li>
 *     <li>gMonth</li>
 *     <li>gMonthDay</li>
 *     <li>gDay</li>
 * </ul>
 */
public class DateTimeType extends NamedTypeMetadata {

    /**
     * Enumeration of supported temporal format types.
     */
    public enum DateTimeFormatType {
        OFFSET_DATE_TIME, DATE_TIME, DATE,
        G_YEAR, G_YEAR_MONTH, G_MONTH, G_MONTH_DAY, G_DAY
    }

    @JsonIgnore
    private TemporalAccessor temporalValue;

    private DateTimeFormatType formatType;

    // --- Regex patterns for format detection ---
    private static final Pattern OFFSET_DATE_TIME_PATTERN =
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,9})?)?([+-]\\d{2}:\\d{2}|Z)$");
    private static final Pattern DATE_TIME_PATTERN =
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,9})?)?$");
    private static final Pattern DATE_PATTERN =
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern G_YEAR_PATTERN =
        Pattern.compile("^\\d{4}$");
    private static final Pattern G_YEAR_MONTH_PATTERN =
        Pattern.compile("^\\d{4}-\\d{2}$");
    private static final Pattern G_MONTH_PATTERN =
        Pattern.compile("^--\\d{2}$");
    private static final Pattern G_MONTH_DAY_PATTERN =
        Pattern.compile("^--\\d{2}-\\d{2}$");
    private static final Pattern G_DAY_PATTERN =
        Pattern.compile("^---\\d{2}$");

    // =====================================================================================
    // Constructors
    // =====================================================================================

    /**
     * Default constructor.
     * <p>
     * Creates an empty {@code DateTimeType} without element name or value.
     */
    public DateTimeType() {
        this(null, (String) null);
    }

    /**
     * Constructor with an element name only.
     *
     * @param elementName the XML/SEDALib element name
     */
    public DateTimeType(String elementName) {
        this(elementName, (String) null);
    }

    /**
     * Constructor for {@link OffsetDateTime} values (date and time with timezone).
     *
     * @param elementName the XML/SEDALib element name
     * @param value       the OffsetDateTime value
     */
    public DateTimeType(String elementName, OffsetDateTime value) {
        super(elementName);
        this.temporalValue = value;
        this.formatType = DateTimeFormatType.OFFSET_DATE_TIME;
    }

    /**
     * Constructor for {@link LocalDateTime} values (date and time without timezone).
     *
     * @param elementName the XML/SEDALib element name
     * @param value       the LocalDateTime value
     */
    public DateTimeType(String elementName, LocalDateTime value) {
        super(elementName);
        this.temporalValue = value;
        this.formatType = DateTimeFormatType.DATE_TIME;
    }

    /**
     * Constructor for {@link LocalDate} values (date only).
     *
     * @param elementName the XML/SEDALib element name
     * @param dateValue   the LocalDate value
     */
    public DateTimeType(String elementName, LocalDate dateValue) {
        super(elementName);
        this.temporalValue = dateValue;
        this.formatType = DateTimeFormatType.DATE;
    }

    /**
     * Constructor for string date values.
     * <p>
     * The string is automatically parsed into the appropriate temporal type.
     *
     * @param elementName the XML/SEDALib element name
     * @param dateString  the date/time string to parse
     */
    public DateTimeType(String elementName, String dateString) {
        super(elementName);
        if (dateString == null || dateString.isEmpty()) {
            this.temporalValue = null;
            this.formatType = null;
        } else {
            parseDateString(dateString);
        }
    }

    // =====================================================================================
    // Parsing and Conversion
    // =====================================================================================

    /**
     * Parses a string into a temporal object and determines its format type.
     *
     * @param dateString the date string to parse
     * @throws RuntimeException if the string format is invalid or unrecognized
     */
    private void parseDateString(String dateString) {
        try {
            if (OFFSET_DATE_TIME_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = OffsetDateTime.parse(dateString);
                this.formatType = DateTimeFormatType.OFFSET_DATE_TIME;
            } else if (DATE_TIME_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = LocalDateTime.parse(dateString);
                this.formatType = DateTimeFormatType.DATE_TIME;
            } else if (DATE_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = LocalDate.parse(dateString);
                this.formatType = DateTimeFormatType.DATE;
            } else if (G_YEAR_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = Year.parse(dateString);
                this.formatType = DateTimeFormatType.G_YEAR;
            } else if (G_YEAR_MONTH_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = YearMonth.parse(dateString);
                this.formatType = DateTimeFormatType.G_YEAR_MONTH;
            } else if (G_MONTH_DAY_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = MonthDay.parse(dateString);
                this.formatType = DateTimeFormatType.G_MONTH_DAY;
            } else if (G_MONTH_PATTERN.matcher(dateString).matches()) {
                this.temporalValue = MonthDay.parse(dateString + "-01");
                this.formatType = DateTimeFormatType.G_MONTH;
            } else if (G_DAY_PATTERN.matcher(dateString).matches()) {
                int day = Integer.parseInt(dateString.substring(3));
                this.temporalValue = LocalDate.of(2000, 1, day);
                this.formatType = DateTimeFormatType.G_DAY;
            } else {
                throw new SEDALibException("Unrecognized date format: " + dateString);
            }
        } catch (DateTimeParseException | SEDALibException e) {
            throw new RuntimeException("Error parsing date: " + dateString, e);
        }
    }

    // =====================================================================================
    // XML Serialization
    // =====================================================================================

    /**
     * Writes the value as XML in SEDA format.
     *
     * @param xmlWriter the XML writer to use
     * @throws SEDALibException if an XML writing error occurs
     */
    @Override
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            if (temporalValue == null) {
                xmlWriter.writeElementValue(elementName, null);
                return;
            }
            DateTimeFormatter formatter = getFormatter();
            xmlWriter.writeElementValue(elementName, formatter.format(temporalValue));
        } catch (XMLStreamException e) {
            throw new SEDALibException("XML writing error in DateTimeType [" + elementName + "]", e);
        }
    }

    /**
     * Returns the formatter corresponding to the current {@link DateTimeFormatType}.
     *
     * @return a {@link DateTimeFormatter} adapted to the format type
     * @throws SEDALibException if the format type is unknown
     */
    private DateTimeFormatter getFormatter() throws SEDALibException {
        switch (formatType) {
            case OFFSET_DATE_TIME: return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            case DATE_TIME: return DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            case DATE: return DateTimeFormatter.ISO_LOCAL_DATE;
            case G_YEAR: return DateTimeFormatter.ofPattern("uuuu");
            case G_YEAR_MONTH: return DateTimeFormatter.ofPattern("uuuu-MM");
            case G_MONTH: return DateTimeFormatter.ofPattern("'--'MM");
            case G_MONTH_DAY: return DateTimeFormatter.ofPattern("'--'MM-dd");
            case G_DAY: return DateTimeFormatter.ofPattern("'---'dd");
            default: throw new SEDALibException("Date type not handled: " + formatType);
        }
    }

    // =====================================================================================
    // CSV Export
    // =====================================================================================

    /**
     * Converts this date/time value into a CSV-compatible representation.
     *
     * @return a {@link LinkedHashMap} containing one entry: the string representation of the value
     * @throws SEDALibException if the date format cannot be determined
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String valueStr = (temporalValue != null) ? getFormatter().format(temporalValue) : "";
        result.put("", valueStr);
        return result;
    }

    // =====================================================================================
    // XML Import
    // =====================================================================================

    /**
     * Reads a DateTimeType element from an XML stream in SEDA format.
     *
     * @param xmlReader the XML reader
     * @return {@code true} if the element was successfully read, {@code false} otherwise
     * @throws SEDALibException if XML parsing fails or the element is malformed
     */
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        try {
            if (!xmlReader.peekBlockIfNamed(elementName))
                return false;
            XMLEvent event = xmlReader.nextUsefullEvent();
            elementName = event.asStartElement().getName().getLocalPart();
            event = xmlReader.nextUsefullEvent();
            if (event.isCharacters()) {
                parseDateString(event.asCharacters().getData());
                event = xmlReader.nextUsefullEvent();
            } else {
                temporalValue = null;
                formatType = null;
            }
            if (!event.isEndElement() || !elementName.equals(event.asEndElement().getName().getLocalPart()))
                throw new SEDALibException("Element " + elementName + " not properly terminated");
            return true;
        } catch (Exception e) {
            throw new SEDALibException("XML reading error in DateTimeType", e);
        }
    }

    // =====================================================================================
    // Getters and Utility Methods
    // =====================================================================================

    /** @return the underlying temporal value */
    @JsonIgnore
    public TemporalAccessor getTemporalValue() {
        return temporalValue;
    }

    /** @return the {@link DateTimeFormatType} enum value */
    @JsonIgnore
    public DateTimeFormatType getFormatTypeEnum() {
        return formatType;
    }

    /** @return the format type as a string */
    @JsonIgnore
    @JsonGetter("dateTimeFormat")
    public String getFormatType() {
        return (formatType == null) ? "" : formatType.name();
    }

    /** @return the date/time as a formatted string */
    @JsonGetter("dateTimeString")
    public String getDateTimeString() {
        if (temporalValue == null)
            return "";
        try {
            return getFormatter().format(temporalValue);
        } catch (SEDALibException e) {
            return "";
        }
    }

    /**
     * Sets the temporal value from a string representation.
     *
     * @param dateTimeString the date/time string to parse
     */
    @JsonSetter("dateTimeString")
    public void setDateTimeString(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            this.temporalValue = null;
            this.formatType = null;
        } else {
            parseDateString(dateTimeString);
        }
    }

    /**
     * Returns the UTC ISO 8601 representation of the temporal value (if applicable).
     *
     * @return the UTC date-time string, or an empty string if not relevant
     */
    @JsonIgnore
    public String getUtcDateTimeString() {
        if (temporalValue == null)
            return "";
        if (formatType == DateTimeFormatType.G_YEAR
            || formatType == DateTimeFormatType.G_YEAR_MONTH
            || formatType == DateTimeFormatType.G_MONTH
            || formatType == DateTimeFormatType.G_MONTH_DAY
            || formatType == DateTimeFormatType.G_DAY)
            return "";
        try {
            OffsetDateTime utcDateTime;
            if (temporalValue instanceof OffsetDateTime) {
                utcDateTime = ((OffsetDateTime) temporalValue).withOffsetSameInstant(ZoneOffset.UTC);
            } else if (temporalValue instanceof LocalDateTime) {
                utcDateTime = ((LocalDateTime) temporalValue)
                    .atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toOffsetDateTime();
            } else if (temporalValue instanceof LocalDate) {
                utcDateTime = ((LocalDate) temporalValue)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toOffsetDateTime();
            } else {
                return "";
            }
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(utcDateTime);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Converts the value to a {@link LocalDateTime} (only for precise date/time formats).
     *
     * @return the {@link LocalDateTime} equivalent, or {@code null} if the type is partial (gYear, gMonth, etc.)
     */
    @JsonIgnore
    public LocalDateTime toLocalDateTime() {
        if (temporalValue == null)
            return null;
        if (formatType == DateTimeFormatType.G_YEAR
            || formatType == DateTimeFormatType.G_YEAR_MONTH
            || formatType == DateTimeFormatType.G_MONTH
            || formatType == DateTimeFormatType.G_MONTH_DAY
            || formatType == DateTimeFormatType.G_DAY)
            return null;
        try {
            switch (formatType) {
                case OFFSET_DATE_TIME:
                    return ((OffsetDateTime) temporalValue)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime();
                case DATE_TIME:
                    return (LocalDateTime) temporalValue;
                case DATE:
                    return ((LocalDate) temporalValue).atStartOfDay();
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
