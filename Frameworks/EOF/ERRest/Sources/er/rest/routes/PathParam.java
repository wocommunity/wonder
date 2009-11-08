package er.rest.routes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Like JSR-311, @PathParam allows you to annotate an action method parameter to specify
 * that its value should be loaded from the route paths.
 * </p>
 * 
 * <pre>
 * public WOActionResults testAction(@PathParam("person") Person personParam) {
 *     ...
 * }
 * </pre>
 * 
 * <p>
 * This will automatically pass the value of the "person" route object (/person/{person:Person}/test) into
 * your action method as the "personParam" parameter.
 * </p>
 * 
 * @author mschrag
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParam {
	String value();
}
