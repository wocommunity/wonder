package er.rest.routes.jsr311;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * {@literal @}Paths allows you to specify a set of {@literal @}Path annotations that map onto a single method.
 * </p>
 * 
 * <pre>
 * {@literal @}Paths({@literal {}{@literal @}Path("/person/{@literal {}person:Person{@literal }}"), {@literal @}Path("/people/{@literal {}person:Person{@literal }}"){@literal }})
 * public WOActionResults testAction({@literal @}PathParam("person") Person personParam) {
 *     ...
 * }
 * </pre>
 * 
 * @author mschrag
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Paths {
	Path[] value();
}