package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
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

    private static final String SEDA_VITAM_VALIDATION_RESOURCE_2_1 = "seda-vitam-2.1-main.xsd";
    private static final String SEDA_VITAM_VALIDATION_RESOURCE_2_2 = "seda-2.2-main.xsd";
    private static final String HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1 = "http://www.w3.org/XML/XMLSchema/v1.1";
    private static final String CATALOG_FILENAME = "xsd_validation/catalog.xml";
    private static final String RNG_FACTORY = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";
    private static final String RNG_PROPERTY_KEY = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;

    private static Schema sedaSchema = null;
    private static int sedaVersion = 0;

    public static Schema getSEDASchema() throws SEDALibException {
        if ((sedaSchema == null) || (sedaVersion != SEDA2Version.getSeda2Version())) {
            switch (SEDA2Version.getSeda2Version()) {
                case 1:
                    sedaSchema = getSchemaFromXSDResource(SEDAXMLValidator.class.getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE_2_1));
                    break;
                case 2:
                    sedaSchema = getSchemaFromXSDResource(SEDAXMLValidator.class.getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE_2_2));
                    break;
                default:
                    throw new SEDALibException("Version du SEDA [" + SEDA2Version.getSeda2VersionString() + "] sans schéma", null);
            }
            sedaVersion = SEDA2Version.getSeda2Version();
        }
        return sedaSchema;
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
