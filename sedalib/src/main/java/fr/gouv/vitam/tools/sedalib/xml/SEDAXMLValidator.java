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
package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.xerces.util.XMLCatalogResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SEDAXMLValidator {

    private static final String SEDA_VITAM_VALIDATION_RESOURCE_2_1 = "seda2_1/seda-vitam-2.1-main.xsd";
    private static final String SEDA_VITAM_VALIDATION_RESOURCE_2_2 = "seda2_2/seda-vitam-2.2-main.xsd";
    private static final String SEDA_VITAM_VALIDATION_RESOURCE_2_3 = "seda2_3/seda-2.3-main.xsd";
    private static final String HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1 = "http://www.w3.org/XML/XMLSchema/v1.1";
    private static final String CATALOG_FILENAME = "xsd_validation/catalog.xml";
    private static final String RNG_FACTORY = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";
    private static final String RNG_PROPERTY_KEY = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;

    public static Schema getSEDASchema() throws SEDALibException {
        switch (SedaContext.getVersion()) {
            case V2_1:
                return getSchemaFromXSDResource(SEDAXMLValidator.class.getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE_2_1));
            case V2_2:
                return getSchemaFromXSDResource(SEDAXMLValidator.class.getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE_2_2));
            case V2_3:
                return getSchemaFromXSDResource(SEDAXMLValidator.class.getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE_2_3));
            default:
                throw new SEDALibException("Version [" + SedaContext.getVersion() + "] sans schéma", null);
        }
    }

    public static Schema getSchemaFromXSDResource(URL xsdResource) throws SEDALibException {
        // Was XMLConstants.W3C_XML_SCHEMA_NS_URI
        try {
            SchemaFactory factory =
                    SchemaFactory.newInstance(HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1);
            // Load catalog to resolve external schemas even offline.
            final URL catalogUrl = SEDAXMLValidator.class.getClassLoader().getResource(CATALOG_FILENAME);
            factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

            return factory.newSchema(xsdResource);
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + xsdResource, e);
        }
    }

    /**
     * Gets schema from xsd file.
     *
     * @param xsdFile the xsd file
     * @return the schema from xsd file
     * @throws SEDALibException the seda lib exception
     */
    public Schema getSchemaFromXSDFile(String xsdFile) throws SEDALibException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1);

            // Load catalog to resolve external schemas even offline.
            final URL catalogUrl = getClass().getClassLoader().getResource(CATALOG_FILENAME);
            factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

            return factory.newSchema(new File(xsdFile));
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + xsdFile, e);
        }
    }

    /**
     * Gets schema from rng file.
     *
     * @param rngFile the rng file
     * @return the schema from rng file
     * @throws SEDALibException the seda lib exception
     */
    public Schema getSchemaFromRNGFile(String rngFile) throws SEDALibException {
        System.setProperty(RNG_PROPERTY_KEY, RNG_FACTORY);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
            // Load catalog to resolve external schemas even offline.
            final URL catalogUrl = getClass().getClassLoader().getResource(CATALOG_FILENAME);
            factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

            return factory.newSchema(new File(rngFile));
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + rngFile, e);
        }
    }

    private String getContextualErrorMessage(String manifest, SAXParseException e) {
        int i = 0;
        String line = "", inArchiveUnit = "", result;

        Scanner scanner = new Scanner(manifest);
        while (scanner.hasNextLine() && (i < e.getLineNumber())) {
            line = scanner.nextLine();
            if (line.trim().startsWith("<ArchiveUnit "))
                inArchiveUnit = line.trim();
            i++;
        }
        result = "Contexte de l'erreur: " + (inArchiveUnit.isEmpty() ? "hors AU" : inArchiveUnit) + "\n" +
                "position de l'erreur identifiée: ligne " + e.getLineNumber() + ", colonne " + e.getColumnNumber() + "\n" +
                "ligne: " + line + "\n" +
                "erreur brute: " + e.getMessage();
        scanner.close();
        return result;
    }

    /**
     * Check with xsd schema.
     *
     * @param manifest  the XML manifest
     * @param xmlSchema the xml schema
     * @return true if validated
     * @throws SEDALibException the seda lib exception
     */
    public boolean checkWithXSDSchema(String manifest, Schema xmlSchema) throws SEDALibException {
        XMLInputFactory xmlInputFactory;
        XMLStreamReader xmlStreamReader = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
            xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(bais, "UTF-8");

            final Validator validator = xmlSchema.newValidator();
            validator.validate(new StAXSource(xmlStreamReader));
            return true;
        } catch (IOException e) {
            throw new SEDALibException("Erreur d'accès au flux XML", e);
        } catch (XMLStreamException e) {
            throw new SEDALibException("Impossible d'ouvrir le flux XML", e);
        } catch (SAXParseException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme\n-> "
                    + getContextualErrorMessage(manifest, e));
        } catch (SAXException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme", e);
        } finally {
            if (xmlStreamReader != null) {
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException ignored) {
                    // ignored
                }
            }
        }
    }

    /**
     * Check with rng schema.
     *
     * @param manifest  the XML manifest
     * @param rngSchema the rng schema
     * @return true if validated
     * @throws SEDALibException the seda lib exception
     */
    public boolean checkWithRNGSchema(String manifest, Schema rngSchema) throws SEDALibException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(manifest.getBytes(StandardCharsets.UTF_8))) {
            final Validator validator = rngSchema.newValidator();
            validator.validate(new StreamSource(bais));
            return true;
        } catch (SAXParseException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme\n-> "
                    + getContextualErrorMessage(manifest, e));
        } catch (SAXException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme", e);
        } catch (IOException e) {
            throw new SEDALibException("Erreur d'accès au flux XML", e);
        }
    }
}
