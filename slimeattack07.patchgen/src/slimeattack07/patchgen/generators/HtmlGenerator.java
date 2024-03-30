package slimeattack07.patchgen.generators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import slimeattack07.patchgen.Utils;

public class HtmlGenerator extends AbstractPatchNoteGenerator implements PatchNoteGenerator {
	private final IProject PROJECT;
	private final IFile IFILE;
	private final boolean IS_VALID;
	private String author = "UNKNOWN";
	private boolean include_base = false;
	private boolean in_list = false;
	private int cat_depth = 0;
	
	/** Constructor.
	 * 
	 * @param project The project to generate patch notes for.
	 * @param version The version being generated. Just used as filename.
	 */
	public HtmlGenerator(IProject project, String version) {
		this.PROJECT = project;
		this.IFILE = Utils.requestUniqueFile(PROJECT, "patchnotes", version, "html");
		this.IS_VALID = this.IFILE != null;
		
		if(IS_VALID)
			init(version);
	}
	
	// TODO: Add support for project name?
	private void init(String version) {
		author = Utils.displayNotBlankInput("PatchGen: Author input", "Who's the author of these patch notes?");
		String style = Utils.displayNotBlankInput("PatchGen: Style input", "What CSS should be used for these patch notes?");
		include_base = style.equals("basic");
		
		addContent("<!DOCTYPE html>", 0, false);
		addContent("<html>", 0, false);
		addContent("<head> ", 1, false);
		addContent(String.format("<title> Patch Notes for version %s </title>", version), 2, false);
		addContent(String.format("<link rel=\"stylesheet\" href=\"%s.css\"/>", style), 2, false);
		addContent("<meta charset=\"UTF-8\"/>", 2, false);
		addContent(String.format("<meta name=\"description\" content=\"Patch notes for version %s.\"/>", version), 2, false);
		addContent(String.format("<meta name=\"keywords\" content=\"Patch notes, update, %s\"/>", version), 2, false);
		addContent(String.format("<meta name=\"author\" content=\"%s\"/>", author), 2, false);
		addContent("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>", 2, false);
		addContent("</head>", 1, false);
		addContent("<body id=\"body\">", 1, false);
		addContent("<header id=\"header\">", 2, false);
		addContent(String.format("<h1 style=\"text-align: center\">Patch Notes for version %s</h1>", version), 3, false);
		addContent("<hr/>", 3, false);
		addContent("</header>", 2, false);
		addContent("<main>", 2, false);
		addContent("", 0, false);
	}
	
	@Override
	public boolean isValid() {
		return IS_VALID;
	}

	@Override
	public void addContent(String content, int depth, boolean bulleted) {
		if(bulleted && !in_list) {
			addToFile(IFILE, indent("<div class=\"list\"><ul>" + System.lineSeparator(), depth));
			in_list = true;
		}
		
		if(!bulleted && in_list) {
			addToFile(IFILE, indent("</ul></div>" + System.lineSeparator(), depth));
			in_list = false;
		}
		
		int real_depth = bulleted ? depth + 1 : depth;
		String real_text = bulleted ? "<li>" + content + "</li>": content;
		String indented = indent(real_text, real_depth);
		addToFile(IFILE, indented + System.lineSeparator());
	}

	@Override
	public void addCategory(String name, int depth) {
		int real_depth = depth + 3;
		
		while(cat_depth > depth) {
			addContent("</div>", real_depth, false);
			cat_depth--;
		}
		
		// HTML supports up to 6 heading levels, so anything above level 5 is defaulted to max level 6.
		switch(depth) {
		case 0: addContent("<button type=\"button\" class=\"collapsible biggest\"><h1>" + name + "</h1></button>", real_depth, false); break;
		case 1: addContent("<button type=\"button\" class=\"collapsible\"><h2>" + name + "</h2></button>", real_depth, false); break;
		case 2: addContent("<button type=\"button\" class=\"collapsible\"><h3>" + name + "</h3></button>", real_depth, false); break;
		case 3: addContent("<button type=\"button\" class=\"collapsible\"><h4>" + name + "</h4></button>", real_depth, false); break;
		case 4: addContent("<button type=\"button\" class=\"collapsible\"><h5>" + name + "</h5></button>", real_depth, false); break;
		default: addContent("<button type=\"button\" class=\"collapsible\"><h6>" + name + "</h6></button>", real_depth, false); break;
		}
		
		if(depth == 0)
			addContent("<div class=\"content biggest\">", real_depth, false);
		else
			addContent("<div class=\"content\">", real_depth, false);
		
		cat_depth++;
	}

	@Override
	public void addText(String text, int depth, boolean is_developer_comment) {
		int real_depth = depth + 3;
		
		if(is_developer_comment)
			addContent("<div class=\"devcom\"><b><i>Developer Comments</b></i>: " + text + "</div>", real_depth + 3, false);
		else
			addContent("<div class=\"text\">" + text + "</div>", real_depth + 3, false);
	}
	
	@Override
	public String indent(String text, int depth) {
		String indented = "";
		
		for(int i = 0; i < depth; i++)
			indented += "\t";
		
		return indented + text;
	}

	@Override
	public void finish() {
		while(cat_depth > 0) {
			addContent("</div>", 3, false);
			cat_depth--;
		}
		
		String scripts = "";
		
		if(Utils.displayYesNo("PatchGen: Script inclusion", "Do you need this HTML file to contain scripts? If you chose the 'basic' CSS style, then it's associated script will automatically be added."))
			scripts = Utils.displayNotBlankInput("PatchGen: Script input", "Please specify the script files to include, separated by commas.");
		
		addContent("</main>", 2, false);
		addContent("<footer id=\"footer\" style=\"text-align: center\">", 2, false);
		addContent("<hr/>", 3, false);
		addContent(String.format("Author: %s", author), 3, false);
		addContent("</footer>", 2, false);
		addContent("</body>", 1, false);
		
		if(include_base)
			addContent("<script src=\"basic.js\"></script>", 1, false);
		
		for(String script : scripts.split(","))
			if(!script.isBlank())
				addContent(String.format("<script src=\"%s.js\"></script>", script.strip()), 1, false);
		
		addContent("</html>", 0, false);
	}
}
