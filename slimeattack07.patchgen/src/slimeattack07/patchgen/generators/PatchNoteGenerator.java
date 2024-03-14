package slimeattack07.patchgen.generators;

/** Interface for patch note generators.
 * 
 */
public interface PatchNoteGenerator {
	
	/** Whether initialization of the generator succeeded.
	 * 
	 * @return True if generator can be used safely, false otherwise.
	 */
	public boolean isValid();
	
	/** Add text to file. Caller must determine which file to add to.
	 * 
	 * @param text Text to add.
	 */
	public void addText(String text, int depth, boolean bulleted);
	
	/** Add category to file. Caller must determine which file to add to.
	 * 
	 * @param name Name of the category.
	 * @param depth Indentation level. 0 = Top level category, 1 = sub-category, 2 = sub-sub-category and so on.
	 */
	public void addCategory(String name, int depth);
}
