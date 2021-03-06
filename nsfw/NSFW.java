import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mak001.api.plugins.Command;
import com.mak001.api.plugins.Command.CommandAction;
import com.mak001.api.plugins.Manifest;
import com.mak001.api.plugins.Plugin;
import com.mak001.ircbot.Bot;
import com.mak001.ircbot.SettingsManager;

@Manifest(authors = { "mak001" }, name = "NSFW", description = "Looks up images [possibly nsfw] off of subreddits and other sites")
public class NSFW extends Plugin {

	private final static Random rand = new Random();

	private List<BasicSite> sites = new ArrayList<BasicSite>();

	private Reddit reddit;
	private Gelbooru gelbooru;

	public NSFW(Bot bot) {
		super(bot, "NSFW");
		reddit = new Reddit(bot, this);
		gelbooru = new Gelbooru(bot, this);
		sites.add(reddit);
		sites.add(gelbooru);
		bot.getPluginManager().registerCommand(nsfw);
	}

	private Command nsfw = new Command(this, "NSFW", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String target = channel == null ? sender : channel;
			if (additional != null && !additional.equals("")) {
				String[] info = additional.split(" ");
				if (info[0].equalsIgnoreCase(reddit.getFlag())) {
					bot.sendMessage(target, reddit.getImage(target, additional.replace(info[0] + " ", "")));

				} else if (info[0].equalsIgnoreCase(gelbooru.getFlag())) {
					bot.sendMessage(target, gelbooru.getTag() + gelbooru.getImage(target, additional.replace(info[0] + " ", "")));

				} else { // defaults to reddit
					if (additional == null || additional.equals(" ") || additional.equals("")) {
						bot.sendMessage(target, reddit.getRandom(target));
					} else {
						bot.sendMessage(target, reddit.getImage(target, additional));
					}
				}

			} else {
				BasicSite site = sites.get(randInt(0, sites.size() - 1));
				bot.sendMessage(target, (site.getTag() == null) ? site.getRandom(target) : site.getTag() + site.getRandom(target));
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "You may specify a subreddit to look for pictures in by using  " + SettingsManager.getCommandPrefix() + "NSFW "
					+ "<SUBREDDIT>");
		}
	});

	/**
	 * Returns a pseudo-random number between min and max, inclusive. The
	 * difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 * 
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value. Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
