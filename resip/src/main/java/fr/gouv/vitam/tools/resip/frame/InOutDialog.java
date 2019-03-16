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
package fr.gouv.vitam.tools.resip.frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;

/**
 * The Class InOutDialog.
 */
public class InOutDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4092514078236156033L;

	/** The ext progress text area. */
	public JTextArea extProgressTextArea;
	
	/** The cancel button. */
	public JButton cancelButton;
	
	/** The ok button. */
	public JButton okButton;
	
	/** The thread. */
	public SwingWorker<?, ?> thread;


	/**
	 * Create the dialog.
	 *
	 * @param owner the owner
	 * @param title the title
	 */
	public InOutDialog(JFrame owner, String title) {
		super(owner, title, false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		setBounds(100, 100, 550, 300);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 550, 0 };
		gridBagLayout.rowHeights = new int[] { 15, 220, 35, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);
		{
			JLabel lblNewLabel = new JLabel("Opération en cours:");
			lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblNewLabel.insets = new Insets(5, 5, 5, 0);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			getContentPane().add(lblNewLabel, gbc_lblNewLabel);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 1;
			getContentPane().add(scrollPane, gbc_scrollPane);
			{
				extProgressTextArea = new JTextArea();
				extProgressTextArea.setWrapStyleWord(true);
				extProgressTextArea.setLineWrap(true);
				scrollPane.setViewportView(extProgressTextArea);
			}
		}
			JPanel buttonPane = new JPanel();
			GridBagLayout gbl_buttonPane = new GridBagLayout();
			gbl_buttonPane.columnWeights = new double[] { 1.0, 1.0 };
			buttonPane.setLayout(gbl_buttonPane);
			GridBagConstraints gbc_buttonPane = new GridBagConstraints();
			gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
			gbc_buttonPane.gridx = 0;
			gbc_buttonPane.gridy = 2;
			getContentPane().add(buttonPane, gbc_buttonPane);

			cancelButton = new JButton("Annuler");
			GridBagConstraints gbc_cancelButton = new GridBagConstraints();
			gbc_cancelButton.gridx = 0;
			gbc_cancelButton.gridy = 0;
			buttonPane.add(cancelButton, gbc_cancelButton);
			cancelButton.addActionListener(arg0 -> buttonCancel());

			okButton = new JButton("Fermer");
			okButton.setEnabled(false);
			GridBagConstraints gbc_okButton = new GridBagConstraints();
			gbc_okButton.gridx = 1;
			gbc_okButton.gridy = 0;
			buttonPane.add(okButton, gbc_okButton);
			okButton.addActionListener(arg0 -> buttonOk());

		
		addWindowListener(new WindowAdapter()
	    {
	        @Override
	        public void windowClosing(WindowEvent e)
	        {
	            cancelButton.doClick();
	            okButton.doClick();
	        }
	    });
		
		pack();
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Sets the thread.
	 *
	 * @param thread the thread
	 */
	public void setThread(SwingWorker<?, ?> thread) {
		this.thread=thread;
	}
	
	/**
	 * Button cancel.
	 */
	private void buttonCancel() {
		thread.cancel(true);
	}

	/**
	 * Button ok.
	 */
	private void buttonOk() {
        setVisible(false);
	}
}
