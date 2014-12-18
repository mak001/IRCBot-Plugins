import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.Colors;

import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Manifest;
import com.mak001.ircBot.plugins.Plugin;
import com.mak001.ircBot.plugins.listeners.MessageListener;

@Manifest(authors = { "mak001" }, name = "Newgrounds link analizer")
public class NewGrounds extends Plugin implements MessageListener {

	private BufferedReader br = null;

	private final String NG_SITE = "http://www.newgrounds.com/";
	private final String AUTHOR_PATTERN = "<em>Author <a href=\"[^>]*\">([-a-zA-Z0-9]*)</a></em>";
	private final String COMPOSER_PATTERN = "<em>Composer <a href=\"[^>]*\">([-a-zA-Z0-9]*)</a></em>";
	// "artist":.*\"(.*)\"
	private final String RATING_PATTERN = "<dd class=\"star-variable\" .*title=\"(\\d*\\.\\d*).* Votes\">";
	private final String PLAYS_PATTERN = "<dd><strong>([1-9](?:\\d{0,2})(?:,\\d{3})*(?:\\.\\d*[1-9])?|0?\\.\\d*[1-9]|0)</strong>";
	private final String NAME_PATTERN = "<title>(.*)</title>";

	private final Pattern authorPattern = Pattern.compile(AUTHOR_PATTERN);
	private final Pattern composerPattern = Pattern.compile(COMPOSER_PATTERN);
	private final Pattern namePattern = Pattern.compile(NAME_PATTERN);
	private final Pattern ratingPattern = Pattern.compile(RATING_PATTERN);
	private final Pattern playsPattern = Pattern.compile(PLAYS_PATTERN);

	public NewGrounds(Bot bot) {
		super(bot, "NG");
	}

	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.contains(NG_SITE)) {
			String link = "";
			String[] s = message.split(" ");
			for (String ss : s) {
				if (ss.contains(NG_SITE))
					link = ss;
			}

			if (link.contains("/audio/listen/")) {
				doOutput(link, "Audio", channel);
			} else {
				doOutput(link, "Portal", channel);
			}
		}

	}

	private void doOutput(String link, String portal, String channel) {
		String page = downloadPage(link);
		String author = "Not found";
		String composer = "";
		String name = "null";
		String plays = "-1";
		String rating = "N/A";

		String ratingColor = Colors.NORMAL;
		try {
			Matcher authorMatcher = authorPattern.matcher(page);
			Matcher composerMatcher = composerPattern.matcher(page);
			Matcher nameMatcher = namePattern.matcher(page);
			Matcher ratingMatcher = ratingPattern.matcher(page);
			Matcher playsMatcher = playsPattern.matcher(page);

			author = findExpression(authorMatcher);
			composer = findExpression(composerMatcher);
			name = findExpression(nameMatcher);
			plays = findExpression(playsMatcher);
			rating = findExpression(ratingMatcher);

			if (rating == null || rating.equalsIgnoreCase("n/a")) {
				rating = "-1";
			}
			try {
				float i = Float.parseFloat(rating);
				if (i < 2.5f) {
					ratingColor = Colors.RED;
				} else if (i < 4f) {
					ratingColor = Colors.YELLOW;
				} else {
					ratingColor = Colors.GREEN;
				}
			} catch (NumberFormatException ne) {
				ne.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String authorString = null;
		if (author == null) {
			authorString = composer;
		} else {
			authorString = author;
		}

		if (bot.getChannelByName(channel).hasMode('c')) {
			bot.sendMessage(channel, "NG " + portal + " -- " + name + "  by: " + authorString + " -- Plays: " + plays + " -- " + "Rating: " + rating
					+ "/5");
		} else {
			bot.sendMessage(channel, Colors.YELLOW + "NG " + portal + Colors.NORMAL + " -- " + name + "  by: " + authorString + " -- Plays: " + plays
					+ " -- " + ratingColor + "Rating: " + rating + "/5");
		}
	}

	private String downloadPage(final String link) {
		StringBuilder s = new StringBuilder();
		long timeOut = System.currentTimeMillis() + 1500;
		try {
			URL url = new URL(link);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");

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

	private String findExpression(Matcher matcher) {
		String s = null;
		while (matcher.find()) {
			s = matcher.group(1);
		}
		return s;
	}
}
