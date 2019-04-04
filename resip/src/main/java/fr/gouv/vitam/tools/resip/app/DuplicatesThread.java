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

import fr.gouv.vitam.tools.resip.frame.DuplicatesWindow;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;

public class DuplicatesThread extends SwingWorker<String, String> {

    private DuplicatesWindow duplicatesWindow;
    private DataObjectPackage dataObjectPackage;
    private LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap;
    private LinkedHashMap<String, List<ArchiveUnit>> auByDigestMap;
    private boolean binaryHash;
    private boolean binaryFilename;
    private boolean physicalAllMD;

    // logger
    private SEDALibProgressLogger spl;

    public DuplicatesThread(DuplicatesWindow duplicatesWindow, boolean binaryHash, boolean binaryFilename,
                            boolean physicalAllMD) {
        this.duplicatesWindow = duplicatesWindow;
        this.binaryHash = binaryHash;
        this.binaryFilename = binaryFilename;
        this.physicalAllMD = physicalAllMD;
    }

    void followTree(ArchiveUnit au, LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap,LinkedHashMap<String, List<DataObjectGroup>> sortedDogByDigestMap) {
        List<ArchiveUnit> auList = au.getChildrenAuList().getArchiveUnitList();

        for (ArchiveUnit childUnit : auList) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId()))
                continue;
            for(DataObject dataObject: childUnit.getDataObjectRefList().getDataObjectList()){
                if (dataObject instanceof DataObjectGroup){
                    for (Map.Entry<String,List<DataObjectGroup>>e:dogByDigestMap.entrySet()){
                        if (e.getValue().contains(dataObject)){
                            e.getValue().remove(dataObject);
                            sortedDogByDigestMap.get(e.getKey()).add((DataObjectGroup)dataObject);
                            auByDigestMap.get(e.getKey()).add(childUnit);
                        }
                        else if (sortedDogByDigestMap.get(e.getKey()).contains(dataObject))
                            auByDigestMap.get(e.getKey()).add(childUnit);
                    }
                }
            }
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            followTree(childUnit,dogByDigestMap,sortedDogByDigestMap);
        }
    }

    private LinkedHashMap<String, List<DataObjectGroup>> treeSort(LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap){
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        LinkedHashMap<String, List<DataObjectGroup>> sortedDogByDigestMap=new LinkedHashMap<String, List<DataObjectGroup>>();
        for (String e:dogByDigestMap.keySet())
            sortedDogByDigestMap.put(e,new ArrayList<DataObjectGroup>());
        auByDigestMap=new LinkedHashMap<String, List<ArchiveUnit>>();
        for (String e:dogByDigestMap.keySet())
            auByDigestMap.put(e,new ArrayList<ArchiveUnit>());
        followTree(dataObjectPackage.getGhostRootAu(),dogByDigestMap,sortedDogByDigestMap);
        return sortedDogByDigestMap;
    }

    @Override
    public String doInBackground() {
        String tmp = null;
        int counter = 0;
        try {
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, null, 1000);
            dataObjectPackage = ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage();

            spl.progressLog(SEDALibProgressLogger.GLOBAL,"Recherche de doublons ( "+(binaryHash?"hachage de fichier ":"")+
                    (binaryFilename?"nom de fichier ":"")+(physicalAllMD?"toute MD physique ":"")+")");
            dogByDigestMap = new LinkedHashMap<String, List<DataObjectGroup>>();
            for (DataObjectGroup dog : dataObjectPackage.getDogInDataObjectPackageIdMap().values()) {
                tmp = dog.getLogBookXmlData();
                for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                    if (binaryHash)
                        tmp += "|BDO=" + bdo.messageDigest;
                    if (binaryFilename)
                        tmp += "|" + bdo.fileInfo.filename;
                }
                for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
                    if (physicalAllMD)
                        tmp += "|PDO=" + pdo.toString();
                }
                if (dogByDigestMap.get(tmp) == null) {
                    ArrayList<DataObjectGroup> dogList = new ArrayList<DataObjectGroup>();
                    dogList.add(dog);
                    dogByDigestMap.put(tmp, dogList);
                } else
                    dogByDigestMap.get(tmp).add(dog);
                counter++;
                spl.progressLogIfStep(SEDALibProgressLogger.GLOBAL, counter,
                        Integer.toString(counter) + " groupes d'objets comparés");
            }
            dogByDigestMap.entrySet().removeIf(e -> e.getValue().size() == 1);
            dogByDigestMap=treeSort(dogByDigestMap);
            spl.progressLog(SEDALibProgressLogger.GLOBAL,
                    Integer.toString(dogByDigestMap.size()) + " lots de groupes d'objets semblables");
        } catch (Exception e) {
            try {
                if (spl != null)
                    spl.progressLog(GLOBAL, "Recherche de doublons impossibles\n-> " + e.getMessage());
            } catch (InterruptedException ignored) {
            }
        }
        return "OK";
    }

    @Override
    protected void done() {
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        if ((!isCancelled()) && (dogByDigestMap != null)) {
            duplicatesWindow.setDuplicatesResult(dogByDigestMap,auByDigestMap);
        }
    }
}
