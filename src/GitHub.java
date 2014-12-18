import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;

import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Manifest;
import com.mak001.ircBot.plugins.Plugin;
import com.mak001.ircBot.plugins.listeners.MessageListener;

@Manifest(authors = { "mak001" }, name = "Newgrounds link analizer")
public class GitHub extends Plugin implements MessageListener {

	private final String GITHUB_SITE = "http://github.com/";
	@SuppressWarnings("unused")
	private final String AUTHOR_PATTERN = "<em>Author <a href=\".*\">(.*)</a></em>";
	// "artist":.*\"(.*)\"
	@SuppressWarnings("unused")
	private final String RATING_PATTERN = "<dd class=\"star-variable\" .*title=\"(\\d*\\.\\d*).* Votes\">";
	@SuppressWarnings("unused")
	private final String PLAYS_PATTERN = "<dd><strong>([1-9](?:\\d{0,2})(?:,\\d{3})*(?:\\.\\d*[1-9])?|0?\\.\\d*[1-9]|0)</strong>";
	@SuppressWarnings("unused")
	private final String NAME_PATTERN = "<title>(.*)</title>";

	private BufferedReader br = null;

	public GitHub(Bot bot) {
		super(bot, "GIT");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		if (message.contains(GITHUB_SITE)) {
			System.out.println("NG link metioned");
			String link = "";
			String[] s = message.split(" ");
			for (String ss : s) {
				if (ss.contains(GITHUB_SITE))
					link = ss;
			}

			if (link.contains("/blob/")) {
				doBlobOutput(link, channel);
			} else {
				doOutput(link, channel);
			}
		}
	}

	@SuppressWarnings("unused")
	private void doBlobOutput(String link, String channel) {
		// TODO Auto-generated method stub
		String contents = downloadPage(link);
	}

	@SuppressWarnings("unused")
	private void doOutput(String link, String channel) {
		// TODO Auto-generated method stub
		String contents = downloadPage(link);
	}

	private String downloadPage(final String link) {
		System.out.println("Downloading page html");
		StringBuilder s = new StringBuilder();
		long timeOut = System.currentTimeMillis() + 1500;
		try {
			br = new BufferedReader(new InputStreamReader(
					new URL(link).openStream()));
			String line;
			while (br != null && (line = br.readLine()) != null) {
				if (line.isEmpty())
					continue;
				if (timeOut <= System.currentTimeMillis()) {
					System.out.println("Timed out");
					br.close();
					br = null;
					break;
				}
				s.append(line);
			}
			System.out.println("Done downloading page");
			if (br != null)
				br.close();
			br = null;
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unused")
	private String findExpression(Matcher matcher) {
		String s = "null";
		while (matcher.find()) {
			return matcher.group(1);
		}
		return s;
	}
}
