import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stackia <jsq2627@gmail.com> on 10/6/14.
 * With IntelliJ IDEA.
 */
public class LevenshteinDistanceCalculator {
    private List source;
    private List target;
    private Results results = new Results();
    private boolean enableReplace = true;

    public void setSource(List source) {
        this.source = source;
    }

    public void setTarget(List target) {
        this.target = target;
    }

    public Results getResults() {
        return results;
    }

    public void setEnableReplace(boolean enableReplace) {
        this.enableReplace = enableReplace;
    }

    // Calculate Levenshtein distance and record progress.
    // Wagner–Fischer algorithm
    public void execute() {
        results = new Results();
        int[][] d = new int[source.size() + 1][target.size() + 1];
        int[][][] back = new int[source.size() + 1][target.size() + 1][2];
        int[][] backType = new int[source.size() + 1][target.size() + 1];

        for (int i = 0; i <= source.size(); i++) {
            d[i][0] = i;
        }

        for (int j = 0; j <= target.size(); j++) {
            d[0][j] = j;
        }

        for (int j = 1; j <= target.size(); j++) {
            for (int i = 1; i <= source.size(); i++) {
                if (source.get(i - 1).equals(target.get(j - 1))) { // Copy
                    d[i][j] = d[i - 1][j - 1];
                    back[i][j][0] = i - 1;
                    back[i][j][1] = j - 1;
                    backType[i][j] = 0;
                } else {
                    int del = d[i - 1][j] + 1;
                    int ins = d[i][j - 1] + 1;
                    int rep = d[i - 1][j - 1] + 1;

                    if (enableReplace) {
                        if (del < ins && del < rep) { // Delete
                            d[i][j] = del;
                            back[i][j][0] = i - 1;
                            back[i][j][1] = j;
                            backType[i][j] = 1;
                        } else if (ins < rep) { // Insert
                            d[i][j] = ins;
                            back[i][j][0] = i;
                            back[i][j][1] = j - 1;
                            backType[i][j] = 2;
                        } else { // Replace
                            d[i][j] = rep;
                            back[i][j][0] = i - 1;
                            back[i][j][1] = j - 1;
                            backType[i][j] = 3;
                        }
                    } else {
                        if (del < ins) { // Delete
                            d[i][j] = del;
                            back[i][j][0] = i - 1;
                            back[i][j][1] = j;
                            backType[i][j] = 1;
                        } else { // Insert
                            d[i][j] = ins;
                            back[i][j][0] = i;
                            back[i][j][1] = j - 1;
                            backType[i][j] = 2;
                        }
                    }
                }
            }
        }

        // Trace back
        int i, j;
        for (i = source.size(), j = target.size(); i > 0 && j > 0; ) {
            int[] pos = {i - 1, j - 1};
            switch (backType[i][j]) {
                case 0:
                    results.copy.add(pos);
                    break;
                case 1:
                    results.delete.add(pos);
                    break;
                case 2:
                    results.insert.add(pos);
                    break;
                case 3:
                    results.replace.add(pos);
                    break;
                default:
                    break;
            }
            int ti = back[i][j][0];
            int tj = back[i][j][1];
            i = ti;
            j = tj;
        }
        while (i > 0) {
            int[] pos = {i - 1, 0};
            results.delete.add(pos);
            i--;
        }
        while (j > 0) {
            int[] pos = {0, j - 1};
            results.insert.add(pos);
            j--;
        }

        results.distance = d[source.size()][target.size()];
    }

    public class Results {
        public final List<int[]> copy = new ArrayList<int[]>();
        public final List<int[]> delete = new ArrayList<int[]>();
        public final List<int[]> insert = new ArrayList<int[]>();
        public final List<int[]> replace = new ArrayList<int[]>();
        public int distance;
    }
}
