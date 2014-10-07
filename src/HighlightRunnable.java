import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Stackia <jsq2627@gmail.com> on 10/7/14.
 * With IntelliJ IDEA.
 */
public class HighlightRunnable implements Runnable {
    private final LevenshteinDistanceCalculator levenshteinDistanceCalculator;
    private final DiffDrawingPanel diffDrawingPanel;
    private final JTextPane sourceTextPane;
    private final JTextPane targetTextPane;
    private final SimpleAttributeSet copiedStyleAttributeSet = new SimpleAttributeSet();
    private final SimpleAttributeSet deletedStyleAttributeSet = new SimpleAttributeSet();
    private final SimpleAttributeSet insertedStyleAttributeSet = new SimpleAttributeSet();
    private final SimpleAttributeSet replacedStyleAttributeSet = new SimpleAttributeSet();
    private List<String> sourceLineList;
    private List<String> targetLineList;
    private DiffMode diffMode;

    public HighlightRunnable(JTextPane sourceTextPane, JTextPane targetTextPane, DiffDrawingPanel diffDrawingPanel, LevenshteinDistanceCalculator levenshteinDistanceCalculator) {
        this.sourceTextPane = sourceTextPane;
        this.targetTextPane = targetTextPane;
        this.diffDrawingPanel = diffDrawingPanel;
        this.levenshteinDistanceCalculator = levenshteinDistanceCalculator;

        StyleConstants.setForeground(copiedStyleAttributeSet, Color.BLACK);
        StyleConstants.setForeground(deletedStyleAttributeSet, Color.RED);
        StyleConstants.setItalic(deletedStyleAttributeSet, true);
        StyleConstants.setForeground(insertedStyleAttributeSet, Color.GREEN);
        StyleConstants.setBold(insertedStyleAttributeSet, true);
        StyleConstants.setForeground(replacedStyleAttributeSet, Color.BLUE);
    }

    // String to List<Character> wrapper
    private java.util.List<Character> stringAsList(final String string) {
        return new AbstractList<Character>() {
            @Override
            public Character get(int index) {
                return string.charAt(index);
            }

            @Override
            public int size() {
                return string.length();
            }
        };
    }

    public void setDiffMode(DiffMode diffMode) {
        this.diffMode = diffMode;
        run();
    }

    private int highlightStart(int index, boolean isSource) {
        if (diffMode == DiffMode.CHARACTERS) {
            return index;
        } else if (diffMode == DiffMode.LINES) {
            int start = 0;
            for (int i = 0; i < index; i++) {
                if (isSource) {
                    start += sourceLineList.get(i).length() + 1;
                } else {
                    start += targetLineList.get(i).length() + 1;
                }
            }
            return start;
        }
        return 0;
    }

    private int highlightLength(int index, boolean isSource) {
        if (diffMode == DiffMode.CHARACTERS) {
            return 1;
        } else if (diffMode == DiffMode.LINES) {
            if (isSource) {
                return sourceLineList.get(index).length();
            } else {
                return targetLineList.get(index).length();
            }
        }
        return 0;
    }

    @Override
    public void run() {
        boolean enableReplace = true;
        if (diffMode == DiffMode.CHARACTERS) {
            enableReplace = true;
            levenshteinDistanceCalculator.setSource(stringAsList(sourceTextPane.getText()));
            levenshteinDistanceCalculator.setTarget(stringAsList(targetTextPane.getText()));
        } else if (diffMode == DiffMode.LINES) {
            sourceLineList = Arrays.asList(sourceTextPane.getText().split("\n"));
            targetLineList = Arrays.asList(targetTextPane.getText().split("\n"));
            enableReplace = false;
            levenshteinDistanceCalculator.setSource(sourceLineList);
            levenshteinDistanceCalculator.setTarget(targetLineList);
        }
        levenshteinDistanceCalculator.setEnableReplace(enableReplace);
        levenshteinDistanceCalculator.execute();
        StyledDocument sourcePaneStyledDocument = sourceTextPane.getStyledDocument();
        StyledDocument targetPaneStyledDocument = targetTextPane.getStyledDocument();

        sourcePaneStyledDocument.setCharacterAttributes(0, sourceTextPane.getText().length(), copiedStyleAttributeSet, true);
        targetPaneStyledDocument.setCharacterAttributes(0, targetTextPane.getText().length(), copiedStyleAttributeSet, true);

        for (int[] p : levenshteinDistanceCalculator.getResults().copy) {
            sourcePaneStyledDocument.setCharacterAttributes(highlightStart(p[0], true), highlightLength(p[0], true), copiedStyleAttributeSet, true);
            targetPaneStyledDocument.setCharacterAttributes(highlightStart(p[1], false), highlightLength(p[1], false), copiedStyleAttributeSet, true);
        }

        for (int[] p : levenshteinDistanceCalculator.getResults().delete) {
            sourcePaneStyledDocument.setCharacterAttributes(highlightStart(p[0], true), highlightLength(p[0], true), deletedStyleAttributeSet, true);
        }

        for (int[] p : levenshteinDistanceCalculator.getResults().insert) {
            targetPaneStyledDocument.setCharacterAttributes(highlightStart(p[1], false), highlightLength(p[1], false), insertedStyleAttributeSet, true);
        }

        if (enableReplace) {
            for (int[] p : levenshteinDistanceCalculator.getResults().replace) {
                sourcePaneStyledDocument.setCharacterAttributes(highlightStart(p[0], true), highlightLength(p[0], true), replacedStyleAttributeSet, true);
                targetPaneStyledDocument.setCharacterAttributes(highlightStart(p[1], false), highlightLength(p[1], false), replacedStyleAttributeSet, true);
            }
        }

        diffDrawingPanel.repaint();
    }

    public enum DiffMode {
        LINES,
        CHARACTERS,
    }
}
