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
package fr.gouv.vitam.tools.resip.metadataeditor.composite;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.CompositeEditorPanel;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The BinaryDataObject metadata editor class.
 */
public class BinaryDataObjectEditor extends CompositeEditor {

    /**
     * Instantiates a new BinaryDataObject editor.
     *
     * @param metadata the BinaryDataObject metadata
     * @param father   the father
     */
    public BinaryDataObjectEditor(BinaryDataObject metadata, MetadataEditor father) {
        super(metadata, father);
    }

    private BinaryDataObject getBinaryDataObjectMetadata() {
        return (BinaryDataObject) metadata;
    }

    @Override
    public String getName(){
        return translateMetadataName("BinaryDataObject")+" - "+(metadata==null? translateMetadataName("Unknown"):getBinaryDataObjectMetadata().getInDataObjectPackageId());
    }

    @Override
    public BinaryDataObject extractEditedObject() throws SEDALibException {
        BinaryDataObject tmpBdo=new BinaryDataObject();
        for (MetadataEditor metadataEditor : metadataEditorList) {
            SEDAMetadata subMetadata=(SEDAMetadata)metadataEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()){
                case "DataObjectVersion":
                    tmpBdo.dataObjectVersion=(StringType)subMetadata;
                    break;
                case "Uri":
                    tmpBdo.uri=(StringType)subMetadata;
                    break;
                case "MessageDigest":
                    tmpBdo.messageDigest=(DigestType) subMetadata;
                    break;
                case "Size":
                    tmpBdo.size=(IntegerType) subMetadata;
                    break;
                case "Compressed":
                    tmpBdo.compressed=(StringType)subMetadata;
                    break;
                case "FormatIdentification":
                    tmpBdo.formatIdentification=(FormatIdentification) subMetadata;
                    break;
                case "FileInfo":
                    tmpBdo.fileInfo=(FileInfo) subMetadata;
                    break;
                case "Metadata":
                    tmpBdo.metadata=(Metadata) subMetadata;
                    break;
            }
        }
        getBinaryDataObjectMetadata().dataObjectVersion=tmpBdo.dataObjectVersion;
        getBinaryDataObjectMetadata().uri=tmpBdo.uri;
        getBinaryDataObjectMetadata().messageDigest=tmpBdo.messageDigest;
        getBinaryDataObjectMetadata().size=tmpBdo.size;
        getBinaryDataObjectMetadata().compressed=tmpBdo.compressed;
        getBinaryDataObjectMetadata().formatIdentification=tmpBdo.formatIdentification;
        getBinaryDataObjectMetadata().fileInfo=tmpBdo.fileInfo;
        getBinaryDataObjectMetadata().metadata=tmpBdo.metadata;

        return getBinaryDataObjectMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(metadataEditorList.size());
        String tmp;

        if (getBinaryDataObjectMetadata().dataObjectVersion!=null)
            summaryList.add(getBinaryDataObjectMetadata().dataObjectVersion.getValue());
        else
            summaryList.add(translateMetadataName("Unknown"));

        if (getBinaryDataObjectMetadata().fileInfo!=null) {
            tmp=getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("Filename");
            if (tmp==null)
                tmp= translateMetadataName("Unknown");
        }
        else
            tmp= translateMetadataName("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification!=null) {
            tmp=getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("MimeType");
            if (tmp==null)
                tmp= translateMetadataName("Unknown");
        }
        else
            tmp= translateMetadataName("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().formatIdentification!=null) {
            tmp=getBinaryDataObjectMetadata().formatIdentification.getSimpleMetadata("FormatId");
            if (tmp==null)
                tmp= translateMetadataName("Unknown");
        }
        else
            tmp= translateMetadataName("Unknown");
        summaryList.add(tmp);

        if (getBinaryDataObjectMetadata().fileInfo!=null) {
            tmp=getBinaryDataObjectMetadata().fileInfo.getSimpleMetadata("LastModified");
            if (tmp==null)
                tmp= translateMetadataName("Unknown");
        }
        else
            tmp= translateMetadataName("Unknown");
        summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    @Override
    public void createMetadataEditorPanel() throws SEDALibException {
        this.metadataEditorList = new ArrayList<MetadataEditor>();

        JButton openButton=new JButton();
        openButton.setIcon(new ImageIcon(getClass().getResource("/icon/folder-open.png")));
        openButton.setText("");
        openButton.setMaximumSize(new Dimension(16, 16));
        openButton.setMinimumSize(new Dimension(16, 16));
        openButton.setPreferredSize(new Dimension(16, 16));
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        openButton.setFocusPainted(false);
        openButton.setFocusable(false);
        openButton.addActionListener(arg -> openButton(getBinaryDataObjectMetadata().getOnDiskPath()));

        this.metadataEditorPanel = new CompositeEditorPanel(this, openButton, false);
        if (getBinaryDataObjectMetadata().dataObjectVersion!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().dataObjectVersion, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(0,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().uri!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().uri, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(1,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().messageDigest!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().messageDigest, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(2,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().size!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().size, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(3,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().compressed!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().compressed, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(4,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().formatIdentification!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().formatIdentification, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(5,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().fileInfo!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().fileInfo, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(6,metadataEditor.getMetadataEditorPanel());
        }
        if (getBinaryDataObjectMetadata().metadata!=null) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getBinaryDataObjectMetadata().metadata, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(7,metadataEditor.getMetadataEditorPanel());
        }
    }

    static private void openButton(Path path){
        System.out.println(path);
    }

    @Override
    public List<Pair<String,String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("DataObjectVersion", translateMetadataName("DataObjectVersion")),
                Pair.of("Uri", translateMetadataName("Uri")),
                Pair.of("MessageDigest", translateMetadataName("MessageDigest")),
                Pair.of("Size", translateMetadataName("Size")),
                Pair.of("FormatIdentification", translateMetadataName("FormatIdentification")),
                Pair.of("FileInfo", translateMetadataName("FileInfo")),
                Pair.of("Metadata", translateMetadataName("Metadata"))));

        for (MetadataEditor me : metadataEditorList) {
            String name=me.getName();
            extensionList.remove(Pair.of(name, translateMetadataName(name)));
        }
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata=null;
        int insertionIndex=0;
        switch(metadataName){
            case "DataObjectVersion":
                sedaMetadata = createSEDAMetadataSample("StringType", "DataObjectVersion", true);
                insertionIndex=0;
                break;
            case "Uri":
                sedaMetadata = createSEDAMetadataSample("StringType", "Uri", true);
                insertionIndex=1;
                break;
            case "MessageDigest":
                sedaMetadata = createSEDAMetadataSample("DigestType", "MessageDigest", true);
                insertionIndex=2;
                break;
            case "Size":
                sedaMetadata = createSEDAMetadataSample("IntegerType", "Size", true);
                insertionIndex=3;
                break;
            case "Compressed":
                sedaMetadata = createSEDAMetadataSample("StringType", "Compressed", true);
                insertionIndex=4;
                break;
            case "FormatIdentification":
                sedaMetadata = createSEDAMetadataSample("FormatIdentification", "FormatIdentification", true);
                insertionIndex=5;
                break;
            case "FileInfo":
                sedaMetadata = createSEDAMetadataSample("FileInfo", "FileInfo", true);
                insertionIndex=6;
                break;
            case "Metadata":
                sedaMetadata = createSEDAMetadataSample("Metadata", "Metadata", true);
                insertionIndex=7;
                break;
        }
        if (sedaMetadata==null)
            throw new SEDALibException("La métadonnée ["+metadataName+"] n'existe pas dans un BinaryDataObject");
        MetadataEditor addedMetadataEditor = createMetadataEditor(sedaMetadata, this);
        metadataEditorList.add(insertionIndex,
                addedMetadataEditor);
        ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(insertionIndex,addedMetadataEditor.getMetadataEditorPanel());
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return false;
    }
}
