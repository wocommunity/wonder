package er.rest.routes.jsr311;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Like JSR-311, @QueryParam allows you to annotate an action method parameter to specify
 * that its value should be loaded from the query parameters.
 * </p>
 * 
 * <pre>
 * public WOActionResults testAction(@QueryParam("person") Person personParam) {
 *     ...
 * }
 * </pre>
 * 
 * <p>
 * This will automatically pass the value of the "person" query object (?person=10) into
 * your action method as the "personParam" parameter.
 * </p>
 * 
 * @author mschrag
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {
	String value();
}
