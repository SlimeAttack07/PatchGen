package slimeattack07.patchgen.generators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import slimeattack07.patchgen.Utils;

public class MarkdownGenerator extends AbstractPatchNoteGenerator implements PatchNoteGenerator {
	private final IProject PROJECT;
	private final IFile IFILE;
	private final boolean IS_VALID;
	
	/** Constructor.
	 * 
	 * @param project The project to generate patch notes for.
	 */
	public MarkdownGenerator(IProject project, String version) {
		this.PROJECT = project;
		this.IFILE = Utils.requestUniqueFile(PROJECT, "patchnotes", version, "md");
		this.IS_VALID = this.IFILE != null;
	}
	
	@Override
	public boolean isValid() {
		return IS_VALID;
	}

	@Override
	public void addContent(String content, int depth, boolean bulleted) {
		String real_text = bulleted ? "* " + content : content;
		addToFile(IFILE, real_text + System.lineSeparator());
	}

	@Override
	public void addCategory(String name, int depth) {
		// Markdown supports up to 6 heading levels, so anything above level 5 is defaulted to max level 6.
		switch(depth) {
		case 0: addContent("# " + name, depth, false); break;
		case 1: addContent("## " + name, depth, false); break;
		case 2: addContent("### " + name, depth, false); break;
		case 3: addContent("#### " + name, depth, false); break;
		case 4: addContent("##### " + name, depth, false); break;
		default: addContent("###### " + name, depth, false); break;
		}
	}

	@Override
	public void addText(String text, int depth, boolean is_developer_comment) {
		if(is_developer_comment) {
			// For compatability, add empty lines before and after block quote.
			addContent("", depth, false);
			addContent("> ***Developer comments***: " + text, depth, false);
			addContent("", depth, false);
		}
		else
			addContent(text, depth, false);
	}

	@Override
	public String indent(String text, int depth) {
		return text; // Markdown doesn't like indentation, so just return the text without indenting.
	}

	@Override
	public void finish() {}
}
