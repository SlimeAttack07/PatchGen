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
	public PlainTextGenerator(IProject project, String old_version, String new_version) {
		this.PROJECT = project;
		String version = String.format("%s_to_%s", old_version, new_version);
		this.IFILE = Utils.requestFile(PROJECT, "patchnotes", version, ".txt");
		
		IS_VALID = this.IFILE != null;
	}
	
	@Override
	public void addText(String text, int depth) {
		String indented = indent(text, depth);
		addToFile(IFILE, indented + System.lineSeparator());
	}

	@Override
	public boolean isValid() {
		return IS_VALID;
	}

	@Override
	public void addCategory(String name, int depth) {
		addText("--[[" + name.toUpperCase() + "]]--", depth); // TODO: Temporarily using upper case for testing.
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
