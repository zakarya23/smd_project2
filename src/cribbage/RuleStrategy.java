package cribbage;

import ch.aplu.jcardgame.Hand;

public interface RuleStrategy {
    public int getScore(Hand hand, String phase);
}
