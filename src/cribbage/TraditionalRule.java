package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Deck;
import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class TraditionalRule implements RuleStrategy {

    private final Deck deck;
    private boolean pairFound = false;
    private boolean runFound = false;

    public TraditionalRule(Deck deck) {
        this.deck = deck;
    }

    // A point contains the name and number of points for each type of score
    public enum Point {
        STARTER("starter", 2, 1),
        PAIR2("pair2", 2, 2), PAIR3("pair3", 6, 3), PAIR4("pair4", 12, 4),
        RUN3("run3", 3, 3), RUN4("run4", 4, 4), RUN5("run5", 5, 5), RUN6("run6", 6, 6), RUN7("run7", 7, 7),
        FIFTEEN("fifteen", 2, 15), THIRTYONE("thirtyone", 2, 31), GO("go", 1, 1),
        FLUSH4("flush4", 4, 4), FLUSH5("flush5", 5, 5),
        JACK("jack", 1, 1);

        public final String name;
        public final int points;
        public final int num;

        Point(String name, int points, int num) {
            this.name = name;
            this.points = points;
            this.num = num;
        }
    }

    // A phase contains an array of the type of points that can be scored while the phase is active
    public enum Phase {
        STARTER(new Point[]{Point.STARTER}),
        PLAY(new Point[]{Point.FIFTEEN, Point.THIRTYONE, Point.GO, Point.RUN7, Point.RUN6, Point.RUN5, Point.RUN4, Point.RUN3, Point.PAIR4, Point.PAIR3, Point.PAIR2}),
        SHOW(new Point[]{Point.FIFTEEN, Point.RUN3, Point.RUN4, Point.RUN5, Point.PAIR2, Point.PAIR3, Point.PAIR4, Point.FLUSH5, Point.FLUSH4, Point.JACK});

        public final Point[] rules;

        Phase(Point[] rules) {
            this.rules = rules;
        }
    }

    // returns a Score object that comprises all the scoreItems applicable in a given turn
    public ScoreComposite getAllScores(String phase, Hand hand, Hand starter) {
        Card starterCard = null;
        if (starter != null) {
            starterCard = starter.getFirst();
        }

        ScoreComposite score = new ScoreComposite(phase);
        switch (phase) {
            case "starter":
                for (Point rule : Phase.STARTER.rules) {
                    score.add(getScore(rule, hand, starterCard));
                }
                break;
            case "play":
                for (Point rule : Phase.PLAY.rules) {
                    // Unable to check for pairs once a pair is found
                    if(!rule.name.contains("pair") && !rule.name.contains("run")) {
                        score.add(getScore(rule, hand, starterCard));
                    } else if((!pairFound && rule.name.contains("pair")) || (!runFound && rule.name.contains("run"))) {
                        score.add(getScore(rule, hand, starterCard));
                    }
                }
                break;
            case "show":
                for (Point rule : Phase.SHOW.rules) {
                    score.add(getScore(rule, hand, starterCard));
                }
                break;
            default:
                // do nothing
        }

        pairFound = false;
        runFound = false;

        return score;
    }

    // returns a Score object of a specific type if the cards meet the criteria
    public Score getScore(Point type, Hand hand, Card starter) {
        Score score = null;
        switch (type) {
//            case STARTER:
//                score = getStarter(hand, starter);
//                break;
            case FIFTEEN:
            case THIRTYONE:
                score = getTotals(type, hand, starter);
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
    public Score getStarter(Hand starter) {
        if(starter.getFirst().getRank().equals(Cribbage.Rank.JACK)) {
            ArrayList<Card> cards = new ArrayList<Card>();
            cards.add(starter.getFirst());
            return new ScoreItem(Point.STARTER.name, Point.STARTER.points, cards);
        }

        return null;
    }

    // returns the total card value of an array of cards
    public int totalValue(Card[] cards) {
        int total = 0;
        for(Card c: cards) {
            total += Cribbage.cardValue(c);
        }
        return total;
    }

    // returns a fifteen or thirty-one ScoreItem if the values of the card add up to 15 or 31
    public Score getTotals(Point type, Hand hand, Card starter) {
        if (starter != null ) { // then phase == show
            hand.insert(starter, false);
            ScoreComposite scoreComposite = new ScoreComposite(type.name);

            // get all combinations of 15
            ArrayList<Card[]> combos = getCombinations(hand.getCardList());
            for(Card[] combo: combos) {

                if (type.equals(Point.FIFTEEN) && totalValue(combo) == Point.FIFTEEN.num)  {
                    ArrayList<Card> cards = new ArrayList<Card>(Arrays.asList(combo));
                    scoreComposite.add(new ScoreItem(type.name, type.points, cards));
                }
            }
            if (scoreComposite.isEmpty()) {
                return null;
            }
            return scoreComposite;
        }

        // calculate the total card value of the hand and compare with fifteen or thiry-one
        if (Cribbage.total(hand) == type.num)  {
            return new ScoreItem(type.name, type.points, hand.getCardList());
        }

        return null;
    }

    // returns a go scoreItem
    public ScoreItem getGoScore(Hand hand) {
        return new ScoreItem(Point.GO.name, Point.GO.points, hand.getCardList());
    }

    // returns a scoreComposite with all the pairs found with or without a starter
    public Score getPairs(Point type, Hand hand, Card starter) {
        ScoreComposite scoreComposite = new ScoreComposite(type.name);
        if(starter != null) { // this is the show
            hand.insert(starter, false);

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

            if (pairs.length == 0) { // No pairs found
                return null;
            }

            for (Hand pair : pairs) {
                scoreComposite.add(new ScoreItem(type.name, type.points, pair.getCardList()));
            }

            return scoreComposite;

        } else {
            Card lastCard = hand.getLast();

            // Used to store the pair found and for return as score.
            Hand result = new Hand(deck);

            int counter = 1;

            result.insert(lastCard,false);

            // Checks for pair from the last card in the hand.
            for(int numOfCards = hand.getNumberOfCards() - 1; numOfCards > 0; numOfCards--) {
                switch(type) {
                    case PAIR4:
                        // Checks if the card has the same rank as the last card played
                        if(hand.getCardList().get(numOfCards - 1).getRankId() == lastCard.getRankId()) {
                            // Stores a clone of the card checked in a newly created hand.
                            result.insert(hand.getCardList().get(numOfCards - 1).clone(),false);
                            counter++;
                            // Once counter reaches 4 the loop ends and returns a score.
                            if(counter == 4) {
                                scoreComposite.add(new ScoreItem(type.name, type.points, result.getCardList()));
                                pairFound = true;
                                return scoreComposite;
                            }
                        } else {
                            return null;
                        }
                        break;
                    case PAIR3:
                        // Checks if the card has the same rank as the last card played
                        if(hand.getCardList().get(numOfCards - 1).getRankId() == lastCard.getRankId()) {
                            // Stores a clone of the card checked in a newly created hand.
                            result.insert(hand.getCardList().get(numOfCards - 1).clone(),false);
                            counter++;
                            // Once counter reaches 4 the loop ends and returns a score.
                            if(counter == 3) {
                                scoreComposite.add(new ScoreItem(type.name, type.points, result.getCardList()));
                                pairFound = true;
                                return scoreComposite;
                            }
                        } else { // Adjacent cards in the hand do not have the same rank.
                            return null;
                        }
                        break;
                    case PAIR2:
                        // Checks if the card has the same rank as the last card played
                        if(hand.getCardList().get(numOfCards - 1).getRankId() == lastCard.getRankId()) {
                            // Stores a clone of the card checked in a newly created hand.
                            result.insert(hand.getCardList().get(numOfCards - 1).clone(),false);
                            counter++;
                            // Once counter reaches 4 the loop ends and returns a score.
                            if(counter == 2) {
                                scoreComposite.add(new ScoreItem(type.name, type.points, result.getCardList()));
                                pairFound = true;
                                return scoreComposite;
                            }
                        } else { // Adjacent cards in the hand do not have the same rank.
                            return null;
                        }
                        break;
                }
            }
        }
        return null;
    }

    // Card comparator for sorting cards by rank
    static class SortRank implements Comparator<Card> {
        public int compare(Card a, Card b) {
            return ((Cribbage.Rank) a.getRank()).order - ((Cribbage.Rank) b.getRank()).order;
        }
    }

    // Returns an arraylist of card sequences
    public ArrayList<ArrayList<Card>> getSequences(int length, Hand hand) {

        ArrayList<ArrayList<Card>> sequences = new ArrayList<ArrayList<Card>>();
        ArrayList<Card> cards = hand.getCardList();
        boolean isRun = true;

        cards.sort(new SortRank());
        for (int j = 1; j < cards.size(); j++) {
            if (!isAdjacent(cards.get(j), cards.get(j - 1))) {   // the cards are not in sequence
                isRun = false;
            }
        }
        if (isRun) {     // tmp is a sequence of the right length
            sequences.add(new ArrayList<>(cards));
            cards.clear();
        }

        return sequences;
    }

    // Returns an arraylist of card sequences
    public ArrayList<ArrayList<Card>> getSequencesShow(int length, Hand hand) {

        ArrayList<ArrayList<Card>> sequences = new ArrayList<ArrayList<Card>>();
        ArrayList<Card> cards = hand.getCardList();

        ArrayList<Card[]> combos = getCombinations(cards);
        for (Card[] combo: combos) {
            boolean isRun = true;

            if (combo.length >= length) {
                Arrays.sort(combo, new SortRank());
                for (int j = 1; j < combo.length; j++) {
                    if (!isAdjacent(combo[j], combo[j - 1])) {   // the cards are not in sequence
                        isRun = false;
                    }
                }
                if (isRun) {     // tmp is a sequence of the right length
                    sequences.add(new ArrayList<>(Arrays.asList(combo)));
                }
            }
        }
        // filter out runs that are already present in a larger run
        ArrayList<ArrayList<Card>> removalList = new ArrayList<ArrayList<Card>>();
        for (ArrayList<Card> current: sequences) {
            for (ArrayList<Card> next: sequences) {
                if ((!next.equals(current) && next.containsAll(current))) {
                    removalList.add(current);
                }
            }
        }
        // filter out runs that are the wrong length
        for (ArrayList<Card> current: sequences) {
            if (current.size() != length) {
                removalList.add(current);
            }
        }
        sequences.removeAll(removalList);
        return sequences;
    }

    // returns true if cards a and b are next to each other in sequence
    public boolean isAdjacent(Card a, Card b ) {
        return Math.abs(((Cribbage.Rank) a.getRank()).order - ((Cribbage.Rank) b.getRank()).order) == 1;
    }

    // returns a scoreComposite of all the runs found with or without a starter
    public Score getRuns(Point type, Hand hand, Card starter) {
        ScoreComposite scoreComposite = new ScoreComposite(type.name);

        if(starter != null) {

            hand.insert(starter,false);
            ArrayList<ArrayList<Card>> runs = null;

            switch (type) {
                case RUN5:
                    runs = getSequencesShow(5,hand);
                    break;
                case RUN4:
                    runs = getSequencesShow(4,hand);
                    break;
                case RUN3:
                    runs = getSequencesShow(3,hand);
                    break;
                default:
                    break;
            }

            // adds a new score to the score composite for every run found.
            for(ArrayList<Card> run: runs) {
                scoreComposite.add(new ScoreItem(type.name, type.points, run));
            }

            if (scoreComposite.isEmpty()) {
                return null;
            }

            return scoreComposite;
        } else {
            switch (type) {
                case RUN7:
                    return runAlgorithm(type,hand,7);
                case RUN6:
                    return runAlgorithm(type,hand,6);
                case RUN5:
                    return runAlgorithm(type,hand,5);
                case RUN4:
                    return runAlgorithm(type,hand,4);
                case RUN3:
                    return runAlgorithm(type,hand,3);
            }
        }
        return null;
    }

    // returns a score with runs found of a certain length
    private Score runAlgorithm(Point type, Hand hand, int runLength) {
        ArrayList<ArrayList<Card>> runs;
        ScoreComposite scoreComposite = new ScoreComposite(type.name);

        // Checks if hand has length longer or equal to the run length
        if (hand.getNumberOfCards() < runLength) {
            return null;
        }

        Hand cardToCheck = new Hand(deck);

        // Adds the last runLength cards to be checked in the hand to a temporary hand.
        cardToCheck.getCardList().addAll(hand.getCardList().subList(hand.getNumberOfCards() - runLength, hand.getNumberOfCards()));
        // Checks if the sublist contains a run
        if ((runs = getSequences(runLength, cardToCheck)).size() == 0) {
            return null;
        } else {
            scoreComposite.add(new ScoreItem(type.name, type.points, runs.get(0)));
            runFound = true;
            return scoreComposite;
        }
    }

    // returns a flush Score for any flushes in a given hand with or without a starter
    public Score getFlushes(Point type, Hand hand, Card starter) {
        int num;
        ScoreComposite scoreComposite = new ScoreComposite(type.name);

        switch(type) {
            case FLUSH4:
                hand.insert(starter, false);
                for (Cribbage.Suit suit : Cribbage.Suit.values()) {
                    num = hand.getNumberOfCardsWithSuit(suit);
                    if (num > 4) {
                        return null;
                    }
                }
                hand.remove(starter, false);
                for (Cribbage.Suit suit : Cribbage.Suit.values()) {
                    num = hand.getNumberOfCardsWithSuit(suit);
                    if (num == 4) {
                        scoreComposite.add(new ScoreItem(type.name, type.points, hand.getCardList()));
                        Hand asd = new Hand(deck);
                        asd.getCardList().addAll(((ScoreItem)scoreComposite.getScores().get(0)).getCards());
                        return scoreComposite;
                    }
                }
                break;
            case FLUSH5:
                hand.insert(starter, false);
                for (Cribbage.Suit suit : Cribbage.Suit.values()) {
                    num = hand.getNumberOfCardsWithSuit(suit);
                    if (num == 5) {
                        scoreComposite.add(new ScoreItem(type.name, type.points, hand.getCardList()));
                        return scoreComposite;
                    }
                }
                break;
        }

        if (scoreComposite.isEmpty()) {
            return null;
        } else {
            return scoreComposite;
        }
    }

    //returns a jack Score if a given card has a Jack with the same suit as the starter card.
    public Score getJack(Point type, Hand hand, Card starter) {
            ScoreComposite scoreComposite = new ScoreComposite(type.name);
            ArrayList<Card> cards = hand.getCardList();
            for (Card card : cards) {
                if (card.getRank().equals(Cribbage.Rank.JACK) && card.getSuit().equals(starter.getSuit())) {
                    ArrayList<Card> jack = new ArrayList<Card>();
                    jack.add(card);
                    scoreComposite.add(new ScoreItem(type.name, type.points, jack));
                    return scoreComposite;
                }
            }
        return null;
    }

    // Returns an arraylist of all combinations of a set of 5 cards
    public ArrayList<Card[]> getCombinations(ArrayList<Card> arr)  {
        ArrayList<Card[]> combos = new ArrayList<Card[]>();
        for (int r = 2; r<=5;r++) {

            switch (r) {
                case 2:
                    for (int i=0; i<5; i++) {
                        for (int j=i+1; j<5; j++) {
                            Card[] tmp = new Card[r];
                            tmp[0] = arr.get(i);
                            tmp[1] = arr.get(j);
                            combos.add(tmp);
                        }
                    }
                    break;
                case 3:
                    for (int i=0; i<5; i++) {
                        for (int j=i+1; j<5; j++) {
                            for (int k=j+1; k<5; k++) {
                                Card[] tmp = new Card[r];
                                tmp[0] = arr.get(i);
                                tmp[1] = arr.get(j);
                                tmp[2] = arr.get(k);
                                combos.add(tmp);
                            }
                        }
                    }
                    break;
                case 4:
                    for (int i=0; i<5; i++) {
                        for (int j=i+1; j<5; j++) {
                            for (int k=j+1; k<5; k++) {
                                for (int l=k+1; l<5; l++) {
                                    Card[] tmp = new Card[r];
                                    tmp[0] = arr.get(i);
                                    tmp[1] = arr.get(j);
                                    tmp[2] = arr.get(k);
                                    tmp[3] = arr.get(l);
                                    combos.add(tmp);
                                }
                            }
                        }
                    }
                    break;
                case 5:
                    Card[] tmp = new Card[r];
                    tmp[0] = arr.get(0);
                    tmp[1] = arr.get(1);
                    tmp[2] = arr.get(2);
                    tmp[3] = arr.get(3);
                    tmp[4] = arr.get(4);
                    combos.add(tmp);
                    break;
            }
        }
        return combos;
    }
}