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
package fr.gouv.vitam.tools.javalibpst;

import java.io.IOException;
//import java.util.HashMap;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.Map;
//import java.util.TreeMap;

public abstract class PSTFileContent {
    public abstract void seek(long index) throws IOException;

    public abstract long getFilePointer() throws IOException;

    public abstract int read() throws IOException;

    public abstract int read(byte[] target) throws IOException;

    public final void readCompletely(final byte[] target) throws IOException {
    //    collectCallStack();
        int read =  this.read(target);
        // bail in common case
        if (read <= 0 || read == target.length) {
            return;
        }

        byte[] buffer = new byte[8192];
        int offset = read;
        while (offset < target.length) {
            read = this.read(buffer);
            if (read <= 0) {
                break;
            }
            int length = Math.min(read, target.length - offset);
            System.arraycopy(buffer, 0, target, offset, length);
            offset += length;
        }
    }

    public abstract byte readByte() throws IOException;

    public abstract void close() throws IOException;

    /**
     * Audit the code execution to better understand calls to `seek` and `read` operations
     * in `PSTFileContent`, and ensure the proper usage of `synchronized(PSTFileContent)`
     * blocks where necessary, at lowest level.
     *
     * Actions Taken:
     * - In the `PSTFile` class, the `synchronized(PSTFileContent)` block has already been applied to:
     *     - `findBtreeItem`,
     *     - `getLeafSize`,
     *     - `getChildDescriptorTree`.
     *
     * - Synchronization has NOT been added to:
     *     - `processDescriptorBTree` (private, only called by `getChildDescriptorTree`),
     *     - `extractLEFileOffset` (private, only called by `findBtreeItem`, `processDescriptorBTree` and `getChildDescriptorTree`).
     *
     * - In the `PSTNodeInputStream` class, the `synchronized(PSTFileContent)` block
     *   has already been added to:
     *     - `loadFromOffsetItem`,
     *     - `getBlockSkipPoints`,
     *     - `detectZlib`,
     *     - `read`.
     * - Synchronization has NOT been added to:
     *     - `seek` (not needed, as the last unused call to `PSTFileContent.seek` has already been removed).
     *

     HashMap<String, Boolean> traceLines = new HashMap<String, Boolean>();
     HashMap<String, String> traceLinesFileAction = new HashMap<String, String>();
     private final AtomicLong lastTraceExecutionTime = new AtomicLong(System.currentTimeMillis());

     protected void collectCallStack() {
        // Check if systemOutTraceLines should be called
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTraceExecutionTime.get() > 120000) { // 2 minutes
            synchronized (this) {
                if (currentTime - lastTraceExecutionTime.get() > 120000) {
                    systemOutTraceLines();
                }
            }
        }

        // Capture the current stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Print the sorted stack trace to System.out
        String traceLine="";
        String fileAction="";
        boolean isSynchronized = Thread.holdsLock(this);
        boolean firstMethod=true;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();
            if ((className.contains("fr.gouv.vitam.tools.javalibpst") || className.contains("microsoft.pst")) && !methodName.contains("collectCallStack")) {
                className = className.replace("fr.gouv.vitam.tools.javalibpst.", "");
                className = className.replace("fr.gouv.vitam.microsoft.pst.", "vitam");
                if (firstMethod) {
                    firstMethod=false;
                    fileAction=methodName;
                }
                else
                    traceLine=traceLine+"/"+className+"."+methodName;
            }
        }
        traceLines.put(traceLine, isSynchronized);
        String tracedFileAction=traceLinesFileAction.get(traceLine);
        if (tracedFileAction==null)
            traceLinesFileAction.put(traceLine,fileAction);
        else if (!tracedFileAction.contains(fileAction))
            traceLinesFileAction.put(traceLine,tracedFileAction+"/"+fileAction);

        if (System.currentTimeMillis()-lastTraceExecutionTime.get()>60000) {
            systemOutTraceLines();
        }
    }

    void systemOutTraceLines() {
        // Update the last execution time
        lastTraceExecutionTime.set(System.currentTimeMillis());

        // Print key-value pairs of traceLines
        TreeMap<String, Boolean> treeMap = new TreeMap<>(traceLines);
        int linesCount = 0;
        System.out.println("NotSync list begin");
        for (Map.Entry<String, Boolean> entry : treeMap.entrySet()) {
            if (!entry.getValue()) {
                System.out.println("---FileAction: "+traceLinesFileAction.get(entry.getKey())+"--Trace: " + entry.getKey());
                linesCount++;
            }
        }
        System.out.println("NotSync list end - "+linesCount+" lines");
    }
     */
}
