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
package fr.gouv.vitam.tools.mailextractlib.formattools;

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import org.apache.tika.Tika;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for the text extraction tool. It uses Tika library.
 */
public class TikaExtractor {

    /** Singleton instance **/
    private static TikaExtractor INSTANCE = null;

    /** Tika object **/
    private Tika tika;

    /** Private constructor */
    public TikaExtractor() {
        Level memLevel;
        Logger logger;

        logger = Logger.getGlobal();
        memLevel = logger.getLevel();
        if (memLevel != Level.FINEST)
            logger.setLevel(Level.OFF);
        tika = new Tika();
        logger.setLevel(memLevel);
    }

    /**
     * Get the FileTextExtractor singleton.
     *
     * @return single instance of FileTextExtractor
     */
    public static TikaExtractor getInstance() {
        if (INSTANCE==null)
            INSTANCE=new TikaExtractor();
        return INSTANCE;
    }

    /**
     * Gets the text form of the file raw content.
     *
     * @param rawContent
     *            the raw content
     * @return the text String
     * @throws MailExtractLibException
     *             if text extract was not possible
     */
    public String extractTextFromBinary(byte[] rawContent) throws MailExtractLibException {
        String s = null;
        Level memLevel;
        Logger logger;

        logger = Logger.getGlobal();
        memLevel = logger.getLevel();
        if (memLevel != Level.FINEST)
            logger.setLevel(Level.OFF);
        try {
            if (rawContent.length > 0)
                s = tika.parseToString(new ByteArrayInputStream(rawContent));
        } catch (Throwable e) {
            throw new MailExtractLibException("mailextractlib.formattools: can't extract text content", e);
        } finally {
            logger.setLevel(memLevel);
        }

        return s;
    }

    /**
     * Gets the mime type of the file raw content.
     *
     * @param rawContent
     *            the raw content
     * @return the mime type
     * @throws MailExtractLibException
     *             if text extract was not possible
     */
    public String getMimeType(byte[] rawContent) throws MailExtractLibException {
        String result = null;
        Level memLevel;
        Logger logger;

        if (rawContent.length > 0) {
            logger = Logger.getGlobal();
            memLevel = logger.getLevel();
            if (memLevel != Level.FINEST)
                logger.setLevel(Level.OFF);
            try {
                result = tika.detect(rawContent);
            } catch (Exception e) {
                // if any problem in identification tools, default mimetype
                result = "application/octet-stream";
            }
            logger.setLevel(memLevel);
        }
        return result;
    }

}
