/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata;

import java.nio.file.attribute.FileTime;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Class FileInfo.
 * <p>
 * Class for SEDA element FileInfo.
 * <p>
 * A BinaryDataObject metadata.
 * <p>
 * Standard quote: "Propriétés techniques génériques du fichier (nom d’origine,
 * logiciel de création, système d’exploitation de création)"
 */
public class FileInfo extends SEDAMetadata{

	// SEDA elements

	/** The filename. */
	public String filename;

	/** The creating application name. */
	public String creatingApplicationName;

	/** The creating application version. */
	public String creatingApplicationVersion;

	/** The creating os. */
	public String creatingOs;

	/** The creating os version. */
	public String creatingOsVersion;

	/** The last modified. */
	public FileTime lastModified;

	// Constructors

	/**
	 * Instantiates a new file info.
	 */
	public FileInfo() {
		this(null, null, null, null, null, null);
	}

	/**
	 * Instantiates a new file info.
	 *
	 * @param filename                   the filename
	 * @param creatingApplicationName    the creating application name
	 * @param creatingApplicationVersion the creating application version
	 * @param creatingOs                 the creating os
	 * @param creatingOsVersion          the creating os version
	 * @param lastModified               the last modified
	 */
	public FileInfo(String filename, String creatingApplicationName, String creatingApplicationVersion,
			String creatingOs, String creatingOsVersion, FileTime lastModified) {
		this.filename = filename;
		this.creatingApplicationName = creatingApplicationName;
		this.creatingApplicationVersion = creatingApplicationVersion;
		this.creatingOs = creatingOs;
		this.creatingOsVersion = creatingOsVersion;
		this.lastModified = lastModified;
	}

	@Override
	public String getXmlElementName() {
		return "FileInfo";
	}

	/* (non-Javadoc)
	 * @see fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toSedaXml(fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter)
	 */
	@Override
	public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
		try {
			xmlWriter.writeStartElement("FileInfo");
			xmlWriter.writeElementValueIfNotEmpty("Filename", filename);
			xmlWriter.writeElementValueIfNotEmpty("CreatingApplicationName", creatingApplicationName);
			xmlWriter.writeElementValueIfNotEmpty("CreatingApplicationVersion", creatingApplicationVersion);
			xmlWriter.writeElementValueIfNotEmpty("CreatingOs", creatingOs);
			xmlWriter.writeElementValueIfNotEmpty("CreatingOsVersion", creatingOsVersion);
			if (lastModified != null)
				xmlWriter.writeElementValueIfNotEmpty("LastModified", lastModified.toString());
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			throw new SEDALibException("Erreur d'écriture XML dans un élément FileInfo\n->" + e.getMessage());
		}
	}

	/**
	 * Import the FileInfo in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
	 * @return the read FileInfo
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected
	 */
	public static FileInfo fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
		FileInfo fi = null;
		String tmp;
		try {
			if (xmlReader.nextBlockIfNamed("FileInfo")) {
				fi = new FileInfo();
				fi.filename = xmlReader.nextMandatoryValue("Filename");
				fi.creatingApplicationName = xmlReader.nextValueIfNamed("CreatingApplicationName");
				fi.creatingApplicationVersion = xmlReader.nextValueIfNamed("CreatingApplicationVersion");
				fi.creatingOs = xmlReader.nextValueIfNamed("CreatingOs");
				fi.creatingOsVersion = xmlReader.nextValueIfNamed("CreatingOsVersion");
				tmp = xmlReader.nextValueIfNamed("LastModified");
				if (tmp != null) {
					Calendar cal = DatatypeConverter.parseDateTime(tmp);
					fi.lastModified = FileTime.fromMillis(cal.toInstant().toEpochMilli());
				}
				xmlReader.endBlockNamed("FileInfo");
			}
		} catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
			throw new SEDALibException("Erreur de lecture XML dans un élément FileInfo\n->" + e.getMessage());
		}
		return fi;
	}

	// Getters and setters

	/**
	 * Gets the last modified in long.
	 *
	 * @return the last modified in long
	 */
	@JsonGetter("lastModified")
	public long getLastModifiedInLong() {
		if (lastModified != null)
			return lastModified.toMillis();
		else
			return 0;
	}

	/**
	 * Sets the last modified from long.
	 *
	 * @param fileLastModifiedTimeLong the new last modified from long
	 */
	@JsonSetter("lastModified")
	public void setLastModifiedFromLong(long fileLastModifiedTimeLong) {
		this.lastModified = FileTime.fromMillis(fileLastModifiedTimeLong);
	}
}
