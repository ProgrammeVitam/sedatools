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
 */
package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.MetadataEditorPanel;
import fr.gouv.vitam.tools.resip.metadataeditor.composite.ComplexListTypeEditor;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The metadata editor class.
 */
abstract public class MetadataEditor {

    /**
     * The metadata.
     */
    public SEDAMetadata metadata;

    /**
     * The metadata editor father if any.
     */
    protected MetadataEditor father;

    /**
     * The metadata panel.
     */
    protected MetadataEditorPanel metadataEditorPanel;

    /**
     * The constant BASE_FONT.
     */
    public static Font BASE_FONT = UIManager.getFont("Label.font");

    /**
     * The constant LABEL_FONT.
     */
    public static Font LABEL_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize() + (float) 2.0);

    /**
     * The constant MINI_LABEL_FONT.
     */
    public static Font MINI_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getSize() - (float) 4.0);

    /**
     * The constant ITALIC_LABEL_FONT.
     */
    public static Font ITALIC_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.ITALIC);

    /**
     * The constant BOLD_LABEL_FONT.
     */
    public static Font BOLD_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.BOLD);

    /**
     * The constant EDIT_FONT.
     */
    public static Font EDIT_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize() + (float) 2.0);

    /**
     * The constant MINI_EDIT_FONT.
     */
    public static Font MINI_EDIT_FONT = EDIT_FONT.deriveFont(EDIT_FONT.getSize() - (float) 4.0);

    /**
     * The constant GENERAL_BACKGROUND.
     */
    public static Color GENERAL_BACKGROUND = UIManager.getColor("Label.background");

    /**
     * The constant GENERAL_BACKGROUND.
     */
    public static Color GENERAL_FOREGROUND = UIManager.getColor("Label.foreground");

    /**
     * The constant COMPOSITE_LABEL_COLOR.
     */
    public static Color COMPOSITE_LABEL_COLOR = new Color(
            (int) Math.min(GENERAL_FOREGROUND.getRed() + 64, 255),
            (int) Math.min(GENERAL_FOREGROUND.getGreen() + 64, 255),
            (int) Math.min(GENERAL_FOREGROUND.getBlue() + 200, 255));

    /**
     * The constant COMPOSITE_LABEL_SEPARATOR_COLOR.
     */
    public static Color COMPOSITE_LABEL_SEPARATOR_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.9),
            (int) (GENERAL_BACKGROUND.getGreen() * 0.9),
            (int) (GENERAL_BACKGROUND.getBlue() * 0.9));

    /**
     * The constant COMPOSITE_LABEL_MARKUP_COLOR.
     */
    public static Color COMPOSITE_LABEL_MARKUP_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.4),
            (int) (GENERAL_BACKGROUND.getGreen() * 0.4),
            (int) (GENERAL_BACKGROUND.getBlue() * 0.4));

    /**
     * The constant COMPOSITE_LABEL_ATTRIBUTE_COLOR.
     */
    public static Color COMPOSITE_LABEL_ATTRIBUTE_COLOR = new Color(
            (int) (GENERAL_BACKGROUND.getRed() * 0.7),
            0,
            (int) (GENERAL_BACKGROUND.getBlue() * 0.7));

    public MetadataEditor(SEDAMetadata metadata, MetadataEditor father) {
        this.metadata = metadata;
        this.father = father;
        this.metadataEditorPanel = null;
    }

    private static Class getMetadataClass(String simpleMetadataType) throws SEDALibException {
        Class result;
        String metadataType = "fr.gouv.vitam.tools.sedalib.metadata.content." + simpleMetadataType;
        try {
            result = Class.forName(metadataType);
        } catch (ClassNotFoundException e1) {
            metadataType = "fr.gouv.vitam.tools.sedalib.metadata.namedtype." + simpleMetadataType;
            try {
                result = Class.forName(metadataType);
            } catch (ClassNotFoundException e2) {
                metadataType = "fr.gouv.vitam.tools.sedalib.metadata.management." + simpleMetadataType;
                try {
                    result = Class.forName(metadataType);
                } catch (ClassNotFoundException e3) {
                    throw new SEDALibException("Le type de métadonnée [" + simpleMetadataType + "] n'est pas connu");
                }
            }
        }
        return result;
    }

    static public SEDAMetadata createMetadataSample(String simpleMetadataType, String elementName, boolean minimal) throws SEDALibException {
        SEDAMetadata result;
        try {
            String metadataEditorType = "fr.gouv.vitam.tools.resip.metadataeditor." + simpleMetadataType + "Editor";
            Class metadataEditorClass = Class.forName(metadataEditorType);
            try {
                result = (SEDAMetadata) (metadataEditorClass.getMethod("get" + (minimal ? "Minimal" : "") + "Sample", String.class).invoke(null, elementName));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new SEDALibException("La création d'un exemple de la métadonnée de type [" + simpleMetadataType + "] n'est pas possible", e);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'un exemple de la métadonnée [" + simpleMetadataType + "] a généré une erreur", te.getTargetException());
            }
        } catch (ClassNotFoundException e1) {
            Class metadataClass = getMetadataClass(simpleMetadataType);
            if (!ComplexListType.class.isAssignableFrom(metadataClass))
                throw new SEDALibException("L'éditeur de métadonnée [" + simpleMetadataType + "] n'existe pas");
            result = (SEDAMetadata) ComplexListTypeEditor.getSample(metadataClass, elementName, minimal);
        }
        return result;
    }

    static public MetadataEditor createMetadataEditor(SEDAMetadata metadata, MetadataEditor father) throws SEDALibException {
        MetadataEditor result;
        if (metadata instanceof ComplexListType)
            result = new ComplexListTypeEditor(metadata, father);
        else {
            String metadataEditorType = "fr.gouv.vitam.tools.resip.metadataeditor." + metadata.getClass().getSimpleName() + "Editor";
            Class metadataEditorClass;
            try {
                metadataEditorClass = Class.forName(metadataEditorType);
            } catch (ClassNotFoundException e) {
                throw new SEDALibException("La métadonnée de type [" + metadata.getClass().getSimpleName() + "] n'a pas d'éditeur", e);
            }
            try {
                result = (MetadataEditor) (metadataEditorClass.getConstructor(SEDAMetadata.class, MetadataEditor.class).newInstance(metadata, father));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new SEDALibException("La création d'un éditeur de la métadonnée de type [" + metadataEditorType + "] n'est pas possible", e);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'un éditeur de la métadonnée [" + metadata.toString() + "] a généré une erreur", te.getTargetException());
            }
        }
        return result;
    }

    static public MetadataEditor createMetadataEditor(String simpleMetadataType, String elementName, boolean minimal, MetadataEditor father) throws
            SEDALibException {
        return createMetadataEditor(createMetadataSample(simpleMetadataType, elementName, minimal), father);
    }

    static public SEDAMetadata getEmptySameMetadata(SEDAMetadata sedaMetadata) throws SEDALibException {
        Constructor<?> cons = null;
        try {
            if (sedaMetadata.getClass().getName().contains("namedtype")) {
                cons = sedaMetadata.getClass().getConstructor(String.class);
                sedaMetadata = (SEDAMetadata) cons.newInstance(sedaMetadata.getXmlElementName());
            }
            else {
                cons = sedaMetadata.getClass().getConstructor();
                sedaMetadata = (SEDAMetadata) cons.newInstance();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new SEDALibException("Pas de constructeur vide pour la métadonnée de type [" + sedaMetadata.getClass() + "]", e);
        } catch (InvocationTargetException e) {
            throw new SEDALibException("Erreur durant la création de la métadonnée vide de type [" + sedaMetadata.getClass() + "]", e.getTargetException());
        }
        return sedaMetadata;
    }

    abstract public SEDAMetadata extractMetadata() throws SEDALibException;

    public String getSummary() throws SEDALibException {
        return "";
    }

    static public String getExtraInformation(SEDAMetadata sedaMetadata) {
        return MetadataEditorConstants.typeExtraInformationMap.get(sedaMetadata.getXmlElementName());
    }

    public static String translate(String tag) {
        String result = MetadataEditorConstants.translateMap.get(tag);
        if (result == null)
            return tag;
        return result;
    }

    abstract public void createMetadataEditorPanel() throws SEDALibException;

    static public SEDAMetadata getSample(String elementName) throws SEDALibException {
        throw new SEDALibException("Métadonnée non implémentée pour [" + elementName + "]");
    }

    static public SEDAMetadata getMinimalSample(String elementName) throws SEDALibException {
        throw new SEDALibException("Métadonnée non implémentée pour [" + elementName + "]");
    }

    public MetadataEditorPanel getMetadataEditorPanel() throws SEDALibException {
        if (metadataEditorPanel == null)
            createMetadataEditorPanel();
        return metadataEditorPanel;
    }

    public boolean isMultiple() throws SEDALibException {
        if (father == null)
            return false;
        if (((ComplexListType) (father.metadata)).getMetadataMap().get(metadata.getXmlElementName()) == null)
            return true;
        return ((ComplexListType) (father.metadata)).getMetadataMap().get(metadata.getXmlElementName()).many;
    }

    /**
     * Gets father.
     *
     * @return the father
     */
    public MetadataEditor getFather() {
        return father;
    }

    /**
     * Gets metadata.
     *
     * @return the metadata
     */
    public SEDAMetadata getMetadata() {
        return metadata;
    }

    public Container getMetadataEditorPanelTopParent() {
        if (father == null)
            return metadataEditorPanel.getParent();
        return father.getMetadataEditorPanelTopParent();
    }
}
