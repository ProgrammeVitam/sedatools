/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.resip.app;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Instant;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToDiskExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

public class ExportThread extends SwingWorker<String, String> {

	static final int SIP_EXPORT = 1;
	static final int MANIFEST_EXPORT = 2;
	static final int DISK_EXPORT = 3;

	private Work work;
	long lastReport = Instant.now().getEpochSecond();
	Exception exitException;
	int exportType;
	JTextArea logTextArea;
	String summary;
	SEDALibProgressLogger spl;
	InOutDialog inOutDialog;

	ExportThread(Work work, int exportType, InOutDialog dialog) {
		this.work = work;
		this.logTextArea = dialog.extProgressTextArea;
		this.exportType = exportType;
		dialog.setThread(this);
		this.inOutDialog=dialog;
	}

	/**
	 * Readable file size.
	 *
	 * @param size the size
	 * @return the string
	 */
	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

	@Override
	public String doInBackground() {
		SEDALibProgressLogger spl = null;
		try {
			if (logTextArea != null)
				spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP,(count, log) -> {
					String newLog = logTextArea.getText() + "\n" + log;
					logTextArea.setText(newLog);
					logTextArea.setCaretPosition(newLog.length());
				},  100);
			else
				spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP);

			// first verify and reindex if neccesary
			if (work.getExportContext().isReindex()) {
				work.getDataObjectPackage().regenerateContinuousIds();
				ResipGraphicApp.getTheApp().mainWindow.allTreeChanged();
			}
			
			ArchiveTransfer archiveTransfer= new ArchiveTransfer();
			work.getDataObjectPackage().setManagementMetadataXmlData(work.getExportContext().getManagementMetadataXmlData());
			archiveTransfer.setDataObjectPackage(work.getDataObjectPackage());
			archiveTransfer.setGlobalMetadata(work.getExportContext().getArchiveTransferGlobalMetadata());
			if (exportType == MANIFEST_EXPORT) {
				inOutDialog.extProgressTextArea.setText("Export du manifest SEDA en " + work.getExportContext().getOnDiskOutput() + "\n");
				ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
				sm.doExportToSEDAXMLManifest(work.getExportContext().getOnDiskOutput(), work.getExportContext().isHierarchicalArchiveUnits(),
						work.getExportContext().isIndented());
				spl.progressLog(SEDALibProgressLogger.GLOBAL,
						"Fichier sauvegardé (" + readableFileSize(new File(work.getExportContext().getOnDiskOutput()).length()) + ")");
				summary=sm.getSummary();
			} else if (exportType == SIP_EXPORT) {
				inOutDialog.extProgressTextArea.setText("Export du SIP SEDA en " + work.getExportContext().getOnDiskOutput() + "\n");
				ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
				sm.doExportToSEDASIP(work.getExportContext().getOnDiskOutput(), work.getExportContext().isHierarchicalArchiveUnits(),
						work.getExportContext().isIndented());
				spl.progressLog(SEDALibProgressLogger.GLOBAL,
						"Fichier sauvegardé (" + readableFileSize(new File(work.getExportContext().getOnDiskOutput()).length()) + ")");
				summary=sm.getSummary();
			} else if (exportType == DISK_EXPORT) {
				inOutDialog.extProgressTextArea.setText("Export en hiérarchie disque en " + work.getExportContext().getOnDiskOutput() + "\n");
				ArchiveTransferToDiskExporter de = new ArchiveTransferToDiskExporter(archiveTransfer, spl);
				de.doExport(work.getExportContext().getOnDiskOutput());
				summary=de.getSummary();
			} else
				spl.progressLog(SEDALibProgressLogger.GLOBAL,"Resip.App: Export attendu inconnu");

			return "OK";
		} catch (Exception e) {
			if (spl != null) {
				try {
					spl.progressLog(SEDALibProgressLogger.GLOBAL,"Resip.App: Export impossible\n->" + e.getMessage());
				} catch (InterruptedException ignored) {
				}
			}
			exitException = e;
			e.printStackTrace();
			return "KO";
		}
	}

	@Override
	protected void done() {
		JTextArea loadText = inOutDialog.extProgressTextArea;
		inOutDialog.okButton.setEnabled(true);
		inOutDialog.cancelButton.setEnabled(false);
		if (isCancelled())
			loadText.setText(loadText.getText() + "\n-> " + "Export annulé, les données seront partiellement sur le disque.");
		else {
			loadText.setText(loadText.getText() + "\n-> " + summary);
			Prefs.getInstance().setPrefsExportDirFromChild(work.getExportContext().getOnDiskOutput());
		}
	}
}
