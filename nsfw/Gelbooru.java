import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mak001.api.WebPage;
import com.mak001.api.plugins.Command;
import com.mak001.api.plugins.Command.CommandAction;
import com.mak001.ircBot.Bot;

public class Gelbooru extends BasicSite {

	private final String TAG = "[NSFW][Gelbooru]  ";
	private final String SEARCH = "http://gelbooru.com/index.php?page=dapi&s=post&q=index";
	private final String IMAGE_PAGE = "<img alt=\"[^\"]*\" height=\"[0-9]*\" src=\"([^\"]*)\" [^>]*>";
	private final Pattern imagePattern = Pattern.compile(IMAGE_PAGE);

	public Gelbooru(Bot bot, NSFW plugin) {
		super(bot, plugin, "-g");

		bot.registerCommand(gelbooru);
	}

	@Override
	public String getRandom(String target) {
		String link = getRandom(0);
		return link;
	}

	private String getRandom(int number) {
		if (number == 6) {
			return "Something went wrong with looking at gelbooru";
		}
		String page = WebPage.downloadPage("http://gelbooru.com/index.php?page=post&s=random");
		String link = findExpression(imagePattern.matcher(page));
		if (link != null) {
			return link;
		} else {
			return getRandom(number + 1);
		}
	}

	private Command gelbooru = new Command(plugin, new String[] { "GELBOORU", "GB" }, new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String target = channel == null ? sender : channel;
			if (additional != null && !additional.equals("")) {
				bot.sendMessage(target, TAG + getImage(target, additional));
			} else {
				bot.sendMessage(target, TAG + getRandom(target));
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
		return readXML(value, 0);
	}

	private String findExpression(Matcher matcher) {
		String s = null;
		while (matcher.find()) {
			s = matcher.group(1);
		}
		return s;
	}

	private String readXML(String additional, int tries) {
		if (additional.startsWith(" ")) {
			additional.replaceFirst(" ", "");
		}
		additional.replace(" ", "+");
		System.out.println(SEARCH + "&tags=" + additional);
		if (tries == 5) {
			return "There was an error.";
		} else {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new URL(SEARCH + "&tags=" + additional).openStream());
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("post");
				if (nList.getLength() > 2) {
					// TODO
					return ((Element) getRandNode(nList)).getAttribute("file_url");
				} else if (nList.getLength() == 1) {
					return ((Element) nList.item(0)).getAttribute("file_url");
				} else {
					return "No images found";
				}
			} catch (Exception e) {
				// if (tries != 5) {
				// return readXML(additional, tries++);
				// }
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
