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

package fr.gouv.vitam.tools.mailextractlib.store.javamail.mbox;

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import javax.mail.internet.SharedInputStream;
import javax.mail.util.SharedByteArrayInputStream;
import javax.mail.util.SharedFileInputStream;
import java.io.*;

/**
 * Optimized buffered mbox file reader for Thunderbird mbox file.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class MboxReader {

    private MailExtractProgressLogger logger;

    private String filePath;

    // for random access to data when file
    private RandomAccessFile raf;

    private SharedInputStream sifs;

    // for buffered access to RandomAccessFile
    private static int BUFFER_SIZE = 4096;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private int len = 0;
    private long bufferPos = 0;
    private int curPos = 0;
    private int lineNum = 0;
    private long fromLineEnd = 0;

    /**
     * Instantiates a new thunder mbox file reader.
     *
     * @param logger
     *            Operation store extractor logger
     * @param file
     *            File containing the mbox formatted data
     * @throws IOException
     *             Unable to open the file.
     */
    public MboxReader(MailExtractProgressLogger logger, File file) throws IOException {
        this.logger = logger;
        this.filePath = file.getPath();
        sifs = new SharedFileInputStream(file);
        raf = new RandomAccessFile(file, "r");
    }

    /**
     * Instantiates a new thunder mbox byte reader.
     *
     * @param logger
     *            Operation store extractor logger
     * @param source
     *            Byte array containing the mbox formatted data
     */
    public MboxReader(MailExtractProgressLogger logger, byte[] source) {
        this.logger = logger;
        sifs = new SharedByteArrayInputStream(source);
    }

    /**
     * Gets the logger created during the store extractor construction, and used
     * in all mailextract classes.
     *
     * <p>
     * For convenience each class which may have some log actions has it's own
     * getProgressLogger method always returning the store extractor logger.
     *
     * @return logger
     */
    public MailExtractProgressLogger getProgressLogger() {
        return logger;
    }

    /**
     * Close.
     *
     * @throws IOException
     *             Unable to close the file.
     */
    public void close() throws IOException {
        if (raf != null)
            raf.close();
        if (sifs instanceof InputStream)
            ((InputStream) sifs).close();
    }

    /**
     * New stream, created from this file containing bytes from start position
     * to end-1 position
     * <p>
     * If end=-1 from bytes are from start position to the end of file
     *
     * @param start
     *            Start
     * @param end
     *            End
     * @return the input stream
     */
    public InputStream newStream(long start, long end) {
        return sifs.newStream(start, end);
    }

    // buffered read
    private final int read() throws IOException {
        if (raf != null) {
            // if File
            if (curPos >= len) {
                bufferPos = raf.getFilePointer();
                if ((len = raf.read(buffer)) == -1)
                    return -1;
                curPos = 0;
            }
        } else {
            // if byte[]
            // TODO get rid of buffer when byte[]
            if (curPos >= len) {
                bufferPos = sifs.getPosition();
                if ((len = ((ByteArrayInputStream) sifs).read(buffer)) == -1)
                    return -1;
                curPos = 0;
            }
        }
        return buffer[curPos++];
    }

    // buffered get file pointer
    private final long getPointer() {
        return bufferPos + curPos;
    }

    // read a complete line but return only first 64 bytes
    private final int readFirstBytesLine(byte[] buffer) throws IOException {
        int i = 0;
        int b;

        lineNum++;
        while (true) {
            b = read();
            if (b == -1)
                return -1;
            if (b == '\n')
                return i;
            if (i < 64) {
                buffer[i++] = (byte) b;
            }
        }
    }

    // construct a String from buffer
    private String constructLine(byte[] buffer, int len) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            final char c = (char) (buffer[i]);
            if (c >= 32)
                stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    // verify line compliance to the delimiter pattern
    // TODO get information on the Thunderbird mbox file delimiter
    // pattern and verify the date if present
    private boolean isCompliantFromLine(byte[] buffer, int len) {
        // too long to be a delimiter line
        if (len > 34) {
            return false;
        } else {
            String line = constructLine(buffer, len);
            if (line.length() == 5)
                return true;
            else if (line.startsWith("From - "))
                return true;
            getProgressLogger().progressLogWithoutInterruption(MailExtractProgressLogger.MESSAGE_DETAILS, "mailextract.mbox|thunderbird: Misleading '" + line + "' line in file " + filePath
                    + " at line " + Integer.toString(lineNum) + " is not considered as a message delimiter");
            return false;
        }
    }

    /**
     * Gets the next position of a "From - date" line start.
     *
     * @return File position
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long getNextFromLineBeg() throws IOException {
        long beg;
        int len;
        byte[] buffer = new byte[64];

        while (true) {
            beg = getPointer();
            len = readFirstBytesLine(buffer);
            if (len == -1) {
                fromLineEnd = -1;
                return -1;
            }
            if ((buffer[0] == 'F') && (buffer[1] == 'r') && (buffer[2] == 'o') && (buffer[3] == 'm')
                    && (buffer[4] == ' ')) {
                // then verify whole line compliance
                if (isCompliantFromLine(buffer, len)) {
                    fromLineEnd = getPointer();
                    return beg;
                }
            }
        }
    }

    /**
     * Gets the end position of the last "From " line identified.
     *
     * @return File position
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long getLastFromLineEnd() throws IOException {
        return fromLineEnd;
    }
}
