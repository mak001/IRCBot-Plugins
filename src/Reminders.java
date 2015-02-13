import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mak001.ircBot.Bot;
import com.mak001.api.plugins.Command;
import com.mak001.api.plugins.Command.CommandAction;
import com.mak001.api.plugins.Plugin;
import com.mak001.ircBot.settings.Settings;

public class Reminders extends Plugin {

	private String prefix = Settings.get(Settings.COMMAND_PREFIX);
	private HashMap<String, List<String>> reminders = new HashMap<String, List<String>>();

	public Reminders(Bot bot) {
		super(bot, "REMINDERS");
		try {
			loadReminders();
		} catch (Exception e) {
			e.printStackTrace();
		}
		bot.getPluginManager().registerCommand(add_reminder);
		bot.getPluginManager().registerCommand(remove_reminder);
		bot.getPluginManager().registerCommand(get_reminder);
	}

	private Command add_reminder = new Command(this, "ADD REMINDER", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			List<String> reminds = reminders.get(sender);
			if (reminds == null) {
				List<String> list = new ArrayList<String>();
				list.add(additional);
				reminders.put(sender.toLowerCase(), list);
			}
			reminds.add(additional);
			saveReminders();
			bot.sendMessage(channel, sender + "  :  added reminder  :  " + additional);
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "Adds a reminder for the user issuing the command  : Syntax: " + prefix + "ADD REMINDER <REMINDER>");
		}
	});

	private Command remove_reminder = new Command(this, "REMOVE REMINDER", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			int id = -1;

			if (additional != null && additional.length() > 0) {
				try {
					id = Integer.parseInt(additional);
				} catch (NumberFormatException e) {
					bot.sendMessage(channel, additional + " is not a valid number.");
					return;
				}
			}
			List<String> reminds = reminders.get(sender);
			if (reminds != null) {
				if (reminds.size() > 1 && id == -1) {
					bot.sendMessage(channel, "You need to specify an id because there are more than one reminder for " + sender);
				} else if (reminds.size() == 1) {
					reminders.remove(sender);
					saveReminders();
				} else {
					bot.sendMessage(channel, sender + "  :  removed reminder  :  " + reminds.remove(id - 1));
					saveReminders();
				}
			} else {
				bot.sendMessage(channel, "There were no reminders for " + sender);
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "Removes a reminder for the user issuing the command  : Syntax: " + prefix + "REMOVE REMINDER <REMINDER_ID>");
		}
	});

	private Command get_reminder = new Command(this, "GET REMINDER", new CommandAction() {

		@Override
		public void onCommand(String channel, String sender, String login, String hostname, String additional) {
			String user = sender;
			String id = null;

			if (additional != null && additional.length() > 0) {
				if (additional.contains(" ")) {
					user = additional.split(" ")[0].toLowerCase();
					id = additional.split(" ")[1];
				} else {
					user = additional;
					id = null;
				}
			}
			List<String> reminds = reminders.get(user);
			if (reminds == null || reminds.size() == 0) {
				bot.sendMessage(channel, user + "  has no reminders");
				return;
			} else if (reminds.size() == 1) {
				bot.sendMessage(channel, user + "  :  " + reminds.get(0));
			} else {
				if (id != null) {
					try {
						int id_int = Integer.parseInt(id);
						if (id_int <= reminds.size()) {
							bot.sendMessage(channel, user + " : " + id + " :  " + reminders.get(user).get(id_int - 1));
						} else {
							bot.sendMessage(channel, "There is no reminder for " + user + " with id " + id);
						}
					} catch (NumberFormatException nfe) {
						bot.sendMessage(channel, id + " is not a valid id");
					}
				} else {
					bot.sendMessage(channel, user + " has " + reminds.size() + " reminders. To see a specific reminder use " + prefix
							+ "GET REMINDER [USER] <REMINDER_NUMBER>");
				}
			}
		}

		@Override
		public void onHelp(String channel, String sender, String login, String hostname) {
			bot.sendMessage(sender, "Gets a reminder for the user issuing the command  : Syntax: " + prefix + "GET REMINDER [USER] <REMINDER_NUMBER>");
		}
	});

	private void loadReminders() throws SAXException, IOException, ParserConfigurationException {
		System.out.println("Loading reminders xml");
		File fXmlFile = new File(Settings.userHome + Settings.fileSeparator + "Settings" + Settings.fileSeparator + "reminders.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("user");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				List<String> temparray = new ArrayList<String>();

				Element eElement = (Element) nNode;
				NodeList list = eElement.getElementsByTagName("reminder");
				for (int i = 0; i < list.getLength(); i++) {
					temparray.add(list.item(i).getTextContent());
					System.out.println(eElement.getAttribute("user_name").toLowerCase() + " == " + list.item(i).getTextContent());
				}
				reminders.put(eElement.getAttribute("user_name").toLowerCase(), temparray);

			}
		}
	}

	private void saveReminders() {
		File fXmlFile = new File(Settings.userHome + Settings.fileSeparator + "Settings" + Settings.fileSeparator + "reminders.xml");
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			Document doc = icBuilder.newDocument();
			Element mainRootElement = doc.createElement("reminders");
			doc.appendChild(mainRootElement);

			for (Entry<String, List<String>> me : reminders.entrySet()) {
				Element user = doc.createElement("user");
				user.setAttribute("user_name", me.getKey());
				for (String rem : me.getValue()) {
					Element node = doc.createElement("reminder");
					node.appendChild(doc.createTextNode(rem));
					user.appendChild(node);

				}
				mainRootElement.appendChild(user);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			doc.getDocumentElement().normalize();
			DOMSource source = new DOMSource(doc);
			StreamResult file = new StreamResult(fXmlFile);

			transformer.transform(source, file);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
