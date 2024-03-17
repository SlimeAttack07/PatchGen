package slimeattack07.patchgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/** Handler for the 'codegen' button in the view screen.
 * Handles generation of the files that developers can use to automate parts of the patch note generation process.
 * 
 */
public class GenCodeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = Utils.getProject();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		if (project == null) {
			System.out.println("Failed to load active project");
			MessageDialog.openError(shell, "Code generator", "Failed to load active project.");
			return null;
		}

		String name = project.getName();
		System.out.println(String.format("Loaded project '%s'", name));
		createFiles(project);
		
		
		MessageDialog.openInformation(shell, "Code generator", "Generated code.");
		
		return null;
	}

	/** Generate files.
	 * 
	 * @param project Project to generate files for.
	 */
	private void createFiles(IProject project) {
		try {
			// TODO: Add version check If I decide to ever update this plugin.
			IFile ifile = Utils.requestFile(project, "annotations", "Watchable", "java");
			InputStream is = new ByteArrayInputStream(Watchable.getCode().getBytes());

			if (ifile.exists()) 
				ifile.setContents(is, false, true, null);
			else 
				ifile.create(is, false, null);
			
			ifile = Utils.requestFile(project, "annotations", "CategoryInfo", "java");
			is = new ByteArrayInputStream(CategoryInfo.getCode().getBytes());

			if (ifile.exists()) 
				ifile.setContents(is, false, true, null);
			else 
				ifile.create(is, false, null);
			
			ifile = Utils.requestFile(project, "patchnotes", "basic", "css");
			is = new ByteArrayInputStream(BasicStyle.getStyle().getBytes());

			if (ifile.exists()) 
				ifile.setContents(is, false, true, null);
			else 
				ifile.create(is, false, null);
		} catch (CoreException | NullPointerException e) {
			e.printStackTrace();
		}
	}
}
