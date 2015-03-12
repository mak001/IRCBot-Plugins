import com.mak001.ircbot.Bot;

public abstract class BasicSite {

	protected final Bot bot;
	protected final NSFW plugin;
	protected final String flag;
	protected final String tag;

	/**
	 * Only use this if you plan to have a dynamic tag
	 * @param bot
	 * @param plugin
	 * @param flag
	 */
	public BasicSite(Bot bot, NSFW plugin, String flag) {
		this(bot, plugin, flag, null);
	}

	public BasicSite(Bot bot, NSFW plugin, String flag, String tag) {
		this.bot = bot;
		this.plugin = plugin;
		this.flag = flag;
		this.tag = tag;
	}

	/**
	 * @return The tag to put in front of all nsfw images from the site
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * 
	 * @return The flag of the site [reddit is -r, gelbooru is -g]
	 */
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
