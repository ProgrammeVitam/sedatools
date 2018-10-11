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
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.utils;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class SEDALibProgressLogger.
 * <p>
 * Class for a logger that can also call a lambda function to follow a progress
 * in execution of long processes. This can be used for example to actualise a
 * progress dialog.
 */
public class SEDALibProgressLogger {

	/**
	 * The Interface ProgressLogFunc.
	 */
	@FunctionalInterface
	public interface ProgressLogFunc {

		/**
		 * Do progress log.
		 *
		 * @param count the count
		 * @param log   the log
		 */
		void doProgressLog(int count, String log);
	}

	/** The progress log func. */
	private ProgressLogFunc progressLogFunc;

	/** The logger. */
	public Logger logger;

	/** The step. */
	public int step;

	/**
	 * Instantiates a new SEDA lib progress logger.
	 *
	 * @param logger the logger
	 */
	public SEDALibProgressLogger(Logger logger) {
		this.progressLogFunc = null;
		this.logger = logger;
		this.step = Integer.MAX_VALUE;
	}

	/**
	 * Instantiates a new SEDA lib progress logger.
	 *
	 * @param progressConsumer the progress consumer
	 * @param logger           the logger
	 * @param step             the step
	 */
	public SEDALibProgressLogger(Logger logger, ProgressLogFunc progressConsumer, int step) {
		this.progressLogFunc = progressConsumer;
		this.logger = logger;
		this.step = step;
	}

	/**
	 * Progress log.
	 *
	 * @param level the level
	 * @param count the count
	 * @param log   the log
	 * @throws InterruptedException the interrupted exception
	 */
	public void ProgressLog(Level level, int count, String log) throws InterruptedException {
		if (progressLogFunc != null) {
			progressLogFunc.doProgressLog(count, log);
		}
		logger.log(level, log);
		Thread.sleep(1);
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
}
