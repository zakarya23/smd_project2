package cribbage;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;

public class ScoreItem implements Score {
    String name;
    int points;
    ArrayList<Card> cards = new ArrayList<Card>();

    public ScoreItem(String name, int points, ArrayList<Card> cards) {
        this.name = name;
        this.points = points;
        this.cards = cards;
    }

    @Override
    public String toString() {
        return points + "," + name + "," + cards.toString();
    }

}
