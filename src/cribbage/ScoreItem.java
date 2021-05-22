package cribbage;

import ch.aplu.jcardgame.Card;

import java.util.ArrayList;

public class ScoreItem implements Score {
    String name;
    int point;
    ArrayList<Card> cards = new ArrayList<Card>();

    public ScoreItem(String name, int point, ArrayList<Card> cards) {
        this.name = name;
        this.point = point;
        this.cards = cards;
    }
}
