package er.quartzscheduler.foundation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ERQSMyJobListener allows you to set your own job listener that will replace the default one.<p>
 * It can be a complete new one but it's likely a good idea to subclass the default job listener.
 *  
 * @author Philippe Rabier
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ERQSMyJobListener 
{
	String value() default "er.quartzscheduler.foundation.ERQSJobListener";
}
