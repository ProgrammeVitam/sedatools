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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.TreeDataObjectPackageEditorPanel;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Search thread.
 */
public class SearchThread extends SwingWorker<String, String> {

    // input
    private ArchiveUnit searchUnit;
    private boolean isWithoutChildArchiveUnitCheck;
    private boolean isWithoutDataObjectGroupCheck;
    private boolean isIdCheck;
    private boolean isMetadataCheck;
    private boolean isRegExpCheck;
    private boolean isCaseCheck;
    private String searchText;
    private Consumer<List<ArchiveUnit>> callBack;

    // treatment
    private String searchExp;
    private DataObjectPackage dataObjectPackage;
    private Pattern searchPattern;
    private List<ArchiveUnit> searchResult;


    /**
     * Instantiates a new Search thread.
     *
     * @param au the au
     */
    public SearchThread(ArchiveUnit au,
                        boolean isWithoutChildArchiveUnitCheck, boolean isWithoutDataObjectGroupCheck,
                        boolean isIdCheck, boolean isMetadataCheck, boolean isRegExpCheck, boolean isCaseCheck,
                        String searchText,
                        Consumer<List<ArchiveUnit>> callBack) {
        this.searchUnit = au;
        this.isWithoutChildArchiveUnitCheck = isWithoutChildArchiveUnitCheck;
        this.isWithoutDataObjectGroupCheck = isWithoutDataObjectGroupCheck;
        this.isIdCheck = isIdCheck;
        this.isMetadataCheck = isMetadataCheck;
        this.isRegExpCheck = isRegExpCheck;
        this.isCaseCheck = isCaseCheck;
        this.searchText = searchText;
        this.callBack = callBack;
    }

    /**
     * Search in archive unit.
     *
     * @param au the au
     */
    void searchInArchiveUnit(ArchiveUnit au) {
        List<ArchiveUnit> auList = au.getChildrenAuList().getArchiveUnitList();
        TreeDataObjectPackageEditorPanel treePane = ResipGraphicApp.getTheWindow().treePane;

        for (ArchiveUnit childUnit : auList) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId()))
                continue;
            try {
                String tmp;
                int dataObjectCount = 0;
                if (childUnit.getTheDataObjectGroup() != null) {
                    DataObjectGroup dataObjectGroup = childUnit.getTheDataObjectGroup();
                    dataObjectCount = dataObjectGroup.getBinaryDataObjectList().size() + dataObjectGroup.getPhysicalDataObjectList().size();
                }
                if (!(isWithoutChildArchiveUnitCheck && (childUnit.getChildrenAuList().getCount() != 0)) &&
                        !(isWithoutDataObjectGroupCheck && (dataObjectCount != 0))) {
                    if (isIdCheck) {
                        tmp = "<" + childUnit.getInDataObjectPackageId() + ">";
                        for (DataObject dataObject : childUnit.getDataObjectRefList().getDataObjectList()) {
                            tmp += "<" + dataObject.getInDataObjectPackageId() + ">";
                            if (dataObject instanceof DataObjectGroup) {
                                for (BinaryDataObject bo : ((DataObjectGroup) dataObject).getBinaryDataObjectList())
                                    tmp += "<" + bo.getInDataObjectPackageId() + ">";
                                for (PhysicalDataObject po : ((DataObjectGroup) dataObject).getPhysicalDataObjectList())
                                    tmp += "<" + po.getInDataObjectPackageId() + ">";
                            }
                        }
                    } else if (isMetadataCheck) {
                        tmp = childUnit.getContent().toString();
                    } else
                        tmp = treePane.getTreeTitle(childUnit);

                    if (isRegExpCheck) {
                        Matcher matcher = searchPattern.matcher(tmp);
                        if (matcher.matches())
                            searchResult.add(childUnit);
                    } else {
                        if (!isCaseCheck) tmp = tmp.toLowerCase();
                        if (tmp.contains(searchExp))
                            searchResult.add(childUnit);
                    }
                }
                dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            } catch (SEDALibException ignored) {
            }
            searchInArchiveUnit(childUnit);
        }
    }

    @Override
    public String doInBackground() {
        searchExp = searchText;
        if (isRegExpCheck) searchPattern = Pattern.compile("[\\S\\s]*" + searchExp + "[\\S\\s]*");
        else if (!isCaseCheck) searchExp = searchExp.toLowerCase();
        if (isIdCheck) searchExp = "<" + searchExp + ">";
        dataObjectPackage=searchUnit.getDataObjectPackage();
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        searchResult = new LinkedList<ArchiveUnit>();

        searchInArchiveUnit(searchUnit);
        return "OK";
    }

    @Override
    protected void done() {
        callBack.accept(searchResult);
    }
}
