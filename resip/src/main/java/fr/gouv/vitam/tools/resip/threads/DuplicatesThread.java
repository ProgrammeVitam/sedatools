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
import fr.gouv.vitam.tools.resip.frame.DuplicatesWindow;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.util.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The class DuplicatesThread.
 * <p>
 * Class for asynchronous duplicates computation thread.
 */
public class DuplicatesThread extends SwingWorker<String, String> {

    /**
     * Graphic element.
     */
    private DuplicatesWindow duplicatesWindow;

    /**
     * Data.
     */
    private DataObjectPackage dataObjectPackage;
    private LinkedHashMap<String, List<DataObjectGroup>> sortedDogByDogDigestMap;
    private HashMap<String, List<ArchiveUnit>> sortedAuByDogDigestMap;
    private HashMap<DataObjectGroup, String> dogKeyMap;
    private boolean binaryHash;
    private boolean binaryFilename;
    private boolean physicalAllMD;

    /**
     * The Exit exception.
     */
//run output
    private Throwable exitThrowable;

    /**
     * Logger.
     */
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Duplicates thread.
     *
     * @param duplicatesWindow the duplicates window
     * @param binaryHash       the binary hash
     * @param binaryFilename   the binary filename
     * @param physicalAllMD    the physical all md
     */
    public DuplicatesThread(DuplicatesWindow duplicatesWindow, boolean binaryHash, boolean binaryFilename,
                            boolean physicalAllMD) {
        this.duplicatesWindow = duplicatesWindow;
        this.binaryHash = binaryHash;
        this.binaryFilename = binaryFilename;
        this.physicalAllMD = physicalAllMD;
    }

    /**
     * Recursively browse the ArchiveUnit tree to sort the DOG by DOG digest map and create the sorted ArchiveUnit by DOG digest map.
     * The result is then sorted in the natural order of ArchiveUnit tree browsing.
     *
     * @param au                the au
     * @param dogByDogDigestMap the dog by digest map
     */
    void followTree(ArchiveUnit au, HashMap<String, List<DataObjectGroup>> dogByDogDigestMap) {
        List<ArchiveUnit> auList = au.getChildrenAuList().getArchiveUnitList();

        for (ArchiveUnit childUnit : auList) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId()))
                continue;
            for (DataObject dataObject : childUnit.getDataObjectRefList().getDataObjectList()) {
                if (dataObject instanceof DataObjectGroup) {
                    String dogKey = dogKeyMap.get(dataObject);
                    if (dogByDogDigestMap.get(dogKey).contains(dataObject)) {
                        dogByDogDigestMap.get(dogKey).remove(dataObject);
                        sortedDogByDogDigestMap.get(dogKey).add((DataObjectGroup) dataObject);
                        sortedAuByDogDigestMap.get(dogKey).add(childUnit);
                    } else if (sortedDogByDogDigestMap.get(dogKey).contains(dataObject))
                        sortedAuByDogDigestMap.get(dogKey).add(childUnit);
                }
            }
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            followTree(childUnit, dogByDogDigestMap);
        }
    }

    private LinkedHashMap<String, List<DataObjectGroup>> treeSort(HashMap<String, List<DataObjectGroup>> dogByDogDigestMap) {
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        sortedDogByDogDigestMap = new LinkedHashMap<String, List<DataObjectGroup>>();
        sortedAuByDogDigestMap = new HashMap<String, List<ArchiveUnit>>();
        for (String e : dogByDogDigestMap.keySet()) {
            sortedDogByDogDigestMap.put(e, new ArrayList<DataObjectGroup>());
            sortedAuByDogDigestMap.put(e, new ArrayList<ArchiveUnit>());
        }
        followTree(dataObjectPackage.getGhostRootAu(), dogByDogDigestMap);
        return sortedDogByDogDigestMap;
    }

    @Override
    public String doInBackground() {
        String tmp;
        int counter = 0;
        try {
            int localLogLevel, localLogStep;
            if (ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()) {
                localLogLevel = SEDALibProgressLogger.OBJECTS_WARNINGS;
                localLogStep = 1;
            } else {
                localLogLevel = SEDALibProgressLogger.OBJECTS_GROUP;
                localLogStep = 1000;
            }
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, null,
                    localLogStep, 2);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());
            dataObjectPackage = ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage();

            doProgressLog(spl, GLOBAL, "resip: recherche de doublons ( " + (binaryHash ? "hachage de fichier " : "") +
                    (binaryFilename ? "nom de fichier " : "") + (physicalAllMD ? "toute MD physique " : "") + ")", null);
            HashMap<String, List<DataObjectGroup>> dogByDigestMap = new HashMap<String, List<DataObjectGroup>>();
            dogKeyMap = new HashMap<DataObjectGroup, String>();
            for (DataObjectGroup dog : dataObjectPackage.getDogInDataObjectPackageIdMap().values()) {
                tmp = (dog.logBook == null ? "" : dog.logBook.toString());
                for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                    if (binaryHash)
                        tmp += "|BDO=" + (bdo.messageDigest == null ? null : bdo.messageDigest.getValue());
                    if (binaryFilename)
                        tmp += "|" + (bdo.fileInfo == null ? null : bdo.fileInfo.getSimpleMetadata("Filename"));
                }
                for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
                    if (physicalAllMD)
                        tmp += "|PDO=" + pdo.toSedaXmlFragments();
                }
                dogKeyMap.put(dog, tmp);
                if (dogByDigestMap.get(tmp) == null) {
                    ArrayList<DataObjectGroup> dogList = new ArrayList<DataObjectGroup>();
                    dogList.add(dog);
                    dogByDigestMap.put(tmp, dogList);
                } else
                    dogByDigestMap.get(tmp).add(dog);
                counter++;
                doProgressLogIfStep(spl, SEDALibProgressLogger.OBJECTS, counter, "resip: " +
                        counter + " groupes d'objets comparés");
            }
            dogByDigestMap = treeSort(dogByDigestMap);
            for (Iterator<Map.Entry<String, List<ArchiveUnit>>> it = sortedAuByDogDigestMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, List<ArchiveUnit>> entry = it.next();
                if (entry.getValue().size() == 1) {
                    it.remove();
                    dogByDigestMap.remove(entry.getKey());
                }
            }
            doProgressLog(spl, GLOBAL,
                    "resip: " + dogByDigestMap.size() + " lots de groupes d'objets semblables", null);
        } catch (Throwable e) {
            exitThrowable = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        if (isCancelled()) {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: recherche de doublons annulée", null);
            duplicatesWindow.setBlankDuplicatesResult();
        } else if (exitThrowable != null) {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: erreur durant la recherche de doublons", exitThrowable);
            duplicatesWindow.setBlankDuplicatesResult();
        } else {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: recherche de doublons terminée", null);
            duplicatesWindow.setDuplicatesResult(sortedDogByDogDigestMap, sortedAuByDogDigestMap);
        }
    }
}
