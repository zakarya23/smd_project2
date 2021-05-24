package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;

public class TraditionalRule implements RuleStrategy {

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

    public enum Phase {
        STARTER(new Point[]{Point.STARTER}),
        PLAY(new Point[]{Point.FIFTEEN, Point.THIRTYONE, Point.GO, Point.RUN3, Point.RUN4, Point.RUN5, Point.RUN6, Point.RUN7, Point.PAIR2, Point.PAIR3, Point.PAIR4}),
        SHOW(new Point[]{Point.FIFTEEN, Point.RUN3, Point.RUN4, Point.RUN5, Point.PAIR2, Point.PAIR3, Point.PAIR4, Point.FLUSH4, Point.FLUSH5, Point.JACK});

        public final Point[] rules;

        Phase(Point[] rules) {
            this.rules = rules;
        }
    }
    // returns a Score object that comprises all the scoreItems applicable in a given turn
    public Score getAllScores(String phase, Hand hand, Card starter) {
        ScoreComposite score = new ScoreComposite(phase);
        switch (phase) {
            case "starter":
                for (Point rule : Phase.STARTER.rules) {
                    score.add(getScore(rule, hand, starter));
                }
                break;
            case "play":
                for (Point rule : Phase.PLAY.rules) {
                    score.add(getScore(rule, hand, starter));
                }
                break;
            case "show":
                for (Point rule : Phase.SHOW.rules) {
                    score.add(getScore(rule, hand, starter));
                }
                break;
            default:
                // do nothing
        }

        return score;
    }

    // returns a Score object of a specific type if the cards meet the criteria
    public Score getScore(Point type, Hand hand, Card starter) {
        Score score = null;
        switch (type) {
            case STARTER:
                score = getStarter(type, hand, starter);
                break;
            case FIFTEEN:
            case THIRTYONE:
                score = getTotals(type, hand, starter);
                break;
            case GO:
                score = getGo(type, hand, starter);
                break;
            case PAIR2:
            case PAIR3:
            case PAIR4:
                score = getPairs(type, hand, starter);
                break;
            case RUN3:
            case RUN4:
            case RUN5:
            case RUN6:
            case RUN7:
                score = getRuns(type, hand, starter);
                break;
            case FLUSH4:
            case FLUSH5:
                score = getFlushes(type, hand, starter);
                break;
            case JACK:
                score = getJack(type, hand, starter);
                break;
            default:
                // do nothing
        }

        return score;
    }

    // returns a starter ScoreItem if the starter card is a Jack
    public Score getStarter(Point type, Hand hand, Card starter) {
        if (starter.getRank().equals(Cribbage.Rank.JACK)) {
            ArrayList<Card> cards = new ArrayList<Card>();
            cards.add(starter);
            return new ScoreItem(type.name, type.points, cards);
        }
        return null;
    }

    // returns a fifteen or thirtyone ScoreItem if the values of the card add up to 15 or 31
    public Score getTotals(Point type, Hand hand, Card starter) {
        if (starter != null) {
            hand.insert(starter, false);
        }
        int total = hand.getScore();

        if (total == 15 || total == 31) {
            return new ScoreItem(type.name, type.points, hand.getCardList());
        }

        return null;
    }

    // returns a go ScoreItem if the values of the cards are below 31 but neither player can play another card
    public Score getGo(Point type, Hand hand, Card starter) {
        int total = hand.getScore();
        boolean go = true;
        ArrayList<Card> cards = hand.getCardList();
        for (Card card: cards) {
            if (card.getValue() + total < 31 );

        }

        return null;
    }

    // returns a Pair Score for any pairs in a given hand
    public Score getPairs(Point type, Hand hand, Card starter) {
        if (starter != null) {
            hand.insert(starter, false);
        }
        Hand[] pairs = null;

        switch (type) {
            case PAIR2:
                pairs = hand.extractPairs();
                break;
            case PAIR3:
                pairs = hand.extractTrips();
                break;
            case PAIR4:
                pairs = hand.extractQuads();
                break;
        }

        if (pairs == null) {
            return null;
        }

        ScoreComposite scoreComposite = new ScoreComposite(type.name);
        for (Hand pair : pairs) {
            scoreComposite.add(new ScoreItem(type.name, type.points, pair.getCardList()));
        }

        return scoreComposite;
    }

    // returns a runs Score for any runs in a given hand
    public Score getRuns(Point type, Hand hand, Card starter) {
        if (starter != null) {
            hand.insert(starter, false);
        }

        Hand[] runs = hand.extractSequences(type.points);

        if (runs.length == 0) {
            return null;
        }

        ScoreComposite scoreComposite = new ScoreComposite(type.name);

        for (Hand run : runs) {
            scoreComposite.add(new ScoreItem(type.name, type.points, run.getCardList()));
        }

        return scoreComposite;
    }

    // returns a flushes Score for any flushes in a given hand
    public Score getFlushes(Point type, Hand hand, Card starter) {
        int num;
        ScoreComposite scoreComposite = new ScoreComposite(type.name);


        for (Cribbage.Suit suit : Cribbage.Suit.values()) {
            num = hand.getNumberOfCardsWithSuit(suit);
            if (num == 4) {
                scoreComposite.add(new ScoreItem(type.name, type.points, hand.getCardList()));
                break;
            }
        }

        hand.insert(starter, false);

        for (Cribbage.Suit suit : Cribbage.Suit.values()) {
            num = hand.getNumberOfCardsWithSuit(suit);
            if (num == 5) {
                scoreComposite.add(new ScoreItem(type.name, type.points, hand.getCardList()));
                break;
            }
        }

        return scoreComposite;
    }

    //returns a jack Score if a given card has a Jack with the same suit as the starter card.
    public Score getJack(Point type, Hand hand, Card starter) {
        ArrayList<Card> cards = hand.getCardList();
        for (Card card: cards) {
            if (card.getRank().equals(Cribbage.Rank.JACK) && card.getSuit().equals(starter.getSuit())) {
                ArrayList<Card> jack = new ArrayList<Card>();
                cards.add(starter);
                return new ScoreItem(type.name, type.points, jack);
            }
        }
        return null;

    }

}