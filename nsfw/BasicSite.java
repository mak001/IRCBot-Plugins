import com.mak001.ircBot.Bot;

public abstract class BasicSite {

	protected final Bot bot;
	protected final NSFW plugin;
	protected final String flag;

	public BasicSite(Bot bot, NSFW plugin, String flag) {
		this.bot = bot;
		this.plugin = plugin;
		this.flag = flag;
	}

	public String getFlag() {
		return flag;
	}

	/**
	 * 
	 * @param target
	 *            - the target to message if something goes horribly wrong
	 * @return A string with the tag [NSFW] and a link to the image
	 */
	public abstract String getRandom(String target);

	/**
	 * 
	 * @param target
	 *            - the target to message if something goes horribly wrong
	 * @param value
	 *            - something to detail tags to search or subreddits
	 * @return A string with the tag [NSFW] and a link to the image
	 */
	public abstract String getImage(String target, String value);

}
