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
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.service.SessionInfo;
import fr.gouv.vitam.tools.resip.service.WorkDirService;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.utils.ResipLogger.getGlobalLogger;

/**
 * The class SessionsDialog.
 * <p>
 * Class for the management of the sessions saved in the work directory: they are listed with what
 * they need, and can be loaded or deleted.
 */
public class SessionsDialog extends JDialog {

    private static final String[] COLUMNS = {
        "Session",
        "Modifiée le",
        "Version SEDA",
        "Binaires",
        "Répertoires temporaires utilisés",
    };

    private final JFrame owner;
    private final String workDir;
    private final File openedSession;
    private final Collection<String> pathsInUse;

    private final SessionsTableModel tableModel;
    private final JTable table;
    private final JCheckBox withTmpDirsCheckBox;

    /**
     * The session to load, null if none.
     */
    private File sessionToLoad;

    // Dialog test context

    /**
     * The entry point of dialog test.
     *
     * @param args the input arguments
     * @throws ClassNotFoundException          the class not found exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws NoSuchMethodException           the no such method exception
     * @throws InvocationTargetException       the invocation target exception
     */
    public static void main(String[] args)
        throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TestDialogWindow window = new TestDialogWindow(SessionsDialog.class);
    }

    /**
     * Instantiates a new SessionsDialog for test.
     *
     * @param owner the owner
     */
    public SessionsDialog(JFrame owner) {
        this(owner, new CreationContext(Preferences.getInstance()).getWorkDir(), null, List.of());
    }

    /**
     * Create the dialog.
     *
     * @param owner         the owner
     * @param workDir       the work directory
     * @param openedSession the session file of the opened work, which can't be deleted, or null
     * @param pathsInUse    the paths of the files of the opened work, whose temporary directories
     *                      are kept
     */
    public SessionsDialog(JFrame owner, String workDir, File openedSession, Collection<String> pathsInUse) {
        super(owner, "Gestion des sessions", true);
        this.owner = owner;
        this.workDir = workDir;
        this.openedSession = openedSession == null ? null : openedSession.getAbsoluteFile();
        this.pathsInUse = pathsInUse;
        this.sessionToLoad = null;

        setPreferredSize(new Dimension(1000, 400));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));

        JLabel workDirLabel = new JLabel("Sessions du répertoire de travail " + workDir);
        workDirLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        contentPane.add(workDirLabel, BorderLayout.NORTH);

        tableModel = new SessionsTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(400);
        contentPane.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        withTmpDirsCheckBox = new JCheckBox(
            "Supprimer aussi les répertoires temporaires utilisés uniquement par ces sessions"
        );
        withTmpDirsCheckBox.setToolTipText(
            "Un répertoire temporaire utilisé par une autre session du répertoire de travail, ou par le travail " +
            "ouvert, est conservé. Les sessions sauvegardées ailleurs ne sont pas connues."
        );
        actionPanel.add(withTmpDirsCheckBox);
        JButton loadButton = new JButton("Charger");
        loadButton.addActionListener(arg -> buttonLoad());
        actionPanel.add(loadButton);
        JButton deleteButton = new JButton("Supprimer");
        deleteButton.addActionListener(arg -> buttonDelete());
        actionPanel.add(deleteButton);
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(arg -> setVisible(false));
        actionPanel.add(closeButton);
        contentPane.add(actionPanel, BorderLayout.SOUTH);

        refresh();
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Gets the session to load, chosen with the "Charger" button.
     *
     * @return the session file, or null if none
     */
    public File getSessionToLoad() {
        return sessionToLoad;
    }

    private void refresh() {
        tableModel.setInfos(WorkDirService.describeSessions(workDir));
    }

    private List<File> selectedSessions() {
        List<File> sessions = new ArrayList<>();
        for (int row : table.getSelectedRows()) sessions.add(
            tableModel.getInfo(table.convertRowIndexToModel(row)).getFile()
        );
        return sessions;
    }

    // actions

    private void buttonLoad() {
        List<File> sessions = selectedSessions();
        if (sessions.size() != 1) {
            UserInteractionDialog.getUserAnswer(
                owner,
                "Sélectionnez une seule session à charger.",
                "Information",
                UserInteractionDialog.IMPORTANT_DIALOG,
                null
            );
            return;
        }
        sessionToLoad = sessions.get(0);
        setVisible(false);
    }

    private void buttonDelete() {
        List<File> sessions = selectedSessions();
        if (sessions.isEmpty()) return;
        if ((openedSession != null) && sessions.stream().anyMatch(s -> s.getAbsoluteFile().equals(openedSession))) {
            UserInteractionDialog.getUserAnswer(
                owner,
                "La session [" + openedSession + "] est celle du travail ouvert,\nfermez-le avant de la supprimer.",
                "Information",
                UserInteractionDialog.IMPORTANT_DIALOG,
                null
            );
            return;
        }
        StringBuilder message = new StringBuilder("Vous allez supprimer définitivement ");
        message.append(sessions.size() == 1 ? "la session:\n" : "les " + sessions.size() + " sessions:\n");
        for (File session : sessions) message.append("  - ").append(session).append("\n");
        if (withTmpDirsCheckBox.isSelected()) message.append(
            "ainsi que les répertoires temporaires qu'elles sont seules à utiliser.\n"
        );
        message.append("Voulez-vous continuer?");
        if (
            UserInteractionDialog.getUserAnswer(
                owner,
                message.toString(),
                "Confirmation",
                UserInteractionDialog.WARNING_DIALOG,
                null
            ) !=
            OK_DIALOG
        ) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<File> deletedTmpDirs = WorkDirService.deleteSessions(
                workDir,
                sessions,
                withTmpDirsCheckBox.isSelected(),
                pathsInUse,
                null
            );
            for (File session : sessions) getGlobalLogger()
                .log(ResipLogger.GLOBAL, "resip: session [" + session + "] supprimée", null);
            for (File tmpDir : deletedTmpDirs) getGlobalLogger()
                .log(ResipLogger.GLOBAL, "resip: répertoire temporaire [" + tmpDir + "] supprimé", null);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(
                owner,
                "Erreur de suppression\n->" + e.getMessage(),
                "Erreur",
                UserInteractionDialog.ERROR_DIALOG,
                null
            );
            getGlobalLogger().log(ResipLogger.STEP, "resip: erreur de suppression de sessions", e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            refresh();
        }
    }

    // the sessions, one per row
    private class SessionsTableModel extends AbstractTableModel {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        private List<SessionInfo> infos = new ArrayList<>();

        void setInfos(List<SessionInfo> infos) {
            this.infos = infos;
            fireTableDataChanged();
        }

        SessionInfo getInfo(int row) {
            return infos.get(row);
        }

        @Override
        public int getRowCount() {
            return infos.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            SessionInfo info = infos.get(row);
            switch (column) {
                case 0:
                    String name = relativeToWorkDir(info.getFile());
                    return info.getFile().getAbsoluteFile().equals(openedSession) ? name + " (ouverte)" : name;
                case 1:
                    return dateFormat.format(new Date(info.getFile().lastModified()));
                case 2:
                    return info.getSedaVersion() == null ? "illisible" : info.getSedaVersion().displayString();
                case 3:
                    if (info.getError() != null) return "";
                    return info.getMissingBinaryCount() == 0 ? "OK" : info.getMissingBinaryCount() + " manquant(s)";
                case 4:
                    List<String> tmpDirs = new ArrayList<>();
                    for (File tmpDir : info.getUsedTmpDirs()) tmpDirs.add(
                        tmpDir.getName() + (tmpDir.isDirectory() ? "" : " (effacé)")
                    );
                    return String.join(", ", tmpDirs);
                default:
                    return "";
            }
        }

        private String relativeToWorkDir(File file) {
            Path workDirPath = Paths.get(workDir).toAbsolutePath().normalize();
            Path path = file.toPath().toAbsolutePath().normalize();
            return path.startsWith(workDirPath) ? workDirPath.relativize(path).toString() : path.toString();
        }
    }
}
