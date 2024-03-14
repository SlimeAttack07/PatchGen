package slimeattack07.patchgen.generators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import slimeattack07.patchgen.Utils;

/** Patch note generator: .txt output.
 * 
 */
public class PlainTextGenerator extends AbstractPatchNoteGenerator implements PatchNoteGenerator {
	private final IProject PROJECT;
	private final IFile IFILE;
	private final boolean IS_VALID;
	
	/** Constructor.
	 * 
	 * @param project The project to generate patch notes for.
	 */
	public PlainTextGenerator(IProject project, String version) {
		this.PROJECT = project;
		this.IFILE = Utils.requestUniqueFile(PROJECT, "patchnotes", version, ".txt");
		
		IS_VALID = this.IFILE != null;
	}
	
	@Override
	public void addText(String text, int depth, boolean bulleted) {
		String real_text = bulleted ? "* " + text : text;
		String indented = indent(real_text, depth);
		addToFile(IFILE, indented + System.lineSeparator());
	}

	@Override
	public boolean isValid() {
		return IS_VALID;
	}

	@Override
	public void addCategory(String name, int depth) {
		addText("--[[" + name.toUpperCase() + "]]--", depth, false); // TODO: Temporarily using upper case for testing.
	}
	
	/** Indent given text.
	 * 
	 * @param text The text to indent.
	 * @param depth The amount of tabs to indent with.
	 * @return The indented text.
	 */
	private String indent(String text, int depth) {
		String indented = "";
		
		for(int i = 0; i < depth; i++)
			indented += "\t";
		
		return indented + text;
	}
}
