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

package fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * A redirecting Charset that delegates encoding and decoding operations to an underlying charset.
 * <p>
 * This class extends {@link java.nio.charset.Charset} and wraps another charset specified by its name.
 * All operations such as {@code newDecoder()}, {@code newEncoder()} and {@code contains()} are delegated to the underlying charset.
 * </p>
 */
public class RedirectCharset extends Charset {

    private final Charset delegate;

    /**
     * Constructs a new RedirectCharset.
     *
     * @param canonicalName the canonical name of this charset.
     * @param aliases       an array of aliases for this charset.
     * @param delegateName  the name of the charset to which operations will be delegated.
     * @throws IllegalArgumentException if the delegate charset is not available.
     */
    public RedirectCharset(String canonicalName, String[] aliases, String delegateName) {
        super(canonicalName, aliases);
        this.delegate = Charset.forName(delegateName);
    }

    /**
     * Tells whether or not this charset contains the given charset.
     * This method delegates the call to the underlying charset.
     *
     * @param cs the given charset.
     * @return {@code true} if, and only if, this charset contains the given charset.
     */
    @Override
    public boolean contains(Charset cs) {
        return delegate.contains(cs);
    }

    /**
     * Creates a new decoder for this charset.
     * This method delegates the call to the underlying charset.
     *
     * @return a new {@link CharsetDecoder} instance.
     */
    @Override
    public CharsetDecoder newDecoder() {
        return delegate.newDecoder();
    }

    /**
     * Creates a new encoder for this charset.
     * This method delegates the call to the underlying charset.
     *
     * @return a new {@link CharsetEncoder} instance.
     */
    @Override
    public CharsetEncoder newEncoder() {
        return delegate.newEncoder();
    }
}