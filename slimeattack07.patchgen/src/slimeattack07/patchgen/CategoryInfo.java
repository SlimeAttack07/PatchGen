package slimeattack07.patchgen;

/** Class holding the @CategoryInfo annotation code for the CodeGen handler.
 * 
 */
public class CategoryInfo{
	// TODO: Test if we can change policy to SOURCE.
	/** Returns the code.
	 * 
	 * @return The code.
	 */
	public static String getCode() {
		return """
package patchgen.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CategoryInfo {
	public String id();
}				
		""";
	}
}