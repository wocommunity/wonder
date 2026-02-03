package er.r2d2w.assignments;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDAssignment;

/**
 * Created this class because it is a bit annoying to have to update
 * ERDDefaultConfigurationNameAssignment every time, and then wait weeks for
 * that update to move from integration to master. This has the same effect, but
 * works dynamically by simply replacing "ConfigurationName" with the entity
 * name and capitalizing the result.
 */
public class R2DDefaultConfigurationNameAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** holds the array of keys this assignment depends upon */
	private static final NSArray<String> _DEPENDENT_KEYS = new NSArray<String>("propertyKey", "object.entityName",
			"entity.name");

	private static final String KEY_FOR_METHOD_LOOKUP = "defaultConfigurationName";

	private static final String REPLACE_TEXT = "ConfigurationName";

	public R2DDefaultConfigurationNameAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultConfigurationNameAssignment(String key, Object value) {
		super(key, value);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver u) {
		return new R2DDefaultConfigurationNameAssignment(u);
	}

	@Override
	public NSArray<String> dependentKeys(String keyPath) {
		return _DEPENDENT_KEYS;
	}

	@Override
	public String keyForMethodLookup(D2WContext c) {
		return KEY_FOR_METHOD_LOOKUP;
	}

	public String defaultConfigurationName(D2WContext c) {
		return Optional.of(keyPath())
			.map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
			.map(s -> s.replaceFirst(Pattern.quote(REPLACE_TEXT), Matcher.quoteReplacement(entityNameForContext(c))))
			.get();
	}

	/**
	 * Calculates the entity name for a given context. If the property is a
	 * relationship (meaning that destinationEntity is set) then this entity is
	 * used
	 * 
	 * @param c
	 *            a D2W context
	 * @return the current entity name for that context.
	 */
	// MOVEME: ERD2WUtilities?
	protected String entityNameForContext(D2WContext c) {
		EOEntity entity = (EOEntity) c.valueForKey("destinationEntity");
		String entityName;
		if (entity != null) {
			entityName = entity.name();
		} else if (c.valueForKey("object") != null && c.valueForKey("object") instanceof EOEnterpriseObject) {
			entityName = ((EOEnterpriseObject) c.valueForKey("object")).entityName();
		} else if (c.entity() != null) {
			entityName = c.entity().name();
		} else {
			entityName = "*all*";
		}
		return entityName;
	}

}
