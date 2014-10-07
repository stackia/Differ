import javax.swing.*;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stackia <jsq2627@gmail.com> on 10/7/14.
 * With IntelliJ IDEA.
 */
public class DiffDrawingPanel extends JPanel {
    private LevenshteinDistanceCalculator levenshteinDistanceCalculator;
    private boolean enableDrawing = true;
    private JTextPane sourceTextPane;
    private JScrollPane sourceScrollPane;
    private JTextPane targetTextPane;
    private JScrollPane targetScrollPane;

    public void initDrawing(LevenshteinDistanceCalculator levenshteinDistanceCalculator, JTextPane sourceTextPane, JScrollPane sourceScrollPane, JTextPane targetTextPane, JScrollPane targetScrollPane) {
        this.levenshteinDistanceCalculator = levenshteinDistanceCalculator;
        this.sourceScrollPane = sourceScrollPane;
        this.sourceTextPane = sourceTextPane;
        this.targetScrollPane = targetScrollPane;
        this.targetTextPane = targetTextPane;
    }

    public void setEnableDrawing(boolean enableDrawing) {
        this.enableDrawing = enableDrawing;
    }

    private int lineViewportPositionY(JTextPane textPane, JViewport viewport, int line, boolean top) {
        try {
            int pos = 0;
            String[] lines = textPane.getText().split("\n");
            for (int i = 0; i < line; i++) {
                pos += lines[i].length() + 1;
            }
            Rectangle lineFirstCharRect, lineLastCharRect, viewportRect;
            lineFirstCharRect = textPane.modelToView(pos);
            lineLastCharRect = textPane.modelToView(pos + lines[line].length() - 1);
            viewportRect = viewport.getViewRect();

            int lineY;
            if (top) {
                lineY = lineFirstCharRect.y;
            } else {
                lineY = lineLastCharRect.y + lineLastCharRect.height;
            }
            if (lineY <= viewportRect.y) {
                return -1;
            } else if (lineY >= viewportRect.y + viewportRect.height) {
                return -2;
            }
            return lineY - viewportRect.y;
        } catch (Exception exception) {
            return -3;
        }
    }

    private int flagToPositionY(int flag) {
        return flag == -1 ? 0 : (flag == -2 ? sourceScrollPane.getViewport().getHeight() : flag);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!enableDrawing) {
            return;
        }
        if (levenshteinDistanceCalculator == null) {
            return;
        }
        List<int[]> areas = new ArrayList<int[]>();
        List<int[]> deletes = levenshteinDistanceCalculator.getResults().delete;
        List<int[]> inserts = levenshteinDistanceCalculator.getResults().insert;
        for (int i = 0; i < deletes.size(); ) {
            int[] delete = deletes.get(i);
            int lb = lineViewportPositionY(sourceTextPane, sourceScrollPane.getViewport(), delete[0], false);
            int rb = lineViewportPositionY(targetTextPane, targetScrollPane.getViewport(), delete[1], false);
            int lt = lineViewportPositionY(sourceTextPane, sourceScrollPane.getViewport(), delete[0], true);
            int rt = rb;
            while (++i < deletes.size() && deletes.get(i)[0] == delete[0] - 1) {
                delete = deletes.get(i);
                lt = lineViewportPositionY(sourceTextPane, sourceScrollPane.getViewport(), delete[0], true);
            }
            if ((rb == -1 && lb == -1) || (rt == -2 && lt == -2)) {
                continue;
            }

            int[] area = {flagToPositionY(lt), flagToPositionY(rt), flagToPositionY(lb), flagToPositionY(rb), 0};
            areas.add(area);
        }
        for (int i = 0; i < inserts.size(); ) {
            int[] insert = inserts.get(i);
            int lb = lineViewportPositionY(sourceTextPane, sourceScrollPane.getViewport(), insert[0], false);
            int rb = lineViewportPositionY(targetTextPane, targetScrollPane.getViewport(), insert[1], false);
            int lt = lb;
            int rt = lineViewportPositionY(targetTextPane, targetScrollPane.getViewport(), insert[1], true);
            while (++i < inserts.size() && inserts.get(i)[1] == insert[1] - 1) {
                insert = inserts.get(i);
                rt = lineViewportPositionY(targetTextPane, targetScrollPane.getViewport(), insert[1], true);
            }
            if ((rb == -1 && lb == -1) || (rt == -2 && lt == -2)) {
                continue;
            }

            int[] area = {flagToPositionY(lt), flagToPositionY(rt), flagToPositionY(lb), flagToPositionY(rb), 1};
            areas.add(area);
        }

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int[] area : areas) {
            CubicCurve2D curve = new CubicCurve2D.Double();
            if (area[4] == 0) {
                graphics2D.setColor(Color.RED);
            } else if (area[4] == 1) {
                graphics2D.setColor(Color.GREEN);
            }
            curve.setCurve(0, area[0], 30, area[0], getSize().getWidth() - 30, area[1], getSize().getWidth(), area[1]);
            graphics2D.draw(curve);
            curve.setCurve(0, area[2], 30, area[2], getSize().getWidth() - 30, area[3], getSize().getWidth(), area[3]);
            graphics2D.draw(curve);
        }
    }
}
