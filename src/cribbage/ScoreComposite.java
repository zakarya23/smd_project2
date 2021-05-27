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
        System.out.println("POP");
        int score = 0;
        for (Score s: scores) {
            System.out.print("+ ");
            System.out.println(s.getScore());
        }
        return score;
    }

    public ArrayList<Score> getScores() {
        return scores;
    }

    public void add(Score score) {
        if (score != null) {
            scores.add(score);
            System.out.println(score.getClass());
            System.out.println(scores);
        }
    }
    // TRY TO FIX ARRAY
    public void remove(int index) {
        scores.remove(index);
    }
}
