package slimeattack07.patchgen.generators;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/** Abstract patch note generator.
 * 
 */
public abstract class AbstractPatchNoteGenerator {
	
	/** Add content to file.
	 * 
	 * @param ifile File to add content to.
	 * @param content Content to add.
	 */
	public void addToFile(IFile ifile, String content) {
		InputStream is = toInputStream(content);
		try {
			if(ifile.exists()) {
				ifile.appendContents(is, false, true, null);
			}
			else
				ifile.create(is, false, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/** Turn a String into an InputStream.
	 * 
	 * @param input The String to transform.
	 * @return The String as InputStream.
	 */
	public InputStream toInputStream(String input) {
		return new ByteArrayInputStream(input.getBytes());
	}
}
