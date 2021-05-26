package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public interface RuleStrategy {
    public Score getAllScores(String phase, Hand hand, Hand starter);
}
