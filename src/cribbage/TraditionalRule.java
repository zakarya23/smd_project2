package cribbage;

import ch.aplu.jcardgame.Hand;

public class TraditionalRule implements RuleStrategy {
    private final int PAIR_POINT = 2;
    private final int STARTER_POINT = 2;

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
}
