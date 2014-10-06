import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Stackia <jsq2627@gmail.com> on 10/6/14.
 * With IntelliJ IDEA.
 */
class DiffWindow {
    private JPanel diffPanel;
    private JTextPane sourceTextPane;
    private JTextPane targetTextPane;
    private JButton loadSourceButton;
    private JButton loadTargetButton;
    private JRadioButton basedOnLinesRadioButton;
    private JRadioButton basedOnCharactersRadioButton;

    private DiffWindow() {
        ButtonGroup diffModeButtonGroup = new ButtonGroup();
        diffModeButtonGroup.add(basedOnCharactersRadioButton);
        diffModeButtonGroup.add(basedOnLinesRadioButton);
        final HighlightRunnable highlightRunnable = new HighlightRunnable(sourceTextPane, targetTextPane);
        basedOnLinesRadioButton.setSelected(true);
        highlightRunnable.setDiffMode(HighlightRunnable.DiffMode.LINES);
        highlightRunnable.run();
        sourceTextPane.getDocument().addDocumentListener(new DocumentShader(highlightRunnable));
        targetTextPane.getDocument().addDocumentListener(new DocumentShader(highlightRunnable));
        basedOnCharactersRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                highlightRunnable.setDiffMode(HighlightRunnable.DiffMode.CHARACTERS);
            }
        });
        basedOnLinesRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                highlightRunnable.setDiffMode(HighlightRunnable.DiffMode.LINES);
            }
        });
        ActionListener loadFileActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showDialog(diffPanel, "Choose") == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String line : lines) {
                            stringBuilder.append(line).append("\n");
                        }
                        if (e.getSource() == loadSourceButton) {
                            sourceTextPane.setText(stringBuilder.toString());
                        } else if (e.getSource() == loadTargetButton) {
                            targetTextPane.setText(stringBuilder.toString());
                        }
                    } catch (IOException io) {
                        JOptionPane.showMessageDialog(diffPanel, "Cannot read file.");
                    }
                }
            }
        };
        loadSourceButton.addActionListener(loadFileActionListener);
        loadTargetButton.addActionListener(loadFileActionListener);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Differ");
        frame.setContentPane(new DiffWindow().diffPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
