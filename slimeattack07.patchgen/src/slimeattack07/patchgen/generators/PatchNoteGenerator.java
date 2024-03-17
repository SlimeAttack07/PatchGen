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
	
	/** Add content to file. Caller must determine which file to add to.
	 * 
	 * @param content Content to add.
	 * @param depth Indentation level.
	 * @param bulleted Whether a bullet point should be generated in from of the content.
	 */
	public void addContent(String content, int depth, boolean bulleted);
	
	/** Add category to file. Caller must determine which file to add to.
	 * 
	 * @param name Name of the category.
	 * @param depth Indentation level. 0 = Top level category, 1 = sub-category, 2 = sub-sub-category and so on.
	 */
	public void addCategory(String name, int depth);
	
	/** Add content to file. Caller must determine which file to add to.
	 * 
	 * @param text Text to add.
	 * @param depth Indentation level.
	 * @param is_developer_comment Whether this is a developer comment or regular text.
	 */
	public void addText(String text, int depth, boolean is_developer_comment);
	
	/** Indent given text.
	 * 
	 * @param text The text to indent.
	 * @param depth The amount of tabs to indent with.
	 * @return The indented text.
	 */
	public String indent(String text, int depth);
	
	/** Allow the generator to generate anything else needed after the last content was added.
	 * Primarily needed for output formats like HTML, which need the right closing tags to be generated.
	 * 
	 */
	public void finish();
}
