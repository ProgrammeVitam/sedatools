/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
 **/
package fr.gouv.vitam.tools.sedalib.xml;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

/**
 * The Class IndentXMLTool.
 * <p>
 * Utility class used to indent any XML string representing XML fragments (multi
 * root) without reallocating all parsing objects each time.
 */
public class IndentXMLTool {

    /**
     * The Constant STANDARD_INDENT used by default for indent number of spaces.
     */
    public static final int STANDARD_INDENT = 2;

    /**
     * The DocumentBuilderFactory.
     */
    DocumentBuilderFactory dbf;

    /**
     * The TransformerFactory.
     */
    TransformerFactory tf;

    /**
     * The app transformer.
     */
    Transformer appTransformer;

    /**
     * The XPathFactory.
     */
    XPathFactory xpf;

    /**
     * The indent length.
     */
    int indentLength;

    /**
     * The indent element.
     */
    String indentElement;

    /**
     * The instance.
     */
    static IndentXMLTool instance;

    /**
     * Instantiates a new indent XML tool.
     *
     * @param indentLength the indent length
     */
    private IndentXMLTool(int indentLength) {
        dbf = DocumentBuilderFactory.newInstance();
        tf = TransformerFactory.newInstance();
        tf.setAttribute("indent-number", 2);
        try {
            appTransformer = tf.newTransformer();
            appTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            appTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            if (indentLength > 0) {
                appTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
                appTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                        Integer.toString(indentLength));
            }
        } catch (Exception e) {
            appTransformer = null;
        }
        xpf = XPathFactory.newInstance();
        this.indentLength = indentLength;
        this.indentElement = StringUtils.repeat(" ", indentLength);
    }

    /**
     * Gets the single instance of IndentXMLTool convenient for a given indent
     * length.
     *
     * @param indentLength the indent length
     * @return single instance of IndentXMLTool
     */
    public static IndentXMLTool getInstance(int indentLength) {
        if ((instance == null) || (instance.indentLength != indentLength))
            instance = new IndentXMLTool(indentLength);
        return instance;
    }

    /**
     * Indent string.
     *
     * @param xml the xml
     * @return the string in xml indented form
     * @throws SEDALibException if XML is badly formed
     */
    public String indentString(String xml) throws SEDALibException {
        String result;

        xml=xml.trim();
        if (xml.isEmpty())
            return "";

        if (appTransformer == null)
            return xml;

        DocumentBuilder appDocumentBuilder;
        try {
            appDocumentBuilder = dbf.newDocumentBuilder(); // NOSONAR no Doctype risk as all is encapsulated in a <INDENT> tag
            Document doc = appDocumentBuilder.parse(new InputSource(new StringReader("<INDENT>" + xml + "</INDENT>")));
            XPath xPath = xpf.newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            appTransformer.transform(new DOMSource(doc), streamResult);

            result = stringWriter.toString();
            result = result.substring(0, result.lastIndexOf("</INDENT>") - 1).substring(9);
            // trim beginning CR
            while (result.startsWith("\n"))
                result = result.substring(1);

            StringBuilder sb = new StringBuilder();
            String tmp;
            try (Scanner s = new Scanner(result)) {
                while (s.hasNextLine()) {
                    tmp = s.nextLine();
                    if (tmp.startsWith(indentElement) && tmp.trim().startsWith("<"))
                        tmp = tmp.substring(indentLength);
                    sb.append(tmp).append('\n');
                }
            }
            if (sb.length() > 1)
                sb.setLength(sb.length() - 1);
            return sb.toString();
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException e) {
            throw new SEDALibException("XML mal formé", e);
        }
    }
}
