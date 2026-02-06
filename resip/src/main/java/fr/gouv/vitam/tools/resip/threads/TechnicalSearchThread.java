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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The type Technical search thread.
 */
public class TechnicalSearchThread extends SwingWorker<String, String> {

    // input
    private ArchiveUnit searchUnit;
    private List<String> formats;
    private long min;
    private long max;
    private Consumer<LinkedHashMap<ArchiveUnit, List<BinaryDataObject>>> callBack;

    // treatment
    private DataObjectPackage dataObjectPackage;
    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchDataObjectResult;
    private boolean searchOthers;
    private List<String> otherFormats;
    private boolean allFormatsFlag;


    /**
     * Instantiates a new Technical search thread.
     *
     * @param au                    the au
     * @param formats               the formats
     * @param min                   the min
     * @param max                   the max
     */
    public TechnicalSearchThread(ArchiveUnit au, List<String> formats, long min, long max,
                                 Consumer<LinkedHashMap<ArchiveUnit, List<BinaryDataObject>>> callBack) {
        this.searchUnit = au;
        this.formats = formats;
        if (formats.contains("Other")) {
            this.searchOthers = true;
            this.otherFormats =
                    ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap().
                            entrySet().
                            stream().
                            flatMap(e -> e.getValue().stream()).
                            collect(Collectors.toList());
            this.formats.remove("Other");
        } else {
            this.searchOthers = false;
            this.otherFormats = null;
        }
        this.allFormatsFlag = ((formats.size() == 0) && !this.searchOthers);
        this.min = min;
        this.max = max;
        this.callBack=callBack;
    }

    private void addBinaryDataObject(ArchiveUnit au, BinaryDataObject bdo) {
        List<BinaryDataObject> bdos = searchDataObjectResult.get(au);
        if (bdos == null)
            bdos = new ArrayList<BinaryDataObject>();
        bdos.add(bdo);
        searchDataObjectResult.put(au, bdos);
    }

    private boolean testBinaryDataObject(BinaryDataObject bdo) {
        IntegerType size=bdo.getMetadataSize();
        if ((size == null) && ((min != 0) || (max != Long.MAX_VALUE)))
            return false;
        if (((min != 0) || (max != Long.MAX_VALUE)) && (size.getValue() < min) || (size.getValue() > max))
            return false;
        if (allFormatsFlag)
            return true;
        FormatIdentification formatIdentification=bdo.getMetadataFormatIdentification();
        if (formatIdentification==null)
            return false;
        if (formats.contains(formatIdentification.getSimpleMetadata("FormatId")))
            return true;
        if (searchOthers && !otherFormats.contains(formatIdentification.getSimpleMetadata("FormatId")))
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
                        addBinaryDataObject(childUnit, bdo);

                } else if (dataObject instanceof DataObjectGroup) {
                    for (BinaryDataObject bdo : ((DataObjectGroup) dataObject).getBinaryDataObjectList()) {
                        if (testBinaryDataObject(bdo))
                            addBinaryDataObject(childUnit, bdo);
                    }
                }
            }
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());

            searchInArchiveUnit(childUnit);
        }
    }

    @Override
    public String doInBackground() {
        dataObjectPackage =searchUnit.getDataObjectPackage();
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        searchDataObjectResult = new LinkedHashMap<ArchiveUnit, List<BinaryDataObject>>();

        searchInArchiveUnit(searchUnit);
        return "OK";
    }

    @Override
    protected void done() {
        callBack.accept(searchDataObjectResult);
    }
}
