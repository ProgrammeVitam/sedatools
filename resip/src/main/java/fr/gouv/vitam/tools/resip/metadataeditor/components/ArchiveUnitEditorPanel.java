package fr.gouv.vitam.tools.resip.metadataeditor.components;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

public interface ArchiveUnitEditorPanel {
    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException;

    public ArchiveUnit extractArchiveUnit() throws SEDALibException;
}
