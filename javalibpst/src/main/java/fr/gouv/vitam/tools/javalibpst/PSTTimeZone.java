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
package fr.gouv.vitam.tools.javalibpst;

import java.util.Calendar;
import java.util.SimpleTimeZone;

/**
 * Class containing time zone information
 * 
 * @author Orin Eman
 *
 *
 */

public class PSTTimeZone {

    private String name;
    private TZRule rule;
    private SimpleTimeZone simpleTimeZone = null;

    PSTTimeZone(final byte[] timeZoneData) {
        this.rule = null;
        this.name = "";

        try {
            final int headerLen = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, 2, 4);
            final int nameLen = 2 * (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, 6, 8);
            this.name = new String(timeZoneData, 8, nameLen, "UTF-16LE");
            int ruleOffset = 8 + nameLen;
            final int nRules = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, ruleOffset, ruleOffset + 2);

            ruleOffset = 4 + headerLen;
            for (int rule = 0; rule < nRules; ++rule) {
                // Is this rule the effective rule?
                final int flags = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, ruleOffset + 4,
                    ruleOffset + 6);
                if ((flags & 0x0002) != 0) {
                    this.rule = new TZRule(timeZoneData, ruleOffset + 6);
                    break;
                }
                ruleOffset += 66;
            }
        } catch (final Exception e) {
            if (PSTFile.isPrintErrors())
                System.err.printf("Exception reading timezone: %s\n", e.toString());
            e.printStackTrace();
            this.rule = null;
            this.name = "";
        }
    }

    PSTTimeZone(String name, final byte[] timeZoneData) {
        this.name = name;
        this.rule = null;

        try {
            this.rule = new TZRule(new SYSTEMTIME(), timeZoneData, 0);
        } catch (final Exception e) {
            if (PSTFile.isPrintErrors())
                System.err.printf("Exception reading timezone: %s\n", e.toString());
            e.printStackTrace();
            this.rule = null;
            name = "";
        }
    }

    public PSTTimeZone(String name, SimpleTimeZone simpleTimeZone) {
        this.name = name;
        this.rule = null;
        this.simpleTimeZone=simpleTimeZone;
    }

    public String getName() {
        return this.name;
    }

    public SimpleTimeZone getSimpleTimeZone() {
        if (this.simpleTimeZone != null) {
            return this.simpleTimeZone;
        }

        if (this.rule.startStandard.wMonth == 0) {
            // A time zone with no daylight savings time
            this.simpleTimeZone = new SimpleTimeZone((this.rule.lBias + this.rule.lStandardBias) * 60 * 1000,
                this.name);

            return this.simpleTimeZone;
        }

        final int startMonth = (this.rule.startDaylight.wMonth - 1) + Calendar.JANUARY;
        final int startDayOfMonth = (this.rule.startDaylight.wDay == 5) ? -1
            : ((this.rule.startDaylight.wDay - 1) * 7) + 1;
        final int startDayOfWeek = this.rule.startDaylight.wDayOfWeek + Calendar.SUNDAY;
        final int endMonth = (this.rule.startStandard.wMonth - 1) + Calendar.JANUARY;
        final int endDayOfMonth = (this.rule.startStandard.wDay == 5) ? -1
            : ((this.rule.startStandard.wDay - 1) * 7) + 1;
        final int endDayOfWeek = this.rule.startStandard.wDayOfWeek + Calendar.SUNDAY;
        final int savings = (this.rule.lStandardBias - this.rule.lDaylightBias) * 60 * 1000;

        this.simpleTimeZone = new SimpleTimeZone(-((this.rule.lBias + this.rule.lStandardBias) * 60 * 1000), this.name,
            startMonth, startDayOfMonth, -startDayOfWeek,
            (((((this.rule.startDaylight.wHour * 60) + this.rule.startDaylight.wMinute) * 60)
                + this.rule.startDaylight.wSecond) * 1000) + this.rule.startDaylight.wMilliseconds,
            endMonth, endDayOfMonth, -endDayOfWeek,
            (((((this.rule.startStandard.wHour * 60) + this.rule.startStandard.wMinute) * 60)
                + this.rule.startStandard.wSecond) * 1000) + this.rule.startStandard.wMilliseconds,
            savings);

        return this.simpleTimeZone;
    }

    public boolean isEqual(final PSTTimeZone rhs) {
        if (this.name.equalsIgnoreCase(rhs.name)) {
            if (this.rule.isEqual(rhs.rule)) {
                return true;
            }

            if (PSTFile.isPrintErrors())
                System.err.printf("Warning: different timezones with the same name: %s\n", this.name);
        }
        return false;
    }

    public SYSTEMTIME getStart() {
        return this.rule.dtStart;
    }

    public int getBias() {
        return this.rule.lBias;
    }

    public int getStandardBias() {
        return this.rule.lStandardBias;
    }

    public int getDaylightBias() {
        return this.rule.lDaylightBias;
    }

    public SYSTEMTIME getDaylightStart() {
        return this.rule.startDaylight;
    }

    public SYSTEMTIME getStandardStart() {
        return this.rule.startStandard;
    }

    public class SYSTEMTIME {

        SYSTEMTIME() {
            this.wYear = 0;
            this.wMonth = 0;
            this.wDayOfWeek = 0;
            this.wDay = 0;
            this.wHour = 0;
            this.wMinute = 0;
            this.wSecond = 0;
            this.wMilliseconds = 0;
        }

        SYSTEMTIME(final byte[] timeZoneData, final int offset) {
            this.wYear = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset, offset + 2) & 0x7FFF);
            this.wMonth = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 2, offset + 4)
                & 0x7FFF);
            this.wDayOfWeek = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 4, offset + 6)
                & 0x7FFF);
            this.wDay = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 6, offset + 8)
                & 0x7FFF);
            this.wHour = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 8, offset + 10)
                & 0x7FFF);
            this.wMinute = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 10, offset + 12)
                & 0x7FFF);
            this.wSecond = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 12, offset + 14)
                & 0x7FFF);
            this.wMilliseconds = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 14,
                offset + 16) & 0x7FFF);
        }

        boolean isEqual(final SYSTEMTIME rhs) {
            return this.wYear == rhs.wYear && this.wMonth == rhs.wMonth && this.wDayOfWeek == rhs.wDayOfWeek
                && this.wDay == rhs.wDay && this.wHour == rhs.wHour && this.wMinute == rhs.wMinute
                && this.wSecond == rhs.wSecond && this.wMilliseconds == rhs.wMilliseconds;
        }

        public short wYear;
        public short wMonth;
        public short wDayOfWeek;
        public short wDay;
        public short wHour;
        public short wMinute;
        public short wSecond;
        public short wMilliseconds;
    }

    /**
     * A static copy of the UTC time zone, available for others to use
     */
    public static SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");

    private class TZRule {

        TZRule(final SYSTEMTIME dtStart, final byte[] timeZoneData, final int offset) {
            this.dtStart = dtStart;
            this.InitBiases(timeZoneData, offset);
            @SuppressWarnings("unused")
            final short wStandardYear = (short) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 12,
                offset + 14);
            this.startStandard = new SYSTEMTIME(timeZoneData, offset + 14);
            @SuppressWarnings("unused")
            final short wDaylightYear = (short) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 30,
                offset + 32);
            this.startDaylight = new SYSTEMTIME(timeZoneData, offset + 32);
        }

        TZRule(final byte[] timeZoneData, final int offset) {
            this.dtStart = new SYSTEMTIME(timeZoneData, offset);
            this.InitBiases(timeZoneData, offset + 16);
            this.startStandard = new SYSTEMTIME(timeZoneData, offset + 28);
            this.startDaylight = new SYSTEMTIME(timeZoneData, offset + 44);
        }

        private void InitBiases(final byte[] timeZoneData, final int offset) {
            this.lBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset, offset + 4);
            this.lStandardBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 4, offset + 8);
            this.lDaylightBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 8, offset + 12);
        }

        boolean isEqual(final TZRule rhs) {
            return this.dtStart.isEqual(rhs.dtStart) && this.lBias == rhs.lBias
                && this.lStandardBias == rhs.lStandardBias && this.lDaylightBias == rhs.lDaylightBias
                && this.startStandard.isEqual(rhs.startStandard) && this.startDaylight.isEqual(rhs.startDaylight);
        }

        SYSTEMTIME dtStart;
        int lBias;
        int lStandardBias;
        int lDaylightBias;
        SYSTEMTIME startStandard;
        SYSTEMTIME startDaylight;
    }
}
