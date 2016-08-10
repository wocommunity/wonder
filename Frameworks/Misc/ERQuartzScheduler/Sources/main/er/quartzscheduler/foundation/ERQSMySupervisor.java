package er.quartzscheduler.foundation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ERQSMySupervisor allows you to set your own supervisor that will replace the default one.<p>
 * It can be a complete new one but it's likely a good idea to subclass the default supervisor.
 *  
 * @author Philippe Rabier
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ERQSMySupervisor 
{
	String value() default "er.quartzscheduler.foundation.ERQSJobSupervisor";
}
