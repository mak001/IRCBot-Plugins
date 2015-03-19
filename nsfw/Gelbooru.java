import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mak001.api.plugins.Command;
import com.mak001.api.plugins.Command.CommandAction;
import com.mak001.ircbot.Bot;

public class Gelbooru extends BasicSite {

	private final String SEARCH = "http://gelbooru.com/index.php?page=dapi&s=post&q=index";

	public Gelbooru(Bot bot, NSFW plugin) {
		super(bot, plugin, "-g", "[NSFW][Gelbooru] ");

		bot.getPluginManager().registerCommand(gelbooru);
	}

	@Override
	public String getRandom(String target) {
		return readXML("rating:explicit", -1, -1, 0);
	}

	private Command gelbooru = new Command(plugin, new String[] { "GELBOORU", "GB" }, new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String target = channel == null ? sender : channel;
			if (additional != null && !additional.equals("")) {
				bot.sendMessage(target, getTag() + getImage(target, additional));
			} else {
				bot.sendMessage(target, getTag() + getRandom(target));
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			String target = channel == null ? sender : channel;
			bot.sendMessage(target, "Gets an image from gelbooru");
			// TODO
		}
	});

	@Override
	public String getImage(String target, String value) {
		return readXML(value, -1, -1, 0);
	}

	/**
	 * 
	 * @param additional
	 *            - The search parameters to use
	 * @param page
	 *            - The page to use. If the page is above the number of pages
	 *            the search has gelbooru will return the last page. Use -1 for
	 *            a random page
	 * @param tries
	 *            - The number of times the method has run recursively (Only
	 *            input 0 when used in other methods)
	 * @param rand_limit
	 *            - The max amount of pages the tag has (only used after
	 *            failing)
	 * @return - A random image link
	 */
	private String readXML(String additional, int page, int rand_limit, int tries) {
		int _page;
		if (page == -1) {
			if (rand_limit == -1) {
				_page = NSFW.randInt(0, 50);
			} else if (rand_limit == 0) {
				_page = 0;
			} else {
				_page = NSFW.randInt(0, rand_limit);
			}
		} else {
			_page = page;
		}

		if (additional.startsWith(" ")) {
			additional.replaceFirst(" ", "");
		}

		if (!additional.contains("loli") && !additional.contains("-loli")) {
			additional = additional + "+-loli";
		}
		if (additional.contains("loli") && !additional.contains("-loli") && !additional.contains("rating:")) {
			additional = additional + "+rating:safe";
		}

		additional.replace(" ", "+");

		System.out.println(tries + " -- " + SEARCH + "&tags=" + additional + "&pid=" + _page + "&limit=100");
		if (tries == 5) {
			return "There was an error.";
		} else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new URL(SEARCH + "&tags=" + additional + "&pid=" + _page + "&limit=100").openStream());
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("post");
				if (nList.getLength() > 2) {
					return ((Element) getRandNode(nList)).getAttribute("file_url");
				} else if (nList.getLength() == 1) {
					return ((Element) nList.item(0)).getAttribute("file_url");
				} else {
					int num = Integer.parseInt(doc.getDocumentElement().getAttribute("count"));
					if (num == 0) {
						return "No images found";
					} else {

						int lim = num / 100;
						System.out.println("num: " + num + "  -----  lim: " + lim);
						return readXML(additional, page, lim, tries + 1);
					}
				}
			} catch (Exception e) {
				if (tries < 5) {
					return readXML(additional, page, rand_limit, tries + 1);
				}
				e.printStackTrace();
				return "There was an error";
			}
		}
	}

	private Node getRandNode(NodeList nList) {
		int rand = NSFW.randInt(0, nList.getLength() - 1);
		return nList.item(rand);
	}
}
