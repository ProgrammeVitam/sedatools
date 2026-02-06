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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Class to hold decoded PidTagConversationIndex
 *
 * @author Nick Buller
 */
public class PSTConversationIndex {
    private static final int HUNDRED_NS_TO_MS = 1000;
    private static final int MINIMUM_HEADER_SIZE = 22;
    private static final int RESPONSE_LEVEL_SIZE = 5;

    private Date deliveryTime;
    private UUID guid;
    private List<ResponseLevel> responseLevels = new ArrayList<>();

    public Date getDeliveryTime() {
        return this.deliveryTime;
    }

    public UUID getGuid() {
        return this.guid;
    }

    public List<ResponseLevel> getResponseLevels() {
        return this.responseLevels;
    }

    @Override
    public String toString() {
        return this.guid + "@" + this.deliveryTime + " " + this.responseLevels.size() + " ResponseLevels";
    }

    public class ResponseLevel {
        short deltaCode;
        long timeDelta;
        short random;

        public ResponseLevel(final short deltaCode, final long timeDelta, final short random) {
            this.deltaCode = deltaCode;
            this.timeDelta = timeDelta;
            this.random = random;
        }

        public short getDeltaCode() {
            return this.deltaCode;
        }

        public long getTimeDelta() {
            return this.timeDelta;
        }

        public short getRandom() {
            return this.random;
        }

        public Date withOffset(final Date anchorDate) {
            return new Date(anchorDate.getTime() + this.timeDelta);
        }
    }

    protected PSTConversationIndex(final byte[] rawConversationIndex) {
        if (rawConversationIndex != null && rawConversationIndex.length >= MINIMUM_HEADER_SIZE) {
            this.decodeHeader(rawConversationIndex);
            if (rawConversationIndex.length >= MINIMUM_HEADER_SIZE + RESPONSE_LEVEL_SIZE) {
                this.decodeResponseLevel(rawConversationIndex);
            }
        }
    }

    private void decodeHeader(final byte[] rawConversationIndex) {
        // According to the Spec the first byte is not included, but I believe
        // the spec is incorrect!
        // int reservedheaderMarker = (int)
        // PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 0, 1);

        final long deliveryTimeHigh = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 0, 4);
        final long deliveryTimeLow = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 4, 6) << 16;
        this.deliveryTime = PSTObject.filetimeToDate((int) deliveryTimeHigh, (int) deliveryTimeLow);

        final long guidHigh = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 6, 14);
        final long guidLow = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 14, 22);

        this.guid = new UUID(guidHigh, guidLow);
    }

    private void decodeResponseLevel(final byte[] rawConversationIndex) {
        final int responseLevelCount = (rawConversationIndex.length - MINIMUM_HEADER_SIZE) / RESPONSE_LEVEL_SIZE;
        this.responseLevels = new ArrayList<>(responseLevelCount);

        for (int responseLevelIndex = 0, position = 22; responseLevelIndex < responseLevelCount; responseLevelIndex++, position += RESPONSE_LEVEL_SIZE) {

            final long responseLevelValue = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, position,
                position + 5);
            final short deltaCode = (short) (responseLevelValue >> 39);
            final short random = (short) (responseLevelValue & 0xFF);

            // shift by 1 byte (remove the random) and mask off the deltaCode
            long deltaTime = (responseLevelValue >> 8) & 0x7FFFFFFF;

            if (deltaCode == 0) {
                deltaTime <<= 18;
            } else {
                deltaTime <<= 23;
            }

            deltaTime /= HUNDRED_NS_TO_MS;

            this.responseLevels.add(responseLevelIndex, new ResponseLevel(deltaCode, deltaTime, random));
        }
    }
}
