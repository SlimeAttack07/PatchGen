package slimeattack07.patchgen;
import java.io.ByteArrayInputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ResetTextHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = Utils.getProject();
		
		if(project == null)
			return null;
		
		if(Utils.displayYesNo("PatchGen: Reset text file", "Would you like to clear the text.json file to start fresh for the next patch?")) {
			IFile ifile = Utils.requestFile(project, "data", "text", ".json");
			
			if(ifile.exists())
				try {
					ifile.setContents(new ByteArrayInputStream("".getBytes()), false, true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
		}
		
		return null;
	}

}
