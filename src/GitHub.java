import java.util.regex.Matcher;

import com.mak001.api.WebPage;
import com.mak001.api.plugins.Manifest;
import com.mak001.api.plugins.Plugin;
import com.mak001.api.plugins.listeners.MessageListener;
import com.mak001.ircbot.Bot;

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
		String contents = WebPage.downloadPage(link);
	}

	@SuppressWarnings("unused")
	private void doOutput(String link, String channel) {
		// TODO Auto-generated method stub
		String contents = WebPage.downloadPage(link);
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
