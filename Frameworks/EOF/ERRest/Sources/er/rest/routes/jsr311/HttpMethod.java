package er.rest.routes.jsr311;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import er.rest.routes.ERXRoute;

/**
 * <p>
 * JSR-311-esque @HttpMethod allows you to annotate other annotations to declare
 * the type of HTTP method they represent.
 * </p>
 * 
 * @author mschrag
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface HttpMethod {
	ERXRoute.Method value();
}
