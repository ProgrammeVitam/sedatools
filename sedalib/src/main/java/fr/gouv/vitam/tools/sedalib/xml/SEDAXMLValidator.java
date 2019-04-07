package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.util.XMLCatalogResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SEDAXMLValidator {

    private static final String SEDA_VITAM_VALIDATION_RESOURCE = "seda-vitam-2.1-main.xsd";
    private static final String HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1 = "http://www.w3.org/XML/XMLSchema/v1.1";
    private static final String CATALOG_FILENAME = "xsd_validation/catalog.xml";
    private static final String RNG_FACTORY = "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory";
    private static final String RNG_PROPERTY_KEY = "javax.xml.validation.SchemaFactory:" + XMLConstants.RELAXNG_NS_URI;

    private static Schema sedaSchema = null;

    public Schema getSEDASchema() throws SEDALibException {
        if (sedaSchema == null)
            sedaSchema = getSchemaFromXSDResource(getClass().getClassLoader().getResource(SEDA_VITAM_VALIDATION_RESOURCE));
        return sedaSchema;
    }

    public Schema getSchemaFromXSDResource(URL xsdResource) throws SEDALibException {
        // Was XMLConstants.W3C_XML_SCHEMA_NS_URI
        final SchemaFactory factory =
                SchemaFactory.newInstance(HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1);
        // Load catalog to resolve external schemas even offline.
        final URL catalogUrl = getClass().getClassLoader().getResource(CATALOG_FILENAME);
        factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

        try {
            return factory.newSchema(xsdResource);
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + xsdResource);
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
        SchemaFactory factory = SchemaFactory.newInstance(HTTP_WWW_W3_ORG_XML_XML_SCHEMA_V1_1);

        // Load catalog to resolve external schemas even offline.
        final URL catalogUrl = getClass().getClassLoader().getResource(CATALOG_FILENAME);
        factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

        try {
            return factory.newSchema(new File(xsdFile));
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + xsdFile);
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
        SchemaFactory factory  = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);

        // Load catalog to resolve external schemas even offline.
        final URL catalogUrl = getClass().getClassLoader().getResource(CATALOG_FILENAME);
        factory.setResourceResolver(new XMLCatalogResolver(new String[]{catalogUrl.toString()}, false));

        try {
            return factory.newSchema(new File(rngFile));
        } catch (SAXException e) {
            throw new SEDALibException("Impossible de charger le schéma " + rngFile);
        }
    }

    /**
     * Check with xsd schema.
     *
     * @param xmlFile   the xml file
     * @param xmlSchema the xml schema
     * @return true if validated
     * @throws SEDALibException the seda lib exception
     */
    public boolean checkWithXSDSchema(InputStream xmlFile, Schema xmlSchema) throws SEDALibException {
        XMLInputFactory xmlInputFactory;
        XMLStreamReader xmlStreamReader=null;
        try {
            xmlInputFactory = XMLInputFactory.newInstance();
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlFile, "UTF-8");

            final Validator validator = xmlSchema.newValidator();
            validator.validate(new StAXSource(xmlStreamReader));
            return true;
        } catch (IOException e) {
            throw new SEDALibException("Erreur d'accès au flux XML\n->"+e.getMessage());
        } catch (XMLStreamException e) {
            throw new SEDALibException("Impossible d'ouvrir le flux XML\n->"+e.getMessage());
        } catch (SAXException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme\n->"+e.getMessage());
        } finally {
            if (xmlStreamReader!=null) {
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException ignored) {
                }
            }
            IOUtils.closeQuietly(xmlFile);
        }
    }

    /**
     * Check with rng schema.
     *
     * @param xmlFile   the xml file
     * @param rngSchema the rng schema
     * @return true if validated
     * @throws SEDALibException the seda lib exception
     */
    public boolean checkWithRNGSchema(InputStream xmlFile, Schema rngSchema) throws SEDALibException {
        try {
            final Validator validator = rngSchema.newValidator();
            validator.validate(new StreamSource(xmlFile));
            return true;
        } catch (SAXException e) {
            throw new SEDALibException("Le flux XML n'est pas conforme\n->"+e.getMessage());
        } catch (IOException e) {
            throw new SEDALibException("Erreur d'accès au flux XML\n->"+e.getMessage());
        } finally {
            IOUtils.closeQuietly(xmlFile);
        }
    }
}
