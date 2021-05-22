package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public class ScoreCalculator {
    void calculate(String stage, int score) {

    }

    void addPoint(Hand hand) {
        System.out.println(hand.extractPairs()[0]);
    }

    void checkForPairs(Hand segment) {
        if(segment.isEmpty() || segment.getNumberOfCards() < 2) {
            return;
        }

        Card lastCardPlayed = segment.getLast();

        System.out.println(lastCardPlayed.getRank());
        System.out.println(segment.get(segment.getNumberOfCards() - 2));

        if(lastCardPlayed.getRank().equals(segment.get(segment.getNumberOfCards() - 2).getRank())) {
//            System.out.println(lastCardPlayed.getRank());
//            System.out.println(segment.get(segment.getNumberOfCards() - 1));
        }
    }
}
