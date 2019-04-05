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
package fr.gouv.vitam.tools.resip.app;

import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.frame.TechnicalSearchDialog;
import fr.gouv.vitam.tools.sedalib.core.*;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class TechnicalSearchThread extends SwingWorker<String, String> {

    private TechnicalSearchDialog technicalSearchDialog;
    private ArchiveUnit searchUnit;
    private DataObjectPackage dataObjectPackage;
    private LinkedHashMap<ArchiveUnit,List<BinaryDataObject>> searchResult;
    private List<String> formats;
    private boolean searchOthers;
    private List<String> otherFormats;
    private long min;
    private long max;
    private boolean allFormatsFlag;


    public TechnicalSearchThread(TechnicalSearchDialog technicalSearchDialog, ArchiveUnit au, List<String> formats,long min, long max) {
        this.technicalSearchDialog = technicalSearchDialog;
        this.searchUnit = au;
        this.formats = formats;
        if (formats.contains("Other")) {
            this.searchOthers=true;
            this.otherFormats =
                    ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap().
                            entrySet().
                            stream().
                            flatMap(e -> e.getValue().stream()).
                            collect(Collectors.toList());
            this.formats.remove("Other");
        }
        else {
            this.searchOthers = false;
            this.otherFormats = null;
        }
        this.allFormatsFlag=(formats.size()==0);
        this.min=min;
        this.max=max;
    }

    private void addBinaryDataObject(ArchiveUnit au, BinaryDataObject bdo){
        List<BinaryDataObject> bdos=searchResult.get(au);
        if (bdos==null)
            bdos=new ArrayList<BinaryDataObject>();
        bdos.add(bdo);
        searchResult.put(au,bdos);
    }

    private boolean testBinaryDataObject(BinaryDataObject bdo) {
        if ((bdo.size<min) || (bdo.size>max))
            return false;
        if (allFormatsFlag)
            return true;
        if (formats.contains(bdo.formatIdentification.formatId))
            return true;
        if (searchOthers && !otherFormats.contains(bdo.formatIdentification.formatId))
            return true;
        return false;
    }

    private void searchInArchiveUnit(ArchiveUnit au) {
        List<ArchiveUnit> auList = au.getChildrenAuList().getArchiveUnitList();

        for (ArchiveUnit childUnit : auList) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId()))
                continue;
            for (DataObject dataObject : childUnit.getDataObjectRefList().getDataObjectList()) {
                if (dataObject instanceof PhysicalDataObject)
                    continue;
                else if (dataObject instanceof BinaryDataObject) {
                    BinaryDataObject bdo = (BinaryDataObject) dataObject;
                    if (testBinaryDataObject(bdo))
                        addBinaryDataObject(childUnit,bdo);

                } else if (dataObject instanceof DataObjectGroup){
                    for (BinaryDataObject bdo:((DataObjectGroup) dataObject).getBinaryDataObjectList()){
                        if (testBinaryDataObject(bdo))
                            addBinaryDataObject(childUnit,bdo);
                    }
                }
            }
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());

            searchInArchiveUnit(childUnit);
        }
    }

    @Override
    public String doInBackground() {
        MainWindow mainWindow = (MainWindow) technicalSearchDialog.getParent();
        dataObjectPackage = mainWindow.getApp().currentWork.getDataObjectPackage();
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        searchResult = new LinkedHashMap<ArchiveUnit,List<BinaryDataObject>>();

        searchInArchiveUnit(searchUnit);
        return "OK";
    }

    @Override
    protected void done() {
        technicalSearchDialog.setSearchResult(searchResult);
    }
}
