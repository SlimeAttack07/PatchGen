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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AddTextHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//TODO: Check id
		
		String category = Utils.displayNotBlankInput("PatchGen: Add Text", "What category should this text be placed in?");
		
		if(category.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return null;
		}
		
		boolean is_developer_comment = Utils.displayYesNo("PatchGen: Add Text", "Should this be marked as a developer comment?");
		
		// TODO: Add support for formatting like bold, italics, etcetera.
		String text = Utils.displayNotBlankInput("PatchGen: Add Text", "Please add your text");
		
		if(text.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return null;
		}
		
		String id = Utils.displayNotBlankInput("PatchGen: Add Text", "Please assign an id to this text."
				+ "This will help you determine whether another developer already added text for the change you are "
				+ "about to describe.");
		
		if(id.equals("NOTHING")) {
			Utils.displayWarning("PatchGen: Add Text", "User cancelled addition of text.");
			return null;
		}
		
		if(processText(id, category, is_developer_comment, text))
			Utils.displayInfo("PatchGen: Add Text", "Added text to file.");
		
		return null;
	}

	private boolean processText(String id, String category, boolean devcom, String text) {
		IProject project = Utils.getProject();
		IFile ifile = Utils.requestFile(project, "data", "text", ".json");
		JsonArray arr = new JsonArray();
		
		if(ifile.exists()) {
			try ( // Auto-closes resources
					Reader reader = new InputStreamReader(ifile.getContents());) {
				Gson gson = new Gson();

				PatchNoteData data = gson.fromJson(reader, PatchNoteData.class);
				
				if(data.contains(id)) {
					boolean accepted = Utils.displayYesNo("PatchGen: Add Text", String.format(
							"Id '%s' already exists. Would you like to overwrite it? It currently says the following: \"%s\"",
							id, data.get(id).get(PatchNoteData.VALUE)));
					if(accepted)
						arr.remove(data.get(id));
					else {
						Utils.displayWarning("PatchGen: Add Text", "User canceled addition of text.");
						return false;
					}
				}
				
				arr = data.getData();				
			} catch (IOException | CoreException e) {
				e.printStackTrace();
			}
		}
		
		JsonObject entry = new JsonObject();
		entry.addProperty(PatchNoteData.ID, id);
		entry.addProperty(PatchNoteData.CATEGORY, category);
		entry.addProperty(PatchNoteData.DEVELOPER_COMMENT, devcom);
		entry.addProperty(PatchNoteData.IS_TEXT, true);
		entry.addProperty(PatchNoteData.VALUE, text);
		
		arr.add(entry);
		JsonObject data = new JsonObject();
		data.add(PatchNoteData.DATA, arr);
		InputStream is = new ByteArrayInputStream(data.toString().getBytes());
		
		try {
			if(ifile.exists())
				ifile.setContents(is, false, true, null);
			else
				ifile.create(is, false, null);
			
			return true;
		} catch (CoreException e) {
			Utils.displayError("PatchGen: Add Text", "Failed to create/modify file.");
		}
		
		return false;
	}
}
