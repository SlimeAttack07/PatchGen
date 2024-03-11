package slimeattack07.patchgen.generators;

import java.util.Scanner;

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
	public PlainTextGenerator(IProject project) {
		this.PROJECT = project;
		// TODO: Add version check. Temporarily using System.in for testing.
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.println("PatchGen: Specify version:");
		String version = scan.nextLine();
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
