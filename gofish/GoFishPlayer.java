import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mak001.cards.CardTable;
import com.mak001.cards.Cards;
import com.mak001.cards.Hand;
import com.mak001.ircBot.Bot;

public class GoFishPlayer extends Hand {

	private int set_length = 4;
	private int cheated = 0;
	private List<List<Cards>> sets = new ArrayList<List<Cards>>();
	private Bot bot;
	private String name;

	public GoFishPlayer(String name, CardTable<?> table, Bot bot) {
		super(table);
		this.bot = bot;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void goFish(String channel) {
		Cards card = takeCardFromDeck();
		bot.sendMessage(channel, getName() + " drew a card from the deck");
		bot.sendNotice(getName(), "you drew a " + card.getCardName());
		makeSet(channel);
	}

	public void takeCardsFromHand(int cardValue, GoFishPlayer player, String channel) {
		super.takeCardsFromHand(player, cardValue);
		makeSet(channel);
	}

	public boolean hasSetsInHand() {
		List<Integer> values = new ArrayList<Integer>();
		for (Cards card : getCards()) {
			values.add(card.getCardValue());
		}
		for (int value : values) {
			if (Collections.frequency(values, value) >= set_length) {
				return true;
			}
		}
		return false;
	}

	public List<Cards> makeSet(String channel) {
		List<Cards> cards = new ArrayList<Cards>();
		if (hasSetsInHand()) {
			List<Integer> values = new ArrayList<Integer>();
			for (Cards card : getCards()) {
				values.add(card.getCardValue());
			}
			for (int value : values) {
				if (Collections.frequency(values, value) >= set_length) {
					for (Cards card : getCards()) {
						if (card.getCardValue() == value) {
							cards.add(card);
							removeCard(card);
						}
					}
				}
			}
			sets.add(cards);
			bot.sendMessage(channel, getName() + " has made a set of " + cards.get(0).getCardValueString() + "s");
			return cards;
		}
		return null;
	}

	public int getTimesCheated() {
		return cheated;
	}

	public void cheated() {
		cheated++;
	}

	public int points() {
		return (sets.size() * 10) - (cheated * 20);
	}

	public void reset() {
		for (List<Cards> set : sets) {
			for (Cards card : set) {
				table.getDeck().addCard(card);
			}
		}
		sets.clear();
		super.reset();
	}

}
