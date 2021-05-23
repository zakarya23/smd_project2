package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.List;

public class TraditionalRule implements RuleStrategy {
    private final int PAIR2_POINT = 2;
    private final int STARTER_POINT = 2;

    public enum Point {
        STARTER("starter", 2),
        PAIR2("pair2", 2), PAIR3("pair3", 3), PAIR4("pair", 4),
        RUN3("run3", 3), RUN4("run4", 4), RUN5("run5", 5), RUN6("run6", 6), RUN7("run7", 7),
        FIFTEEN("fifteen", 2), THIRTYONE("thirtyone", 2), GO("go", 1),
        FLUSH4("flush4", 4), FLUSH5("flush5", 5),
        JACK("jack", 1);

        public final String name;
        public final int points;

        Point(String name, int points) {
            this.name = name;
            this.points = points;
        }
    }

    public int getScore(Hand hand, String phase) {
        switch(phase) {
            case "starter":
                // Check for jack in starter
                if(!hand.getCardsWithRank(Cribbage.Rank.JACK).isEmpty()) {
                    return STARTER_POINT;
                }
                break;
        }

        return 0;
    }

    public Score getScore(String phase, Card starter) {
        ScoreComposite scoreComposite = new ScoreComposite(phase);

        if (starter.getRank() == (Cribbage.Rank.JACK)) {
            scoreComposite.add(new ScoreItem("starter", STARTER_POINT, new ArrayList<>(List.of(starter)) ));
        }

        return scoreComposite;
    }

    public Score getScore(String phase, Hand play) {
        ScoreComposite scoreComposite = new ScoreComposite(phase);


        // totals
        if (play.getScore() == 15) {
            scoreComposite.add(new ScoreItem(Point.FIFTEEN.name, Point.FIFTEEN.points, play.getCardList() ));
        }
        if (play.getScore() == 31) {
            scoreComposite.add(new ScoreItem(Point.THIRTYONE.name, Point.THIRTYONE.points, play.getCardList() ));
        }

        // Pairs
        if (!play.getPairs().isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.PAIR2.name, Point.PAIR2.points, play.getCardList() ));
        }
        if (!play.getTrips().isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.PAIR3.name, Point.PAIR3.points, play.getCardList() ));
        }
        if (!play.getQuads().isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.PAIR4.name, Point.PAIR4.points, play.getCardList() ));
        }


        //runs
        if (!play.getSequences(3).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN3.name, Point.RUN3.points, play.getCardList() ));
        }
        if (!play.getSequences(4).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN4.name, Point.RUN4.points, play.getCardList() ));
        }
        if (!play.getSequences(5).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN5.name, Point.RUN5.points, play.getCardList() ));
        }
        if (!play.getSequences(6).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN6.name, Point.RUN6.points, play.getCardList() ));
        }
        if (!play.getSequences(7).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN7.name, Point.RUN7.points, play.getCardList() ));
        }



        return scoreComposite;
    }

    public Score getScore(String phase, Card starter, Hand playerHand, Hand crib) {
        ScoreComposite scoreComposite = new ScoreComposite(phase);
        playerHand.insert(starter,false);
        ArrayList<Card> cards;

        // totals
        if (playerHand.getScore() == 15) {
            scoreComposite.add(new ScoreItem(Point.FIFTEEN.name, Point.FIFTEEN.points, playerHand.getCardList() ));
        }

        //runs
        if (!playerHand.getSequences(3).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN3.name, Point.RUN3.points, playerHand.getCardList() ));
        }
        if (!playerHand.getSequences(4).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN4.name, Point.RUN4.points, playerHand.getCardList() ));
        }
        if (!playerHand.getSequences(5).isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.RUN5.name, Point.RUN5.points, playerHand.getCardList() ));
        }

        // Pairs
        if (!playerHand.getPairs().isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.PAIR2.name, Point.PAIR2.points, playerHand.getCardList() ));
        }
        if (!playerHand.getTrips().isEmpty()) {
            scoreComposite.add(new ScoreItem(Point.PAIR3.name, Point.PAIR3.points, playerHand.getCardList() ));
        }

        //flush
        if (playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.CLUBS) < 4 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.DIAMONDS) < 4 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.SPADES) < 4 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.HEARTS) < 4 ) {
            scoreComposite.add(new ScoreItem(Point.FLUSH4.name, Point.FLUSH4.points, playerHand.getCardList()));
        }


        if (playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.CLUBS) == 5 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.DIAMONDS) == 5 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.SPADES) == 5 ||
                playerHand.getNumberOfCardsWithSuit(Cribbage.Suit.HEARTS) == 5 ) {
            scoreComposite.add(new ScoreItem(Point.FLUSH5.name, Point.FLUSH5.points, playerHand.getCardList()));
        }

        if ( (cards.add(playerHand.getCard(starter.getSuit(), Cribbage.Rank.JACK))).isEmpty) {
            scoreComposite.add(new ScoreItem(Point.JACK.name, Point.JACK.points, playerHand.getCardList()));

        }



    }


}
