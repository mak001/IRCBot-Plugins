import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mak001.imgurAPI.Imgur;
import com.mak001.imgurAPI.classes.Connect.GenericConnectionException;
import com.mak001.ircBot.Bot;
import com.mak001.api.plugins.Command;
import com.mak001.api.plugins.Command.CommandAction;
import com.mak001.ircBot.settings.Settings;

public class Reddit extends BasicSite {

	List<String> subreddits = new ArrayList<String>();
	HashMap<String, List<String>> likes = new HashMap<String, List<String>>();
	String[] sorting = { "time", "top" };
	String[] windows = { "day", "week", "month", "year", "all" };
	String previous_id;
	Imgur imgur;

	public Reddit(Bot bot, NSFW nsfw) {
		super(bot, nsfw, "-r");

		loadSubReddits();
		loadLikes();

		bot.registerCommand(reddit);
		bot.registerCommand(like);
	}

	private Command reddit = new Command(plugin, "REDDIT", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String subreddit;
			String target = channel == null ? sender : channel;

			if (additional != null && !additional.equals("") && !additional.contains(" ")) {
				subreddit = additional;
			} else {
				bot.sendMessage(target, "The Reddit command needs to be given a rsubeddit");
				return;
			}

			JSONArray data = digest(roll(subreddit), 0, target);

			if (data == null) {
				bot.sendMessage(target, "Failed to get a valid page for " + subreddit + ". Are you sure it is a valid subreddit?");
				return;
			}

			int ran = NSFW.randInt(0, data.length() - 1);
			JSONObject image = (JSONObject) data.get(ran);
			if (image.getBoolean("nsfw") == false) {
				bot.sendMessage(target, "[/r/" + subreddit + "]  " + image.getString("link"));
			} else {
				bot.sendMessage(target, "[NSFW] [/r/" + subreddit + "]  " + image.getString("link"));
			}
			previous_id = image.getString("id");
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "You have to specify a subreddit to look for pictures in by using  " + Settings.COMMAND_PREFIX + "REDDIT "
					+ "<SUBREDDIT>");
		}
	});

	private Command like = new Command(plugin, "LIKE", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String id = "";
			if (additional == null || additional.equals("")) {
				id = previous_id;
			} else {
				id = getid(additional);
			}
			if (!id.equals("")) {
				if (!hasLiked(id, sender.toLowerCase())) {
					if (likes.get(id) == null) {
						likes.put(id, new ArrayList<String>());
					}
					likes.get(id).add(sender.toLowerCase());
					bot.sendMessage(channel, sender + " now likes " + id + ";  " + id + " now has " + likes.get(id).size() + " likes");
					saveLikes();
				} else {
					bot.sendMessage(channel, sender + " already likes " + id);
				}
			} else {
				bot.sendMessage(channel, "did not have a previous id or could not find the given id.");
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "Likes the last image the bot posted or the designated image  " + Settings.COMMAND_PREFIX + "LIKE "
					+ "<Imgur_URL or image id>");
		}
	});

	private String getid(String string) {
		Matcher matcher = Pattern.compile("([A-Z0-9])\\w+").matcher(string);
		while (matcher.find()) {
			return matcher.group();
		}
		return "";
	}

	private boolean hasLiked(String id, String user) {
		return likes.get(id) != null && likes.get(id).contains(user.toLowerCase());
	}

	private HashMap<String, Object> roll(String subreddit) {
		int max_page = 4;
		int page = NSFW.randInt(0, max_page);

		String sort = sorting[NSFW.randInt(0, 1)];
		String window = null;
		if (sort.equalsIgnoreCase("top")) {
			window = windows[NSFW.randInt(0, 4)];
		}

		if (window != null && !window.equalsIgnoreCase("year") && !window.equalsIgnoreCase("all")) {
			page = 0;
		}

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("subreddit", subreddit);
		data.put("sort", sort);
		data.put("window", window);
		data.put("page", page);

		return data;
	}

	// TODO - remove the target
	public JSONArray digest(HashMap<String, Object> roll_data, int trys, String target) {
		try {
			if (trys == 6) {
				JSONObject gallery_raw = imgur.gallery().subreddit_gallery((String) roll_data.get("subreddit"), "time", 0, "all");
				JSONArray gallery = (JSONArray) gallery_raw.get("data");

				if (gallery.length() < 2) {
					return null;
				}
				return gallery;
			}

			JSONObject gallery_raw = imgur.gallery().subreddit_gallery((String) roll_data.get("subreddit"), (String) roll_data.get("sort"),
					(Integer) roll_data.get("page"), (String) roll_data.get("window"));

			JSONArray gallery = (JSONArray) gallery_raw.get("data");

			if (gallery.length() < 2) {
				return digest(roll((String) roll_data.get("subreddit")), trys + 1, target);
			}
			return gallery;
		} catch (GenericConnectionException gce) {
			if (trys == 6) {
				bot.sendMessage(target, "An error occurred. It's ID was " + gce.getCode());
				return null;
			} else {
				return digest(roll((String) roll_data.get("subreddit")), trys + 1, target);
			}
		} catch (Exception e) {
			if (trys == 6) {
				bot.sendMessage(target, "Failed to get a random image from " + roll_data.get("subreddit"));
				return null;
			} else {
				return digest(roll((String) roll_data.get("subreddit")), trys + 1, target);
			}
		}
	}

	private String getRandomSubReddit() {
		return subreddits.get(NSFW.randInt(0, subreddits.size() - 1));
	}

	private void saveLikes() {
		File file = new File(Settings.userHome + Settings.fileSeparator + "Settings" + Settings.fileSeparator + "reddit_likes.txt");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

			for (Entry<String, List<String>> values : likes.entrySet()) {
				String string = values.getKey() + ":";
				for (String value : values.getValue()) {
					string = string + value + ",";
				}
				writer.write(string);
				writer.write(Settings.lineSeparator);
			}
		} catch (FileNotFoundException e) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void loadLikes() {
		InputStream ips;
		File file = new File(Settings.userHome + Settings.fileSeparator + "Settings" + Settings.fileSeparator + "reddit_likes.txt");
		try {
			ips = new FileInputStream(file);

			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(":");
				final String[] names = parts[1].split(",");
				likes.put(parts[0], new ArrayList<String>() {
					private static final long serialVersionUID = -3438184176562025377L;
					{
						for (String name : names) {
							add(name);
						}
					}
				});
			}
			br.close();
		} catch (FileNotFoundException e) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadSubReddits() {
		try {
			imgur = new Imgur("eb6ed1f98a1783d", "d8c995f395116d94186e1d4abad6ea4e2ae66009");

			InputStream ips = new FileInputStream(Settings.userHome + Settings.fileSeparator + "Settings" + Settings.fileSeparator
					+ "subreddit_list.txt");
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				subreddits.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getRandom(String target) {
		return getImage(target, getRandomSubReddit());
	}

	@Override
	public String getImage(String target, String value) {
		String subreddit;

		if (value.contains(" ")) {
			return "Subreddits can not contain spaces";
		} else {
			subreddit = value;
		}

		JSONArray data = digest(roll(subreddit), 0, target);

		if (data == null) {
			return "Failed to get a valid page for " + subreddit + ". Are you sure it is a valid subreddit?";
		}

		int ran = NSFW.randInt(0, data.length() - 1);
		JSONObject image = (JSONObject) data.get(ran);
		previous_id = image.getString("id");
		if (image.getBoolean("nsfw") == false) {
			return "[/r/" + subreddit + "]  " + image.getString("link");
		} else {
			return "[NSFW] [/r/" + subreddit + "]  " + image.getString("link");
		}
	}
}
