package er.rest.routes.jsr311;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * @Paths allows you to specify a set of @Path annotations that map onto a single method.
 * </p>
 * 
 * <pre>
 * @Paths({@Path("/person/{person:Person}"), @Path("/people/{person:Person}")})
 * public WOActionResults testAction(@PathParam("person") Person personParam) {
 *     ...
 * }
 * </pre>
 * 
 * @author mschrag
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Paths {
	Path[] value();
}