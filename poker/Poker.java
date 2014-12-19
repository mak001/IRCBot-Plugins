import com.mak001.cards.CardTable;
import com.mak001.ircBot.Bot;
import com.mak001.api.plugins.Manifest;
import com.mak001.api.plugins.Plugin;
import com.mak001.api.plugins.listeners.MessageListener;

@Manifest(authors = { "mak001" }, name = "POKER", description = "Allows you to play poker")
public class Poker extends Plugin implements MessageListener {

	private CardTable<PokerPlayer> table;

	public Poker(Bot bot) {
		super(bot, "POKER");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		// TODO Auto-generated method stub

	}

}
