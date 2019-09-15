package fr.gouv.vitam.tools.resip.metadataeditor;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * The type Numeric filter.
 */
public class NumericFilter extends DocumentFilter {
    @Override
    public void replace(FilterBypass fb, int offs, int length,
                        String str, AttributeSet a) throws BadLocationException {
        if (str.charAt(0)=='-')
            str = "-"+str.replaceAll("[^0-9]", "");
        else
            str = str.replaceAll("[^0-9]", "");
        super.replace(fb, offs, length, str, a);
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str,
                             AttributeSet a) throws BadLocationException {
        if (str.charAt(0)=='-')
            str = "-"+str.replaceAll("[^0-9]", "");
        else
            str = str.replaceAll("[^0-9]", "");
        super.insertString(fb, offs, str, a);
    }
}