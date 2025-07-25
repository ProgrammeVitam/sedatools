package example;

import java.util.Vector;

import fr.gouv.vitam.tools.javalibpst.PSTException;
import fr.gouv.vitam.tools.javalibpst.PSTFile;
import fr.gouv.vitam.tools.javalibpst.PSTFolder;
import fr.gouv.vitam.tools.javalibpst.PSTMessage;

public class Test {
    public static void main(final String[] args) {
        new Test(args[0]);
    }

    public Test(final String filename) {
        try {
            final PSTFile pstFile = new PSTFile(filename);
            System.out.println(pstFile.getMessageStore().getDisplayName());
            this.processFolder(pstFile.getRootFolder());
        } catch (final Exception err) {
            err.printStackTrace();
        }
    }

    int depth = -1;

    public void processFolder(final PSTFolder folder) throws PSTException, java.io.IOException {
        this.depth++;
        // the root folder doesn't have a display name
        if (this.depth > 0) {
            this.printDepth();
            System.out.println(folder.getDisplayName());
        }

        // go through the folders...
        if (folder.hasSubfolders()) {
            final Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (final PSTFolder childFolder : childFolders) {
                this.processFolder(childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            this.depth++;
            PSTMessage email = (PSTMessage) folder.getNextChild();
            while (email != null) {
                if (!email.getMessageClass().equals("IPM.Note")) {
                    this.printDepth();
                    System.out.println("Email: [" + email.getMessageClass() + "]" + email.getDescriptorNodeId() + " - " + email.getSubject());
                }
                email = (PSTMessage) folder.getNextChild();
            }
            this.depth--;
        }
        this.depth--;
    }

    public void printDepth() {
        for (int x = 0; x < this.depth - 1; x++) {
            System.out.print(" | ");
        }
        System.out.print(" |- ");
    }
}
