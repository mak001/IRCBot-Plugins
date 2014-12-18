import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Manifest;
import com.mak001.ircBot.plugins.Plugin;
import com.mak001.ircBot.plugins.listeners.*;
import com.mak001.ircBot.settings.Settings;

@Manifest(authors = { "mak001" }, name = "YouTube utility")
public class Youtube extends Plugin implements MessageListener {

	private BufferedReader br = null;

	@SuppressWarnings("unused")
	private final String WATCH_DESCRIPTION_PATTERN = "";
	@SuppressWarnings("unused")
	private final String WATCH_NAME_PATTERN = "";
	private final String WATCH_UPLOADER_PATTERN = "";
	@SuppressWarnings("unused")
	private final String WATCH_URL = "youtube.com/watch?";

	@SuppressWarnings("unused")
	private final String SEARCH_DESCRIPTION_PATTERN = "";
	@SuppressWarnings("unused")
	private final String SEARCH_NAME_PATTERN = "";
	@SuppressWarnings("unused")
	private final String SEARCH_LING_PATTERN = "";
	@SuppressWarnings("unused")
	private final String SEARCH_URL = "http://www.youtube.com/results?search_query=";

	public Youtube(Bot bot) {
		super(bot, "YouTube");
	}

	public void onCommand(String channel, String sender, String login,
			String hostname, String message) {
		String[] s = message.split(" ");
		int limit = -1;
		String search = "";
		try {
			limit = Integer.parseInt(s[s.length - 1]);
			search = message.replace(" " + s[s.length - 1], "");
		} catch (Exception e) {
			e.printStackTrace();
			limit = 5;
			search = message;
		}
		search(sender, search, limit);
	}

	private void search(String sender, String search, int limit) {
		String[] vids = new String[limit];
		System.out.println(search);
		try {
			// TODO
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String vid : vids) {
			bot.sendMessage(sender, vid);
		}
	}

	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		// makes sure it isn't the bot sending a link
		if (!sender.equals(Settings.NICK) && !sender.equals(bot.getNick())) {
			if (message.contains("(?i)youtube.com")) {
				String s[] = message.split(" ");
				for (String ss : s) {
					if (ss.contains("(?i)youtube.com")) {
						getVideoInfo(channel, ss);
					}
				}
			}
		}
		// TODO Auto-generated method stub

	}

	private void getVideoInfo(String channel, String link) {
		String page = downloadPage(link);
		System.out.println("Now scanning page");

		String uploader = "null";
		String name = null;
		String plays = null;
		try {
			Pattern authorPattern = Pattern.compile(WATCH_UPLOADER_PATTERN);
			Matcher authorMatcher = authorPattern.matcher(page);

			uploader = findExpression(authorMatcher);

		} catch (Exception e) {
		}

		bot.sendMessage(channel, "YouTube " + " -- " + name + "  uploaded by: "
				+ uploader + " -- Plays: " + plays);
		// TODO Auto-generated method stub

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
			br.close();
			br = null;
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String findExpression(Matcher matcher) {
		String s = "null";
		while (matcher.find()) {
			return matcher.group(1);
		}
		return s;
	}
}
