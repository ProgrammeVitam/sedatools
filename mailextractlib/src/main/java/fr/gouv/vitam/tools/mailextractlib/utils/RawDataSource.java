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

package fr.gouv.vitam.tools.mailextractlib.utils;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class RawDataSource for bypassing the automatic datahandler mecanism in
 * JavaMail Mime generation.
 */
public class RawDataSource implements DataSource {

    /** The input stream. */
    ByteArrayInputStream inputStream;

    /** The mime type. */
    String mimeType;

    /** The name. */
    String name;

    /**
     * Instantiates a new raw data source.
     *
     * @param rawContent
     *            the raw content
     * @param mimeType
     *            the mime type
     * @param name
     *            the name
     */
    public RawDataSource(byte[] rawContent, String mimeType, String name) {
        if (rawContent == null)
            rawContent = new byte[0];
        inputStream = new ByteArrayInputStream(rawContent);
        this.mimeType = mimeType;
        this.name = name;
    }

    /**
     * This method returns an <code>InputStream</code> representing the data and
     * throws the appropriate exception if it can not do so. Note that a new
     * <code>InputStream</code> object must be returned each time this method is
     * called, and the stream must be positioned at the beginning of the data.
     *
     * @return an InputStream
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * This method returns an <code>OutputStream</code> where the data can be
     * written and throws the appropriate exception if it can not do so. Note
     * that a new <code>OutputStream</code> object must be returned each time
     * this method is called, and the stream must be positioned at the location
     * the data is to be written.
     *
     * @return an OutputStream
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("No output on this Datasource");
    }

    /**
     * This method returns the MIME type of the data in the form of a string. It
     * should always return a valid type. It is suggested that getContentType
     * return "application/octet-stream" if the DataSource implementation can
     * not determine the data type.
     *
     * @return the MIME Type
     */
    public String getContentType() {
        return mimeType;
    }

    /**
     * Return the <i>name</i> of this object where the name of the object is
     * dependant on the nature of the underlying objects. DataSources
     * encapsulating files may choose to return the filename of the object.
     * (Typically this would be the last component of the filename, not an
     * entire pathname.)
     *
     * @return the name of the object.
     */
    public String getName() {
        return name;
    }

}
