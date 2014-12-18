import com.mak001.ircBot.Boot;
import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Command;
import com.mak001.ircBot.plugins.Command.CommandAction;
import com.mak001.ircBot.plugins.Plugin;
import com.mak001.ircBot.plugins.listeners.*;

public class TestPlugin extends Plugin implements NickChangeListener {

    public TestPlugin(Bot bot) {
	super(bot, "TEST");

	bot.registerCommand(test);
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
	bot.sendMessage(Boot.CHANNEL, oldNick + " changed to " + newNick);
    }

    private Command test = new Command(this, GENERAL_COMMAND, new CommandAction() {

	@Override
	public void onCommand(String channel, String sender, String login, String hostname, String additional) {
	    bot.sendMessage(sender, "WORKING");
	}

	@Override
	public void onHelp(String channel, String sender, String login, String hostname) {
	}

    });
}
