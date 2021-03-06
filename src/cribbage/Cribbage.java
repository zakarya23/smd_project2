package cribbage;

// Cribbage.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.awt.Font;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cribbage extends CardGame {
	static Cribbage cribbage;  // Provide access to singleton

	public enum Suit {
		CLUBS, DIAMONDS, HEARTS, SPADES
	}

	public enum Rank {
		// Order of cards is tied to card images
		ACE(1,1), KING(13,10), QUEEN(12,10), JACK(11,10), TEN(10,10), NINE(9,9), EIGHT(8,8), SEVEN(7,7), SIX(6,6), FIVE(5,5), FOUR(4,4), THREE(3,3), TWO(2,2);
		public final int order;
		public final int value;
		Rank(int order, int value) {
			this.order = order;
			this.value = value;
		}
	}

	static int cardValue(Card c) { return ((Cribbage.Rank) c.getRank()).value; }

	/*
	Canonical String representations of Suit, Rank, Card, and Hand
	*/
	String canonical(Suit s) { return s.toString().substring(0, 1); }

	String canonical(Rank r) {
		switch (r) {
			case ACE:case KING:case QUEEN:case JACK:case TEN:
				return r.toString().substring(0, 1);
			default:
				return String.valueOf(r.value);
		}
	}

	String canonical(Card c) { return canonical((Rank) c.getRank()) + canonical((Suit) c.getSuit()); }

	String canonical(Hand h) {
		Hand h1 = new Hand(deck); // Clone to sort without changing the original hand
		for (Card C: h.getCardList()) h1.insert(C.getSuit(), C.getRank(), false);
		h1.sort(Hand.SortType.POINTPRIORITY, false);
		return "[" + h1.getCardList().stream().map(this::canonical).collect(Collectors.joining(",")) + "]";
	}

	class MyCardValues implements Deck.CardValues { // Need to generate a unique value for every card
		public int[] values(Enum suit) {  // Returns the value for each card in the suit
			return Stream.of(Rank.values()).mapToInt(r -> (((Rank) r).order-1)*(Suit.values().length)+suit.ordinal()).toArray();
		}
	}

	static Random random;

	public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
		int x = random.nextInt(clazz.getEnumConstants().length);
		return clazz.getEnumConstants()[x];
	}

	static boolean ANIMATE;

	void transfer(Card c, Hand h) {
		if (ANIMATE) {
			c.transfer(h, true);
		} else {
			c.removeFromHand(true);
			h.insert(c, true);
		}
	}

	private void dealingOut(Hand pack, Hand[] hands) {
		for (int i = 0; i < nStartCards; i++) {
			for (int j=0; j < nPlayers; j++) {
				Card dealt = randomCard(pack);
				dealt.setVerso(false);  // Show the face
				transfer(dealt, hands[j]);
			}
		}
	}

	static int SEED;

	public static Card randomCard(Hand hand){
		int x = random.nextInt(hand.getNumberOfCards());
		return hand.get(x);
	}

	private final String version = "0.1";
	static public final int nPlayers = 2;
	public final int nStartCards = 6;
	public final int nDiscards = 2;
	private final int handWidth = 400;
	private final int cribWidth = 150;
	private final int segmentWidth = 180;
	private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover", new MyCardValues());
	private final Location[] handLocations = {
			new Location(360, 75),
			new Location(360, 625)
	};
	private final Location[] scoreLocations = {
			new Location(590, 25),
			new Location(590, 675)
	};
	private final Location[] segmentLocations = {  // need at most three as 3x31=93 > 2x4x10=80
			new Location(150, 350),
			new Location(400, 350),
			new Location(650, 350)
	};
	private final Location starterLocation = new Location(50, 625);
	private final Location cribLocation = new Location(700, 625);
	private final Location seedLocation = new Location(5, 25);
	// private final TargetArea cribTarget = new TargetArea(cribLocation, CardOrientation.NORTH, 1, true);
	private final Actor[] scoreActors = {null, null}; //, null, null };
	private final Location textLocation = new Location(350, 450);
	private final Hand[] hands = new Hand[nPlayers];
	private Hand starter;
	private Hand crib;

	private int dealer = 1;
	private Hand[] startingHands = new Hand[nPlayers];
	private String logFileName = "cribbage.log";
	private File file = File.getInstance();

	public static void setStatus(String string) { cribbage.setStatusText(string); }

	static private final IPlayer[] players = new IPlayer[nPlayers];
	private final int[] scores = new int[nPlayers];

	final Font normalFont = new Font("Serif", Font.BOLD, 24);
	final Font bigFont = new Font("Serif", Font.BOLD, 36);

	// Scoring
	private TraditionalRuleStrategy ruleStrategy = new TraditionalRuleStrategy(deck);

	private void initScore() {
		for (int i = 0; i < nPlayers; i++) {
			scores[i] = 0;
			scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont);
			addActor(scoreActors[i], scoreLocations[i]);
		}
	}

	private void updateScoreGraphics(int player) {
		removeActor(scoreActors[player]);
		scoreActors[player] = new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont);
		addActor(scoreActors[player], scoreLocations[player]);
	}

	private void deal(Hand pack, Hand[] hands) {
		for (int i = 0; i < nPlayers; i++) {
			hands[i] = new Hand(deck);
			// players[i] = (1 == i ? new HumanPlayer() : new RandomPlayer());
			players[i].setId(i);
			players[i].startSegment(deck, hands[i]);
		}
		RowLayout[] layouts = new RowLayout[nPlayers];
		for (int i = 0; i < nPlayers; i++)
		{
			layouts[i] = new RowLayout(handLocations[i], handWidth);
			layouts[i].setRotationAngle(0);
			// layouts[i].setStepDelay(10);
			hands[i].setView(this, layouts[i]);
			hands[i].draw();
		}
		layouts[0].setStepDelay(0);

		dealingOut(pack, hands);
		for (int i = 0; i < nPlayers; i++) {
			hands[i].sort(Hand.SortType.POINTPRIORITY, true);

			// Adds the deal card event into the log.
			String eventText = "deal,P" + i + "," + canonical(hands[i]) +'\n';
			file.append(logFileName,eventText);
		}
		layouts[0].setStepDelay(0);
	}

	private void discardToCrib() {
		String event = "discard";

		crib = new Hand(deck);
		RowLayout layout = new RowLayout(cribLocation, cribWidth);
		layout.setRotationAngle(0);
		crib.setView(this, layout);
		// crib.setTargetArea(cribTarget);
		crib.draw();

		int playerNumber = 0;
		startingHands[0] = new Hand(deck);
		startingHands[1] = new Hand(deck);

		for (IPlayer player: players) {
			String eventMsg = event + ',' + 'P' + player.id + ',';

			Hand discardedCards = new Hand(deck);

			for (int i = 0; i < nDiscards; i++) {
				transfer(player.discard(), crib);

				discardedCards.getCardList().add(crib.getLast().clone());
			}


			for(int i = 0; i < hands[playerNumber].getNumberOfCards(); i++) {
				startingHands[playerNumber].getCardList().add(hands[playerNumber].get(i));
			}
			playerNumber++;

			crib.sort(Hand.SortType.POINTPRIORITY, true);

			eventMsg += canonical(discardedCards) + '\n';
			file.append(logFileName, eventMsg);
		}
	}

	private void starter(Hand pack) {
		String event = "starter";

		starter = new Hand(deck);  // if starter is a Jack, the dealer gets 2 points
		RowLayout layout = new RowLayout(starterLocation, 0);
		layout.setRotationAngle(0);
		starter.setView(this, layout);
		starter.draw();
		Card dealt = randomCard(pack);
		dealt.setVerso(false);
		transfer(dealt, starter);

		String eventMessage = event + ',' + canonical(starter.getFirst()) + '\n';
		file.append(logFileName,eventMessage);

		// Checks if starter has score.
		ScoreComposite starterScore = (ScoreComposite) ruleStrategy.getAllScores(event,starter,null);

		updateScore(starterScore,dealer);
	}

	static int total(Hand hand) {
		int total = 0;
		for (Card c: hand.getCardList()) total += cardValue(c);
		return total;
	}

	class Segment {
		Hand segment;
		boolean go;
		int lastPlayer;
		boolean newSegment;

		void reset(final List<Hand> segments) {
			segment = new Hand(deck);
			segment.setView(Cribbage.this, new RowLayout(segmentLocations[segments.size()], segmentWidth));
			segment.draw();
			go = false;        // No-one has said "go" yet
			lastPlayer = -1;   // No-one has played a card yet in this segment
			newSegment = false;  // Not ready for new segment yet
		}
	}

	private void play() {
		String playEvent = "play";
		String scoreEvent = "score";

		final int thirtyone = 31;
		List<Hand> segments = new ArrayList<>();
		int currentPlayer = 0; // Player 1 is dealer
		Segment s = new Segment();
		s.reset(segments);

		while (!(players[0].emptyHand() && players[1].emptyHand())) {
			// System.out.println("segments.size() = " + segments.size());

			String playerRepresentation = "P" + players[currentPlayer].id;
			String playEventMessage = playEvent + ',' + playerRepresentation + ',';
			String scoreEventMessage = scoreEvent + ',' + playerRepresentation + ',';

			Card nextCard = players[currentPlayer].lay(thirtyone-total(s.segment));
			if (nextCard == null) {
				if (s.go) {

					ScoreItem score = ruleStrategy.getGoScore(hands[s.lastPlayer]);

					if (score != null) {
						scores[s.lastPlayer] += score.getScore();
						updateScoreGraphics(s.lastPlayer);

						String message = scoreEventMessage + scores[s.lastPlayer] + ',' + score.getScore() + ",go\n";
						file.append(logFileName,message);

						// Another "go" after previous one with no intervening cards
						// lastPlayer gets 1 point for a "go"
					}
					s.newSegment = true;
				} else {
					// currentPlayer says "go"
					s.go = true;
				}
				currentPlayer = (currentPlayer+1) % 2;
			} else {
				s.lastPlayer = currentPlayer; // last Player to play a card in this segment
				transfer(nextCard, s.segment);

				playEventMessage += (total(s.segment) + "," + canonical(nextCard.clone()) + "\n");
				file.append(logFileName, playEventMessage);

				if (total(s.segment) == thirtyone) {
					// lastPlayer gets 2 points for a 31
					ScoreComposite scoresApplicable = (ScoreComposite) ruleStrategy.getAllScores(playEvent,s.segment,null);

					updateScore(scoresApplicable, s.lastPlayer);

					s.newSegment = true;
					currentPlayer = (currentPlayer+1) % 2;
				} else {
					// if total(segment) == 15, lastPlayer gets 2 points for a 15
					ScoreComposite scoresApplicable = (ScoreComposite) ruleStrategy.getAllScores(playEvent,s.segment,null);

					updateScore(scoresApplicable, s.lastPlayer);

					if (!s.go) { // if it is "go" then same player gets another turn
						currentPlayer = (currentPlayer + 1) % 2;
					}
				}
			}

			if (s.newSegment) {
				segments.add(s.segment);
				s.reset(segments);
			}
		}
	}

	void showHandsCrib() {
		String event = "show";
		String scoreEvent = "score";
		String scoreEventMessage = scoreEvent + ',';

		// score player 0 (non dealer)
		// score player 1 (dealer)
		for(int i = 0; i < nPlayers; i++) {
			String showMessage = event + ",P" + i + "," + canonical(starter.getFirst()) + '+' + canonical(startingHands[i]) + '\n';
			file.append(logFileName, showMessage);

			ScoreComposite score = (ScoreComposite) ruleStrategy.getAllScores(event, startingHands[i], starter);

			updateScore(score, i);
		}

		// score crib (for dealer)
		String showMessage = event + ",P1," + canonical(starter.getFirst()) + '+' + canonical(crib) + '\n';
		file.append(logFileName,showMessage);

		ScoreComposite score = ruleStrategy.getAllScores(event,crib,starter);

		updateScore(score,dealer);
		// all points that come out of show go to dealer!
	}

	void updateScore(ScoreComposite scoresApplicable, int playerNumber) {
		String playerRepresentation = "P" + players[playerNumber].id;
		String scoreEventMessage = "score" + ',' + playerRepresentation + ',';

		Iterator scoreIterator = scoresApplicable.getScores().iterator();

		// Loops through all score found.
		while(scoreIterator.hasNext()) {
			Score currentScore = (Score) scoreIterator.next();

			String scoreType = currentScore.getClass().getName();

			switch(scoreType) {
				case "cribbage.ScoreItem":
					ScoreItem scoreItem = (ScoreItem) currentScore;
					scores[playerNumber] += scoreItem.getScore();

					updateScoreGraphics(playerNumber);

					// Stores the score inside player class as score composite.
					players[playerNumber].addScore(scoreItem);

					// Logging message
					String message = scoreEventMessage + this.scores[playerNumber] + ',' + scoreItem.getScore() + ',' + ((ScoreItem) currentScore).getName() + '\n';
					file.append(logFileName, message);
					break;
				case "cribbage.ScoreComposite":
					ScoreComposite scoreComposite = (ScoreComposite) currentScore;
					Iterator scoreCompositeIterator = scoreComposite.getScores().iterator();

					while(scoreCompositeIterator.hasNext()) {
						scoreItem = (ScoreItem) scoreCompositeIterator.next();

						scores[playerNumber] += scoreItem.getScore();
						updateScoreGraphics(playerNumber);

						// Stores the score inside player class as score composite.
						players[playerNumber].addScore(scoreItem);

						// Used to store cards for logging.
						Hand scoreCondition = new Hand(deck);
						scoreCondition.getCardList().addAll(scoreItem.getCards());

						// Logging message
						message = scoreEventMessage + this.scores[playerNumber] + ',' + scoreItem.getScore() + ',' + scoreItem.getName() + ',' + canonical(scoreCondition)+ '\n';
						file.append(logFileName, message);
					}
					break;
			}
		}
	}

	public Cribbage()
	{
		super(850, 700, 30);
		cribbage = this;
		setTitle("Cribbage (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
		setStatusText("Initializing...");
		initScore();

		Hand pack = deck.toHand(false);
		RowLayout layout = new RowLayout(starterLocation, 0);
		layout.setRotationAngle(0);
		pack.setView(this, layout);
		pack.setVerso(true);
		pack.draw();
		addActor(new TextActor("Seed: " + SEED, Color.BLACK, bgColor, normalFont), seedLocation);

		/* Play the round */
		deal(pack, hands);
		discardToCrib();
		starter(pack);
		play();
		showHandsCrib();

		addActor(new Actor("sprites/gameover.gif"), textLocation);
		setStatusText("Game over.");
		refresh();
	}

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			InstantiationException, IllegalAccessException {
		/* Handle Properties */
		// System.out.println("Working Directory = " + System.getProperty("user.dir"));
		Properties cribbageProperties = new Properties();
		// Default properties
		cribbageProperties.setProperty("Animate", "true");
		cribbageProperties.setProperty("Player0", "cribbage.RandomPlayer");
		cribbageProperties.setProperty("Player1", "cribbage.HumanPlayer");

		// Read properties
		try (FileReader inStream = new FileReader("cribbage.properties")) {
			cribbageProperties.load(inStream);
		}

		// Control Graphics
		ANIMATE = Boolean.parseBoolean(cribbageProperties.getProperty("Animate"));

		// Control Randomisation
		/* Read the first argument and save it as a seed if it exists */
		if (args.length > 0 ) { // Use arg seed - overrides property
			SEED = Integer.parseInt(args[0]);
		} else { // No arg
			String seedProp = cribbageProperties.getProperty("Seed");  //Seed property
			if (seedProp != null) { // Use property seed
				SEED = Integer.parseInt(seedProp);
			} else { // and no property
				SEED = new Random().nextInt(); // so randomise
			}
		}
		random = new Random(SEED);

		// Control Player Types
		Class<?> clazz;
		clazz = Class.forName(cribbageProperties.getProperty("Player0"));
		players[0] = (IPlayer) clazz.getConstructor().newInstance();
		clazz = Class.forName(cribbageProperties.getProperty("Player1"));
		players[1] = (IPlayer) clazz.getConstructor().newInstance();
		// End properties

		String gameProperties = "seed," + SEED + "\n" +
				cribbageProperties.getProperty("Player0") + ",P0\n" +
				cribbageProperties.getProperty("Player1") + ",P1\n";

		File file = File.getInstance();
		file.clear("cribbage.log");
		file.append("cribbage.log",gameProperties);

		new Cribbage();
	}
}