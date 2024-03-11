package slimeattack07.patchgen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Handler for the 'generate' button in the view screen. Handles generation of
 * patch notes as well as updating the monitored data.
 */
public class GenerateHandler extends AbstractHandler {
	// TODO: Make log to expose what plugin is doing and if it has encountered
	// errors.
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Find the active project
		IProject project = getProject();

		if (project == null) {
			System.out.println("Failed to load active project");
			Utils.displayInfo("Generate patch notes", "Failed to load active project.");
			return null;
		}

		String name = project.getName();
		System.out.println(String.format("Loaded project '%s'", name));

		processProject(project);

		Utils.displayInfo("Generate patch notes", String.format("Generated patch notes for project '%s'", name));
		return null;
	}

	/**
	 * Get the active project.
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

	/**
	 * Processes a project, generating patch notes.
	 * 
	 * @param project The project to process. Must be a Java project or method will
	 *                terminate.
	 */
	private void processProject(IProject project) {
		IJavaProject javaproject = JavaCore.create(project);

		if (javaproject == null) {
			System.out.println("Not a Java project!");
			Utils.displayInfo("Generate patch notes", String
					.format("Failed to generate patch notes: Project '%s' is not a Java project!", project.getName()));
			return;
		}

		JsonArray data = new JsonArray();

		try {
			for (IPackageFragment frag : javaproject.getPackageFragments()) {
				// Only process source files, ignore things like libraries.
				if (frag.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : frag.getCompilationUnits()) {
						System.out.println(String.format("File: %s", unit.getElementName()));
						for (IType type : unit.getAllTypes()) {
							JsonArray partial = processFields(type);

							if (!partial.isEmpty())
								data.addAll(partial);
						}
					}
				}
			}

			if (!data.isEmpty()) {
				JsonObject data_object = new JsonObject();
				data_object.add(PatchNoteData.DATA, data);
				createFiles(project, data_object, true);
			} else {
				System.out.println("Nothing changed!");
				Utils.displayInfo("Generate patch notes", "Failed to detect any changes.");
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process fields in a class.
	 * 
	 * @param type The class to process.
	 */
	private JsonArray processFields(IType type) {
		JsonArray data = new JsonArray();

		try {
			for (IField field : type.getFields()) {
				System.out.println(String.format("Field info: %s = %s", field.getElementName(), field.getConstant()));
				JsonObject partial = processAnnotations(field);

				if (partial != null && !partial.isEmpty())
					data.add(partial);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * Process annotations on a field.
	 * 
	 * @param field The field to process.
	 */
	@Nullable
	private JsonObject processAnnotations(IField field) {
		try {
			for (IAnnotation ann : field.getAnnotations()) {
				if (ann.getElementName().equals("Watchable")) { // TODO: Make these constants
					System.out.println(String.format("   Annotation info: %s", ann.getElementName()));
					JsonObject outer = new JsonObject();
					Object value = field.getConstant();

					// Store value of field.
					if (value instanceof Number)
						outer.addProperty(PatchNoteData.VALUE, (Number) value);
					else if (value instanceof Boolean)
						outer.addProperty(PatchNoteData.VALUE, (Boolean) value);
					else
						outer.addProperty(PatchNoteData.VALUE, value.toString());

					// Store meta info like category and name (if provided).
					for (IMemberValuePair pair : ann.getMemberValuePairs()) {
						System.out.println(String.format("      Pair %s %s", pair.getMemberName(), pair.getValue()));

						switch (pair.getMemberName()) {
						case PatchNoteData.ID:
							outer.addProperty(PatchNoteData.ID, pair.getValue().toString());
							break;
						case PatchNoteData.CATEGORY:
							outer.addProperty(PatchNoteData.CATEGORY, pair.getValue().toString());
							break;
						case PatchNoteData.NAME:
							outer.addProperty(PatchNoteData.NAME, pair.getValue().toString());
							break;
						default:
							System.out.println(String.format("Unknown memberpair: %s = %s", pair.getMemberName(),
									pair.getValue()));
							break;
						}
					}

					System.out.println("Generated following JSON:");
					System.out.println(outer);
					return outer;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Generate JSON database.
	 * 
	 * @param project   Project to generate files for.
	 * @param result    The resulted text to put in the file.
	 * @param overwrite Whether an existing file should be overwritten.
	 */
	private void createFiles(IProject project, JsonObject result, boolean overwrite) {
		try {
			// TODO: Make folder/file gen run on plugin load?
			// Check if patchgen folder exists, create if it doesn't exist.
			IFolder folder_patchgen = project.getFolder(new Path("src/patchgen"));

			if (!folder_patchgen.exists())
				folder_patchgen.create(false, false, null);

			IFolder folder_data = project.getFolder(new Path("src/patchgen/data"));

			// Check if patchgen/data folder exists, create if it doesn't exist.
			if (!folder_data.exists())
				folder_data.create(false, false, null);

			// Check if data.json exists, create if it doesn't exist.
			// TODO: Add version check.
			System.out.println("DataGen: Specify version:");
			String version = Utils.displayNotBlankInput("Version input", "Specify version name.", "categories");

			IFile ifile = project.getFile(new Path(String.format("src/patchgen/data/%s.json", version)));

			if (overwrite) { // TODO: May need pretty printer. Probably rewrite writer to not do a toString()
								// cuz of string max length
				InputStream is = new ByteArrayInputStream(result.toString().getBytes());

				if (!ifile.exists())
					ifile.create(is, false, null);
				else
					ifile.setContents(is, false, true, null);
			}

			compareToVersion(project, version);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Compare two versions. New version should be provided, old version will be
	 * requested from user by this method.
	 * 
	 * @param project     Project to compare versions for.
	 * @param new_version The new version.
	 */
	private void compareToVersion(IProject project, String new_version) {
		// TODO: Add version check. Temporarily using System.in for testing.
		System.out.println("Comparison: Specify version to compare to:");
		String old_version = Utils.displayNotBlankInput("Version input", "Specify version to compare to.", "categories");

		// TODO: Add way to determine if other versions even exist to compare to.
		if (old_version.equals("no") || old_version.equals("categories"))
			return;

		IFile ifile_new = Utils.requestFile(project, "data", new_version, "json");
		IFile ifile_old = Utils.requestFile(project, "data", old_version, "json");

		if (!ifile_new.exists()) {
			System.out.println(String.format("File src/patchgen/data/%s.json does not exist", new_version));
			return;
		}

		if (!ifile_old.exists()) {
			System.out.println(String.format("File src/patchgen/data/%s.json does not exist", old_version));
			return;
		}

		try ( // Auto-closes resources
				Reader reader_new = new InputStreamReader(ifile_new.getContents());
				Reader reader_old = new InputStreamReader(ifile_old.getContents());) {
			Gson gson = new Gson();

			PatchNoteData data_new = gson.fromJson(reader_new, PatchNoteData.class);
			PatchNoteData data_old = gson.fromJson(reader_old, PatchNoteData.class);
			data_new.genNotes(data_old, project, old_version, new_version);
		} catch (IOException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
