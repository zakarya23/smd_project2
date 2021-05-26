package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public interface RuleStrategy {
    public ScoreComposite getAllScores(String phase, Hand hand, Hand starter);
}
