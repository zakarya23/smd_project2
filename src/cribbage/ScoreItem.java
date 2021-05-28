package cribbage;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;

public class ScoreItem implements Score {
    String name;
    int score;
    ArrayList<Card> cards = new ArrayList<Card>();

    public ScoreItem(String name, int score, ArrayList<Card> cards) {
        this.name = name;
        this.score = score;
        this.cards = cards;
    }

    @Override
    public String toString() {
        return name + "," + cards.toString();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getScore() {
        return score;
    }
}
