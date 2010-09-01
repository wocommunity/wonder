package er.rest.routes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ERXRouteParameter is an annotation that should be applied to a method on an IERXRouteComponent, which will be
 * automatically set from the route keys specified in a URL. For more information, see {@link IERXRouteComponent}.
 * 
 * @author mschrag
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface ERXRouteParameter {

}
