import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.Colors;

import com.mak001.api.WebPage;
import com.mak001.api.plugins.Manifest;
import com.mak001.api.plugins.Plugin;
import com.mak001.api.plugins.listeners.MessageListener;
import com.mak001.ircBot.Bot;

@Manifest(authors = { "mak001" }, name = "Newgrounds link analizer")
public class NewGrounds extends Plugin implements MessageListener {

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
		String page = WebPage.downloadPage(link);
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
	
	private String findExpression(Matcher matcher) {
		String s = null;
		while (matcher.find()) {
			s = matcher.group(1);
		}
		return s;
	}
}
