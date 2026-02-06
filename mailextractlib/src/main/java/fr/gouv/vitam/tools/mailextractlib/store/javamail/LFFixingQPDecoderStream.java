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
package fr.gouv.vitam.tools.mailextractlib.store.javamail;

import org.eclipse.angus.mail.util.ASCIIUtility;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * LFFixingQPDecoderStream is an input stream filter decoding quoted-printable stream,
 * but considering that LF alone in content are CRLF. This is especially to correct encoding in TNEF.
 */
public class LFFixingQPDecoderStream extends FilterInputStream {
    /**
     * The back buffer.
     */
    protected byte[] ba = new byte[2];
    /**
     * The spaces count.
     */
    protected int spaces = 0;
    /**
     * The CR character written flag.
     */
    protected boolean crWritten = false;

    /**
     * Instantiates a new Lf fixing qp decoder stream.
     *
     * @param in the in
     */
    public LFFixingQPDecoderStream(InputStream in) {
        super(new PushbackInputStream(in, 2));
    }

    public int read() throws IOException {
        if (this.spaces > 0) {
            --this.spaces;
            return 32;
        } else {
            int c = this.in.read();
            if (c != 32) {
                if ((c == 10) && !crWritten) {
                    ((PushbackInputStream) this.in).unread(c);
                    crWritten = true;
                    return 13;
                } else if (c == 13) {
                    crWritten = true;
                    return 13;
                } else {
                    crWritten = false;
                    if (c == 61) {
                        int a = this.in.read();
                        if (a == 10) {
                            return this.read();
                        } else if (a == 13) {
                            int b = this.in.read();
                            if (b != 10) {
                                ((PushbackInputStream) this.in).unread(b);
                            }

                            return this.read();
                        } else if (a == -1) {
                            return -1;
                        } else {
                            this.ba[0] = (byte) a;
                            this.ba[1] = (byte) this.in.read();

                            try {
                                return ASCIIUtility.parseInt(this.ba, 0, 2, 16);
                            } catch (NumberFormatException var4) {
                                ((PushbackInputStream) this.in).unread(this.ba);
                                return c;
                            }
                        }
                    } else {
                        return c;
                    }
                }
            } else {
                while ((c = this.in.read()) == 32) {
                    ++this.spaces;
                }

                if (c != 13 && c != 10 && c != -1) {
                    ((PushbackInputStream) this.in).unread(c);
                    c = 32;
                } else {
                    this.spaces = 0;
                }

                return c;
            }
        }
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        int i;
        for (i = 0; i < len; ++i) {
            int c;
            if ((c = this.read()) == -1) {
                if (i == 0) {
                    i = -1;
                }
                break;
            }

            buf[off + i] = (byte) c;
        }

        return i;
    }

    public long skip(long n) throws IOException {
        long skipped;
        for (skipped = 0L; n-- > 0L && this.read() >= 0; ++skipped) {
        }

        return skipped;
    }

    public boolean markSupported() {
        return false;
    }

    public int available() throws IOException {
        return this.in.available();
    }
}
