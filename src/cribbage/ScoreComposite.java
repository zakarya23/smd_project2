package cribbage;

import java.util.ArrayList;

public class ScoreComposite implements Score {
    private String name;
    private ArrayList<ScoreItem> scores;

    public ScoreComposite() {
        scores = new ArrayList<ScoreItem>();
    }


    @Override
    public int getScore() {
        int score = 0;
        for (ScoreItem item: scores) {
            score += item.point;
        }
        return score;
    }

    public void add(ScoreItem score) {
        scores.add(score);
    }

    public void remove(int index) {
        scores.remove(index);
    }
}
