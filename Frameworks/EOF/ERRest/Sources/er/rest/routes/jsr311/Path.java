package er.rest.routes.jsr311;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Like JSR-311, @Path allows you to annotate an action method to specify
 * the URL path that maps to it. You may define multiple @Path declarations
 * on a single method using the @Paths annotation.
 * </p>
 * 
 * <pre>
 * @Path("/person/{person:Person}")
 * public WOActionResults testAction(@PathParam("person") Person personParam) {
 *     ...
 * }
 * </pre>
 * 
 * <p>
 * The url pattern uses the same rules as ERXRoute.
 * </p>
 * 
 * @author mschrag
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Path {
	String value();
}
