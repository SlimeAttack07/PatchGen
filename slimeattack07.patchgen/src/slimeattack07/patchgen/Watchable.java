package slimeattack07.patchgen;

/** Class holding the @Watchable annotation code for the CodeGen handler.
 * 
 */
public class Watchable{
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
@Target(ElementType.FIELD)
public @interface Watchable {
	public String id();
	public String name() default "";
	public String category() default "";
}				
		""";
	}
}