import com.mak001.cards.CardTable;
import com.mak001.cards.Cards;
import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Command;
import com.mak001.ircBot.plugins.Manifest;
import com.mak001.ircBot.plugins.Command.CommandAction;
import com.mak001.ircBot.plugins.Plugin;
import com.mak001.ircBot.plugins.listeners.MessageListener;

@Manifest(authors = { "mak001" }, name = "GOFISH", description = "Allows you to play go fish")
public class GoFish extends Plugin implements MessageListener {

	private CardTable<GoFishPlayer> table;
	private GoFishPlayer target;
	private int card_id = -1;
	private boolean gameRecruiting = false;
	private boolean gameGoing = false;

	public GoFish(Bot bot) {
		super(bot, "");
		bot.registerCommand(startGame);
		bot.registerCommand(joinGame);
		bot.registerCommand(leaveGame);
		bot.registerCommand(getCards);
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (gameGoing && table.getPlayers().contains(sender.toLowerCase())) {
			if (isCurrentPlayer(sender)) {
				for (GoFishPlayer player : table.getHands()) {
					if (hasNameAndCard(message, player)) {
						card_id = getCardID(message.toLowerCase().replace(player.getName().toLowerCase(), ""));
						if (currentGoFishPlayer().hasCard(card_id)) {
							target = player;
						} else {
							bot.sendMessage(channel, sender + " failed to ask " + player.getName() + " for "
									+ Cards.getByID(card_id).getSimpleCardName() + " because " + sender + "does not have the card being asked for.");
							card_id = -1;
							target = null;
						}

					}
				}
			} else if (isSender(sender)) {
				if (message.toLowerCase().contains("go fish")) {
					handleGoFish(channel, sender, message);
				} else {
					currentGoFishPlayer().takeCardsFromHand(card_id, target, channel);
					target = null;
					cycle(channel);
				}
			}
		}
	}

	private boolean hasNameAndCard(String message, GoFishPlayer player) {
		return message.toLowerCase().contains(player.getName().toLowerCase())
				&& containsCard(message.toLowerCase().replace(player.getName().toLowerCase(), ""));
	}

	private boolean isCurrentPlayer(String sender) {
		return currentGoFishPlayer() != null && sender.equalsIgnoreCase(currentGoFishPlayer().getName());
	}

	private boolean isSender(String sender) {
		return target != null && sender.equalsIgnoreCase(target.getName());
	}

	private void handleGoFish(String channel, String sender, String message) {
		currentGoFishPlayer().goFish(channel);
		for (Cards card : target.getCards()) {
			if (card.getCardValue() == card_id) {
				target.cheated();
				bot.sendNotice(target.getName(), "You cheated, but i wont tell anyone.");
				break;
			}
		}
		target = null;
		cycle(channel);
	}

	private int getCardID(String message) {
		for (int id = 0; id < Cards.names.length; id++) {
			if (containsCard(message, id + 1)) {
				return id + 1;
			}
		}
		return -1;
	}

	private boolean containsCard(String message) {
		for (int id = 0; id < Cards.names.length; id++) {
			if (containsCard(message, id + 1)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsCard(String message, int id) {
		return message.toLowerCase().contains(Cards.names[id].toLowerCase()) || message.toLowerCase().contains((id) + "");
	}

	private void calculateWinner(String channel) {
		for (GoFishPlayer GoFishPlayer : table.getHands()) {
			GoFishPlayer.points();
			// TODO
		}

		table.resetTable();

		table = null;
	}

	private int cycle_number = 0;

	private GoFishPlayer cycle(String channel) {
		if (cycle_number == table.getPlayers().size() - 1) {
			cycle_number = 0;
		} else {
			cycle_number++;
		}
		bot.sendMessage(channel, "It is now " + table.getHandsAndNames().getAt(cycle_number).getName() + "'s turn.");
		return table.getHandsAndNames().getAt(cycle_number);
	}

	private GoFishPlayer currentGoFishPlayer() {
		return table.getHandsAndNames().getAt(cycle_number);
	}

	private void dealCards(String channel) {
		for (int i = 0; i < 5; i++) {
			for (GoFishPlayer GoFishPlayer : table.getHands()) {
				GoFishPlayer.takeCardFromDeck();
			}
		}
		for (GoFishPlayer GoFishPlayer : table.getHands()) {
			bot.sendNotice(GoFishPlayer.getName(), GoFishPlayer.listCards());
			GoFishPlayer.makeSet(channel);
		}
	}

	public boolean hasPlayer(String name) {
		return table.getHands().contains(name.toLowerCase());
	}

	public GoFishPlayer getPlayer(String name) {
		return table.getHand(name.toLowerCase());
	}

	public GoFishPlayer getPlayer(int number) {
		return table.getHand(table.getHandsAndNames().getAt(number).getName());
	}

	private Command startGame = new Command(this, "GOFISH START", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			if (gameRecruiting && !gameGoing) {
				if (table.getHandsAndNames().size() < 2) {
					bot.sendMessage(channel, "There are not enough GoFishPlayers to forcefully start the game.");
				} else {
					gameRecruiting = false;
					gameGoing = true;
					bot.sendMessage(channel, "Starting the game.");

					dealCards(channel);
					cycle(channel);
				}
			} else if (!gameGoing && !gameRecruiting) {
				bot.sendMessage(channel, "There is a game now recruiting.");
				gameRecruiting = true;
				table = new CardTable<GoFishPlayer>();
				table.getDeck().shuffle(5);
			} else {
				bot.sendMessage(channel, "There is a game already going.");
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			// TODO
		}
	});

	private Command leaveGame = new Command(this, "GOFISH LEAVE", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			for (GoFishPlayer GoFishPlayer : table.getHands()) {
				if (GoFishPlayer.getName().equalsIgnoreCase(sender)) {
					GoFishPlayer.reset();
					table.removeHand(GoFishPlayer.getName());
					table.getHands().remove(GoFishPlayer);
					table.getDeck().shuffle(2);
					bot.sendMessage(channel, sender + " has left the game. Their cards have been re-added to the deck"
							+ " and the deck has been re-shuffled.");
				}
			}
			if (table.getHands().size() < 2) {
				calculateWinner(channel);
				gameGoing = false;
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			// TODO
		}
	});

	private Command joinGame = new Command(this, "GOFISH JOIN", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			for (GoFishPlayer GoFishPlayer : table.getHands()) {
				if (GoFishPlayer.getName().equalsIgnoreCase(sender)) {
					bot.sendMessage(sender, "You are already in the game.");
					return;
				}
			}
			if (gameRecruiting) {
				table.addHand(sender, new GoFishPlayer(sender.toLowerCase(), table, bot));
				bot.sendMessage(channel, sender + " has joined the game.");
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			// TODO
		}
	});

	private Command getCards = new Command(this, "GOFISH CARDS", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {

			for (String player : table.getPlayers()) {
				System.out.println(player);
			}

			if (table.getPlayers().contains(sender.toLowerCase())) {
				bot.sendNotice(sender, table.getHand(sender).listCards());
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			// TODO Auto-generated method stub

		}
	});
}
