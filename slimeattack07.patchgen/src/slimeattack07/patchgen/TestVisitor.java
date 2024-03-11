package slimeattack07.patchgen;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class TestVisitor implements IResourceVisitor{
	// TODO: Might not need this anymore?
	@Override
	public boolean visit(IResource resource) throws CoreException {
		// TODO: Ignore Watchable.java
		if("java".equals(resource.getFileExtension())) {
			System.out.println(String.format("Visiting %s", resource.getName()));
			System.out.println(resource.getFullPath() + " | " + resource.getProjectRelativePath());
			try {
				String test = resource.getFullPath().toString();
				
				if(!test.contains("Bastion"))
					return true;
				
//				Class<?> cl = Class.forName("");
//				System.out.println(String.format("Class: %s", cl.getName()));
//				
//				 for (Field f : resource.getClass().getDeclaredFields()) {
//					System.out.println(String.format("Looking at field %s", f.getName()));
////					f.getType();
//				}
			} catch(SecurityException e) {
				e.printStackTrace();
				System.out.println("No");
			}
		}
		return true;
	}
}
