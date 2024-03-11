package slimeattack07.patchgen;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/** Utility class with convenience methods used by other classes.
 * 
 */
public class Utils {

	/** Request a file to dump data in.
	 * 
	 * @param project Project to generate file for.
	 * @param dir The patchgen subdirectory to put the file in.
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 */
	@Nullable
	public static IFile requestFile(IProject project, String dir, String name, String extension) {
		try {
			// TODO: Make folder/file gen run on plugin load?
			// Check if patchgen folder exists, create if it doesn't exist.
			IFolder folder_patchgen = project.getFolder(new Path("src/patchgen"));

			if (!folder_patchgen.exists())
				folder_patchgen.create(false, false, null);

			IFolder folder_dir = project.getFolder(new Path(String.format("src/patchgen/%s", dir)));

			// Check if patchgen/data folder exists, create if it doesn't exist.
			if (!folder_dir.exists())
				folder_dir.create(false, false, null);
			
			String real_name = (name == null || name.isBlank()) ? "NONAMEPROVIDED" : name;
			
			IFile ifile = project.getFile(new Path(String.format("src/patchgen/%s/%s.%s", dir, real_name, extension)));
			return ifile;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
