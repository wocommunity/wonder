package er.extensions.eof;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;

/**
 * <h1>ERXQueryEOAttribute.java</h1>
 * <p style="max-width:700px">
 * This subclass of EOAttribute allows you to define attributes with a definition
 * such as "COUNT(DISTINCT lineItems.lineItemID)" that can later be used with
 * ERXQuery.
 * </p>
 * <p style="max-width:700px">
 * What makes this class different from EOAttribute is that you do not have to
 * add the attribute to the entity but you do have to tell the attribute what
 * entity to use via the setAdHocEntity() method.  The entity is used when
 * looking up the properties referenced in the definition of the attribute,
 * i.e. "SUM(lineItems.extendedAmount)".
 * </p>
 * <p style="max-width:700px">
 * Instances of this class can be used with ERXQuery's select() and groupBy()
 * methods.  Their names can be referenced by the orderings passed into
 * the orderBy() method and by qualifiers passed into the where() and having()
 * methods.
 * </p>
 * <p style="max-width:700px">
 * This class provides a factory method to create an instance of this class
 * which encapsulates the correct order in which the name, entity, prototype
 * and definition must be set for the attribute to work properly during
 * SQL generation.
 * </p>
 * 
 * @author Ricardo J. Parada
 */

@SuppressWarnings("javadoc")

public class ERXQueryEOAttribute extends EOAttribute {
	
	protected void setAdHocEntity(EOEntity entity) {
		_parent = entity;
	}
	
	/**
	 * Override to make sure that simple definitions like "SYSDATE" work okay.  
	 */
	@Override
	public void setDefinition(String definition) {
		// If the definition becomes null after setting it then let's try using the
		// readFormat instead.  For example, super.setDefinition("SYSDATE") does not work. On
		// the other hand calling setReadFormat("SYSDATE") and setColumnName("") does the
		// trick.  In general super.setDefinition(definition) does not seem to work for
		// simple oracle function that don't even have parenthesis like "SYSDATE".
		super.setDefinition(definition);
		
		// If the definition was not set above then wrap it in parenthesis
		if (definition() == null) {
			setReadFormat(definition);
			setColumnName("");
		}
	}

	// Factory method

	/**
	 * Creates an instance of this class with the given name, definition and prototype
	 * attribute name.  For example:
	 * <pre>
	 * {@code
	 * EOAttribute attr = 
	 *     ERXQueryEOAttribute.create(
	 *         claimEntity, "totalExpectedAmount", "SUM(expectedAmount)", "currencyAmount2"
	 *     );
	 * }
	 * </pre>
	 */
	public static EOAttribute create(EOEntity entity, String name, String definition, String prototypeAttributeName) {
		// Get prototype attribute
		EOAttribute prototype = entity.model().prototypeAttributeNamed(prototypeAttributeName);
		
		// Create attribute, set name and ad hoc entity *immediately before*
		// setting the prototype and the definition
		ERXQueryEOAttribute attr = new ERXQueryEOAttribute();
		attr.setName(name);
		attr.setAdHocEntity(entity);
		
		// The prototype must be set *after* the ad hoc entity
		attr.setPrototype(prototype);
		
		// The definition must be set *after* the prototype
		attr.setDefinition(definition);
				
		return attr;
	}

	public static ERXQueryEOAttribute create(EOEntity entity, String name, String definition, EOAttribute similarAttribute) {
		// Create attribute, set name and ad hoc entity *immediately before*
		// setting the prototype and the definition
		ERXQueryEOAttribute attr = new ERXQueryEOAttribute();
		attr.setName(name);
		attr.setAdHocEntity(entity);
		
		// The prototype must be set *after* the ad hoc entity
		EOAttribute prototype = similarAttribute.prototype();
		if (prototype != null) {
			attr.setPrototype(prototype);
		} else {
			attr.setClassName(similarAttribute.className());
			attr.setExternalType(similarAttribute.externalType());
			attr.setValueFactoryMethodName(similarAttribute.valueFactoryMethodName());
			attr.setFactoryMethodArgumentType(similarAttribute.factoryMethodArgumentType());
			attr.setAdaptorValueConversionClassName(similarAttribute.adaptorValueConversionClassName());
			attr.setAdaptorValueConversionMethodName(similarAttribute.adaptorValueConversionMethodName());
			attr.setValueType(similarAttribute.valueType());
			attr.setWidth(similarAttribute.width());
			attr.setPrecision(similarAttribute.precision());
		}
		// The definition must be set *after* the prototype
		attr.setDefinition(definition);
		
		return attr;
	}

}