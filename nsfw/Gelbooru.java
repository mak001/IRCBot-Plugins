import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.net.www.protocol.http.HttpURLConnection;

import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Command;
import com.mak001.ircBot.plugins.Command.CommandAction;

public class Gelbooru extends BasicSite {

	private final String TAG = "[NSFW][Gelbooru]  ";
	private final String SEARCH = "http://gelbooru.com/index.php?page=dapi&s=post&q=index";
	private final String IMAGE_PAGE = "<img alt=\"[^\"]*\" height=\"[0-9]*\" src=\"([^\"]*)\" [^>]*>";
	private final Pattern imagePattern = Pattern.compile(IMAGE_PAGE);

	private BufferedReader br = null;

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
		String page = downloadPage("http://gelbooru.com/index.php?page=post&s=random");
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

	private String downloadPage(final String link) {
		StringBuilder s = new StringBuilder();
		long timeOut = System.currentTimeMillis() + 1500;
		try {
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");

			boolean redirect = false;

			int status = connection.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
					redirect = true;
			}
			if (redirect) {
				// get redirect url from "location" header field
				String newUrl = connection.getHeaderField("Location");
				// get the cookie if need, for login
				String cookies = connection.getHeaderField("Set-Cookie");
				// open the new connnection again
				connection = (HttpURLConnection) new URL(newUrl).openConnection();
				connection.setRequestProperty("Cookie", cookies);
				connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				connection.addRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
			}

			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while (br != null && (line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				if (timeOut <= System.currentTimeMillis()) {
					br.close();
					br = null;
					break;
				}
				s.append(line);
			}
			if (br != null)
				br.close();
			br = null;
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
