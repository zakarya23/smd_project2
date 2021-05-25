package cribbage;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;

public interface RuleStrategy {
    public void getAllScores(String phase, Hand hand, Card starter, IPlayer current, IPlayer other);
}
