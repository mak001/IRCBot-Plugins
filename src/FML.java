import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;

import com.mak001.ircBot.Bot;
import com.mak001.ircBot.plugins.Plugin;

public class FML extends Plugin {

	private BufferedReader br = null;

	@SuppressWarnings("unused")
	private final String FML_RANDOM_URL = "http://www.fmylife.com/random";
	@SuppressWarnings("unused")
	private final String FML_ENTRY_URL = "http://www.fmylife.com/apps/search.php";

	public FML(Bot bot) {
		super(bot, "FML");
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private String findExpression(Matcher matcher) {
		String s = "null";
		while (matcher.find()) {
			return matcher.group(1);
		}
		return s;
	}

	public void onCommand(String channel, String sender, String login,
			String hostname, String command) {
		String[] s = command.split(" ");
		if (s[0].equalsIgnoreCase("RANDOM")) {
			// TODO
		} else {

		}
	}

	public void onPrivateCommand(String sender, String login, String hostname,
			String command) {
		// TODO Auto-generated method stub

	}

}
