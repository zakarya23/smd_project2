package cribbage;

import java.util.ArrayList;

public class ScoreComposite implements Score {
    private String name;
    private ArrayList<Score> scores;

    public ScoreComposite(String name) {
        this.name = name;
        this.scores = new ArrayList<Score>();
    }


    @Override
    public int getScore() {
        int score = 0;
        for (Score item: scores) {
            score += item.getScore();
        }
        return score;
    }

    public void add(Score score) {
        scores.add(score);
    }

    public void remove(int index) {
        scores.remove(index);
    }
}
