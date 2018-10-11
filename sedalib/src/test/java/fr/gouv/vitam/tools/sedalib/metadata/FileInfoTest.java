package fr.gouv.vitam.tools.sedalib.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.attribute.FileTime;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

class FileInfoTest {

	public String toSedaXmlFragments(FileInfo fi) throws SEDALibException {
		String result = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
			fi.toSedaXml(xmlWriter);
			xmlWriter.close();
			result = baos.toString("UTF-8");
			baos.close();
		} catch (SEDALibException | XMLStreamException | IOException e) {
			throw new SEDALibException("Erreur interne ->" + e.getMessage());
		}
		return result;
	}

	@Test
	void test() throws SEDALibException {
		FileInfo fi;

		fi = new FileInfo("filename", "creatingApplicationName", "creatingApplicationVersion", "creatingOs",
				"creatingOsVersion", FileTime.fromMillis(0));

		System.err.println(toSedaXmlFragments(fi));
		
		fi = new FileInfo("filename", null, null,null,null,null);

		System.err.println(toSedaXmlFragments(fi));
	}

}
