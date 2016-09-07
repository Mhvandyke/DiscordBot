package novaz.command.poe;

import novaz.core.AbstractCommand;
import novaz.main.Config;
import novaz.main.NovaBot;
import novaz.modules.reddit.RedditScraper;
import novaz.modules.reddit.pojo.Comment;
import novaz.modules.reddit.pojo.Post;
import novaz.util.Misc;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * !poeitem
 * Analyzes an item from path of exile
 */
public class PoeLabsCommand extends AbstractCommand {
	private Pattern imagePattern = Pattern.compile("(?m)(normal|uber|merciless|cruel) lab notes[\\s]*(https?:.*(png|jpg))", Pattern.MULTILINE);
	private static final Set<String> validArgs = new HashSet<>(Arrays.asList(
			new String[]{"normal", "cruel", "merciless", "uber"}
	));

	public PoeLabsCommand(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Attempts to find a description from reddit for the labratory instance.";
	}

	@Override
	public String getCommand() {
		return "poelab";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"poelab              //lists for all difficulties",
				"poelab <difficulty> //only for that difficulty",
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		List<Post> search = RedditScraper.search("pathofexile", "title%3ADaily+Labyrinth+author%3AAutoModerator&sort=new&restrict_sr=on&t=day");

		if (!search.isEmpty()) {
			Post post = search.get(0);
			List<Comment> comments = RedditScraper.getComments(post.data.getId());
			for (Comment comment : comments) {
				if (comment.data.isOp) {
					continue;
				}
				String searchText = comment.data.body.toLowerCase();
				if (args.length > 0) {
					if (!validArgs.contains(args[0].toLowerCase())) {
						return "There is no such difficulty";
					}
					if (!searchText.contains(args[0].toLowerCase())) {
						continue;
					}
					Matcher m = imagePattern.matcher(searchText);
					while (m.find()) {
						if (m.group(1).equals(args[0].toLowerCase())) {
							return "Path of exile labirinth" + Config.EOL + Config.EOL +
									post.data.title + " [**"+args[0].toLowerCase()+"**]" + Config.EOL + m.group(2);
						}
					}
				} else {
					if (searchText.contains("normal") && searchText.contains("cruel") && searchText.contains("merciless")) {
						return "Path of exile labirinth" + Config.EOL + Config.EOL +
								post.data.title + Config.EOL +
								Misc.makeTable(comment.data.body);
					}
				}
			}
		}
		return "Can't find labdetails :(";
	}
}