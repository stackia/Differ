import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by Stackia <jsq2627@gmail.com> on 10/6/14.
 * With IntelliJ IDEA.
 */
class DocumentShader implements DocumentListener {
    private final HighlightRunnable highlightRunnable;

    public DocumentShader(HighlightRunnable highlightRunnable) {
        this.highlightRunnable = highlightRunnable;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(highlightRunnable);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(highlightRunnable);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }
}
