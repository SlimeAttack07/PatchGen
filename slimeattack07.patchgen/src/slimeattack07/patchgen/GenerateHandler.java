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
		IProject project = Utils.getProject();

		if (project == null) {
			System.out.println("Failed to load active project");
			Utils.displayError("Generate patch notes", "Failed to load active project.");
			return null;
		}

		String name = project.getName();
		System.out.println(String.format("Loaded project '%s'", name));

		processProject(project);

		Utils.displayInfo("Generate patch notes", String.format("Generated patch notes for project '%s'", name));
		return null;
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
			Utils.displayError("Generate patch notes", String
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
							String category = getClassCategory(type);
							JsonArray partial = processFields(type, category);

							if (!partial.isEmpty())
								data.addAll(partial);
						}
					}
				}
			}

			if (!data.isEmpty()) {
				JsonObject data_object = new JsonObject();
				data_object.add(PatchNoteData.DATA, data);
				createFiles(project, data_object);
			} else {
				System.out.println("Nothing changed!");
				Utils.displayWarning("Generate patch notes", "Failed to detect any changes.");
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	/** Get the Category id for a class.
	 * 
	 * @param type The class.
	 * @return The category id, or the empty String if not present.
	 */
	private String getClassCategory(IType type) {
		try {
			for(IAnnotation ann : type.getAnnotations()) {
				if(ann.getElementName().equals("CategoryInfo")) {
					for (IMemberValuePair pair : ann.getMemberValuePairs()) {
						System.out.println(String.format("      Pair %s %s", pair.getMemberName(), pair.getValue()));
						
						switch(pair.getMemberName()) {
						case PatchNoteData.ID: return pair.getValue().toString();
						default: break;
						}
					}
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}

	/**
	 * Process final fields in a class.
	 * 
	 * @param type The class to process.
	 * @param category The category id to overwrite the field's category with. Will not overwrite if the empty String is provided.
	 * @return A JsonArray holding the data for all fields.
	 */
	private JsonArray processFields(IType type, String category) {
		JsonArray data = new JsonArray();

		try {
			for (IField field : type.getFields()) {
				if(field.getConstant() == null)
					continue;
				
				System.out.println(String.format("Field info: %s = %s", field.getElementName(), field.getConstant()));
				JsonObject partial = processAnnotations(field, category);

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
	 * @param category The category id to overwrite the field's category with. Will not overwrite if the empty String is provided.
	 * @return A JsonObject holding the data related to the field.
	 */
	@Nullable
	private JsonObject processAnnotations(IField field, String category) {
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

					// Store info like category and name (if provided).
					for (IMemberValuePair pair : ann.getMemberValuePairs()) {
						System.out.println(String.format("      Pair %s %s", pair.getMemberName(), pair.getValue()));

						switch (pair.getMemberName()) {
						case PatchNoteData.ID:
							outer.addProperty(PatchNoteData.ID, pair.getValue().toString());
							break;
						case PatchNoteData.CATEGORY:
							if(category.isBlank())
								outer.addProperty(PatchNoteData.CATEGORY, pair.getValue().toString());
							
							break;
							
						case PatchNoteData.NAME:
							outer.addProperty(PatchNoteData.NAME, pair.getValue().toString());
							break;
						case PatchNoteData.BULLETED:
							outer.addProperty(PatchNoteData.BULLETED, (boolean) pair.getValue());
							break;
						default:
							System.out.println(String.format("Unknown memberpair: %s = %s", pair.getMemberName(),
									pair.getValue()));
							break;
						}
					}
					
					if(!category.isBlank())
						outer.addProperty(PatchNoteData.CATEGORY, category);
						
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
	private void createFiles(IProject project, JsonObject result) {
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
			
			boolean accepted = false;
			String version = "";
			// TODO: May need pretty printer. Probably rewrite writer to not do a toString()
			// cuz of string max length
			InputStream is = new ByteArrayInputStream(result.toString().getBytes());
			
			while(!accepted) {
				System.out.println("DataGen: Specify version:");
				version = Utils.displayNotBlankInput("PatchGen: Version input", "Specify version name.", "categories");
	
				IFile ifile = project.getFile(new Path(String.format("src/patchgen/data/%s.json", version)));

				if (!ifile.exists()) {
					ifile.create(is, false, null);
					accepted = true;
				}
				else if (Utils.displayYesNo("PatchGen: Version Input", 
						String.format("Version '%s' already exists. Would you like to overwrite it?", version))){
					ifile.setContents(is, false, true, null);
					accepted = true;
				}
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
		boolean accepted = false;
		IFile ifile_new = null;
		IFile ifile_old = null;
		String old_version = "";
		
		while(!accepted) {
			System.out.println("Comparison: Specify version to compare to:");
			old_version = Utils.displayNotBlankInput("Version input", "Specify version to compare to. Enter 'cancel' to cancel.", "categories");

			// TODO: Add way to determine if other versions even exist to compare to.
			if (old_version.toLowerCase().equals("cancel")) {
				Utils.displayInfo("PatchGen: Comparison", "User canceled generation of release notes.");
				return;
			}

			ifile_new = Utils.requestFile(project, "data", new_version, "json");
			ifile_old = Utils.requestFile(project, "data", old_version, "json");

			if (ifile_new == null || !ifile_new.exists()) {
				System.out.println(String.format("File src/patchgen/data/%s.json does not exist", new_version));
				Utils.displayError("PatchGen: Comparison", String.format("File src/patchgen/data/%s.json does not exist", new_version));
				return;
			}

			if (ifile_old == null || !ifile_old.exists()) {
				System.out.println(String.format("File src/patchgen/data/%s.json does not exist", old_version));
				Utils.displayWarning("PatchGen: Comparison", String.format("File src/patchgen/data/%s.json does not exist", old_version));
			}
			else
				accepted =  true;
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
