/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of javalibpst.
 *
 * javalibpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * javalibpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with javalibpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package fr.gouv.vitam.tools.javalibpst;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * PSTActivity represents Journal entries
 *
 * @author Richard Johnson
 */
public class PSTActivity extends PSTMessage {

    /**
     * Instantiates a new Pst activity.
     *
     * @param theFile             the the file
     * @param descriptorIndexNode the descriptor index node
     * @throws PSTException the pst exception
     * @throws IOException  the io exception
     */
    public PSTActivity(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * Instantiates a new Pst activity.
     *
     * @param theFile              the the file
     * @param folderIndexNode      the folder index node
     * @param table                the table
     * @param localDescriptorItems the local descriptor items
     */
    public PSTActivity(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Type
     *
     * @return the log type
     */
    public String getLogType() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008700, PSTFile.PSETID_Log));
    }

    /**
     * Start
     *
     * @return the log start
     */
    public Date getLogStart() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008706, PSTFile.PSETID_Log));
    }

    /**
     * Duration
     *
     * @return the log duration
     */
    public int getLogDuration() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008707, PSTFile.PSETID_Log));
    }

    /**
     * End
     *
     * @return the log end
     */
    public Date getLogEnd() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008708, PSTFile.PSETID_Log));
    }

    /**
     * LogFlags
     *
     * @return the log flags
     */
    public int getLogFlags() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x0000870c, PSTFile.PSETID_Log));
    }

    /**
     * DocPrinted
     *
     * @return the boolean
     */
    public boolean isDocumentPrinted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870e, PSTFile.PSETID_Log)));
    }

    /**
     * DocSaved
     *
     * @return the boolean
     */
    public boolean isDocumentSaved() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870f, PSTFile.PSETID_Log)));
    }

    /**
     * DocRouted
     *
     * @return the boolean
     */
    public boolean isDocumentRouted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008710, PSTFile.PSETID_Log)));
    }

    /**
     * DocPosted
     *
     * @return the boolean
     */
    public boolean isDocumentPosted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008711, PSTFile.PSETID_Log)));
    }

    /**
     * Type Description
     *
     * @return the log type desc
     */
    public String getLogTypeDesc() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008712, PSTFile.PSETID_Log));
    }

    @Override
    public String toString() {
        return "Type ASCII or Unicode string: " + this.getLogType() + "\n" + "Start Filetime: " + this.getLogStart()
            + "\n" + "Duration Integer 32-bit signed: " + this.getLogDuration() + "\n" + "End Filetime: "
            + this.getLogEnd() + "\n" + "LogFlags Integer 32-bit signed: " + this.getLogFlags() + "\n"
            + "DocPrinted Boolean: " + this.isDocumentPrinted() + "\n" + "DocSaved Boolean: " + this.isDocumentSaved()
            + "\n" + "DocRouted Boolean: " + this.isDocumentRouted() + "\n" + "DocPosted Boolean: "
            + this.isDocumentPosted() + "\n" + "TypeDescription ASCII or Unicode string: " + this.getLogTypeDesc();

    }

}
