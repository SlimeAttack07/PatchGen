package slimeattack07.patchgen;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/** Utility class with convenience methods used by other classes.
 * 
 */
public class Utils {

	/** Request a unique file to dump data in. If file already exists, user will be asked if it can be overwritten.
	 * 
	 * @param project Project to generate file for.
	 * @param dir The patchgen subdirectory to put the file in.
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @return The file to dump data in, or null if an error occurred or user refuses to overwrite.
	 */
	@Nullable
	public static IFile requestUniqueFile(IProject project, String dir, String name, String extension) {
		IFile ifile = requestFile(project, dir, name, extension);
		
		if(ifile.exists()) {
			boolean overwrite = Utils.displayYesNo("PatchGen: File Request", 
					String.format("File 'patchgen/%s/%s.%s' already exists. Would you like to overwrite it?", dir, name, extension));
			
			// Clear contents if overwrite is allowed.
			if(overwrite) {
				try {
					ifile.setContents(new ByteArrayInputStream("".getBytes()), false, true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				
				return ifile;
			}
			
			return null;
		}
		
		return ifile;
	}
	
	/** Request a file to dump data in.
	 * 
	 * @param project Project to generate file for.
	 * @param dir The patchgen subdirectory to put the file in.
	 * @param name The name of the file.
	 * @param extension The extension of the file.
	 * @return The file to dump data in, or null if an error occurred
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
	
	/** Display error to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayError(String title, String message) {
		try {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openError(shell, title, message);
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying info:");
			e.printStackTrace();
		}
	}
	
	/** Display warning to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayWarning(String title, String message) {
		try {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openWarning(shell, title, message);
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying info:");
			e.printStackTrace();
		}
	}
	
	/** Display information to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static void displayInfo(String title, String message) {
		try {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openInformation(shell, title, message);
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying info:");
			e.printStackTrace();
		}
	}
	
	/** Display yes/no question to user.
	 * 
	 * @param title Title of the display window.
	 * @param message The message in the display window.
	 */
	public static boolean displayYesNo(String title, String message) {
		try {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return MessageDialog.openQuestion(shell, title, message);
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying info:");
			e.printStackTrace();
		}
		
		return false;
	}
	
	/** Display a window requesting user for a positive integer. If the user presses 'Cancel', then -1 is returned.
	 * 
	 * @param title The title of the display window.
	 * @param message The message in the display window.
	 * @return The input positive integer, or -1 if user pressed 'Cancel'.
	 */
	public static int displayPositiveIntInput(String title, String message) {
		int input_value = -1; // Cancel will default to -1.
		
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			InputDialog input_dialog = new InputDialog(shell, title, message, "", new PositiveIntegerValidator());
			input_dialog.setBlockOnOpen(true); // Can't proceed without user input.
			int status = input_dialog.open();
			
			if(status == Window.OK)
				input_value = toInt(input_dialog.getValue());
		
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying input:");
			e.printStackTrace();
		}
		
		return input_value;
	}
	
	/** Display a window requesting user for non-empty input. If the user presses 'Cancel', then "NOTHING" is returned.
	 * 
	 * @param title The title of the display window.
	 * @param message The message in the display window.,\
	 * @return The input text, or "NOTHING" if user pressed 'Cancel'.
	 */
	public static String displayNotBlankInput(String title, String message, String... banned) {
		String input_value = "NOTHING"; // Cancel will default to -1.
		
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			InputDialog input_dialog = new InputDialog(shell, title, message, "", 
					new NotBlankValidator(new ArrayList<>(Arrays.asList(banned))));
			input_dialog.setBlockOnOpen(true); // Can't proceed without user input.
			int status = input_dialog.open();
			
			if(status == Window.OK)
				input_value = input_dialog.getValue();
		
		} catch(IllegalStateException | NullPointerException e) {
			System.out.println("Encountered error displaying input:");
			e.printStackTrace();
		}
		
		return input_value;
	}
	
	/** Convert a String to an integer. Returns -1 if the input String is not an integer.
	 * 
	 * @param s The String to convert.
	 * @return The String as an integer, or -1 if String is not an integer.
	 */
	public static int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {}
		
		return -1;
	}
	
	/** Validator for positive integers.
	 * 
	 */
	private static class PositiveIntegerValidator implements IInputValidator{
		@Override
		public String isValid(String newText) {
			try {
				int i = Integer.parseInt(newText);
				return i >= 0 ? null : "Integer must be positive (>= 0).";
			} catch(NumberFormatException e) {}

			return "Must be a positive integer.";
		}
		
	}
	
	/** Validator for non-blank input.
	 * Also supports banning certain input.
	 * 
	 */
	private static class NotBlankValidator implements IInputValidator{
		private ArrayList<String> illegal;
		
		public NotBlankValidator(ArrayList<String> illegal) {
			this.illegal = illegal;
		}
		
		@Override
		public String isValid(String newText) {
			return newText.isBlank() ? "Can't be blank" : 
				illegal.contains(newText) ? String.format("'%s' is not permitted", newText) : null;
		}
		
	}
}
