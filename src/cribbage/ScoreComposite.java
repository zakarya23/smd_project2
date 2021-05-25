package cribbage;

import java.util.ArrayList;

public class ScoreComposite implements Score {
    private String name;
    private ArrayList<Score> scores;

    public ScoreComposite(String name) {
        this.name = name;
        this.scores = new ArrayList<>();
    }


    @Override
    public int getScore() {
        int score = 0;
        if (scores.size() > 0) {
            for (Score item : scores) {
                if (item != null) {
                    score += item.getScore();
                }
            }
        }
        return score;
    }

    public void add(Score score) {
        scores.add(score);
        System.out.println("scores");
        System.out.println(scores);
    }

    public void remove(int index) {
        scores.remove(index);
    }
}
