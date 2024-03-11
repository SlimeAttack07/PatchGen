package slimeattack07.patchgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/** Handler for the 'codegen' button in the view screen.
 * Handles generation of the files that developers can use to automate parts of the patch note generation process.
 * 
 */
public class GenCodeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = getProject();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		if (project == null) {
			System.out.println("Failed to load active project");
			MessageDialog.openInformation(shell, "Code generator", "Failed to load active project.");
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
			// TODO: Make folder/file gen run on plugin load?
			// Check if patchgen folder exists, create if it doesn't exist.
			IFolder folder_patchgen = project.getFolder(new Path("src/patchgen"));

			if (!folder_patchgen.exists())
				folder_patchgen.create(false, false, null);

			IFolder folder_annotations = project.getFolder(new Path("src/patchgen/annotations"));

			// Check if patchgen/annotations folder exists, create if it doesn't exist.
			if (!folder_annotations.exists())
				folder_annotations.create(false, false, null);

			// Check if Watchable.java exists, create if it doesn't exist.
			// TODO: Add version check If I decide to ever update this plugin.
			IFile ifile = project.getFile(new Path("src/patchgen/annotations/Watchable.java"));

			if (!ifile.exists()) {
				InputStream is = new ByteArrayInputStream(Watchable.getCode().getBytes());
				ifile.create(is, false, null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/** Get the active project.
	 * 
	 * @return The active project, or null if none are active.
	 */
	@Nullable
	private IProject getProject() {
		// Find the active project
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = activePage.getActiveEditor();
		IProject project = null;

		if (activeEditor != null) {
			IEditorInput input = activeEditor.getEditorInput();
			project = input.getAdapter(IProject.class);
			if (project == null) {
				IResource resource = input.getAdapter(IResource.class);
				if (resource != null) {
					project = resource.getProject();
				}
			}
		}

		return project;
	}
}
