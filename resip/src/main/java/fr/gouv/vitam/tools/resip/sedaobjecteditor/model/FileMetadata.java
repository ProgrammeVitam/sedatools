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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.LocalDateTimeUtil;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.digest.DigestSha512;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads the technical metadata of the file of a BinaryDataObject, when the file is changed in the
 * editor.
 */
public final class FileMetadata {

    private FileMetadata() {}

    /**
     * Reads the technical metadata of a file: its SHA-512 digest, its size, its format identified
     * by DROID and its file information.
     *
     * @param file        the file
     * @param sedaVersion the SEDA version of the BinaryDataObject
     * @param unknown     the text standing for a format DROID doesn't identify
     * @return the MessageDigest, Size, FormatIdentification and FileInfo metadata, in this order
     * @throws SEDALibException if the digest or the format can't be computed
     * @throws IOException      if the file can't be read
     */
    public static List<SEDAMetadata> of(Path file, SedaVersion sedaVersion, String unknown)
        throws SEDALibException, IOException {
        DigestType digest = new DigestType("MessageDigest", DigestSha512.compute(file), "SHA-512");
        IntegerType size = new IntegerType("Size", Files.size(file));
        IdentificationResult ir = DroidIdentifier.getInstance().getIdentificationResult(file);
        FormatIdentification formatIdentification = ir != null
            ? new FormatIdentification(sedaVersion, ir.getName(), ir.getMimeType(), ir.getPuid(), null)
            : new FormatIdentification(sedaVersion, unknown, null, unknown, null);
        FileInfo fileInfo = new FileInfo(sedaVersion);
        fileInfo.addNewMetadata("Filename", file.getFileName().toString());
        fileInfo.addNewMetadata(
            "LastModified",
            LocalDateTimeUtil.getFormattedDateTime(Files.getLastModifiedTime(file))
        );
        return List.of(digest, size, formatIdentification, fileInfo);
    }
}
