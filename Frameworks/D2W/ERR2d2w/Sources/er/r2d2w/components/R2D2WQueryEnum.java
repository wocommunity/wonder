package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

import er.directtoweb.components.relationships.ERD2WQueryToOneRelationship;
import er.extensions.foundation.ERXStringUtilities;
import er.r2d2w.components.relationships.R2DToManyRelationship;
import er.r2d2w.components.relationships.R2DToOneRelationship;

public class R2D2WQueryEnum<T extends Enum<T>> extends ERD2WQueryToOneRelationship {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private Class<T> _klass;
	private Object enumItem;
	private String labelID;
	
	public R2D2WQueryEnum(WOContext context) {
		super(context);
	}

	public void reset() {
		labelID = null;
		_klass = null;
		enumItem = null;
		super.reset();
	}

	public String componentName() {
		return !hasMultipleSelection()
				?R2DToOneRelationship.class.getSimpleName()
				:R2DToManyRelationship.class.getSimpleName();
	}
	
	public Class<T> enumClass() {
		if(_klass == null) {
			EOAttribute attr = (EOAttribute)d2wContext().valueForKey("smartAttribute");
			_klass = _NSUtilities.classWithName(attr.className());
		}
		return _klass;
	}

	public NSArray<T> restrictedChoiceList() {
		//First check for a restricted choice key.
		String restrictedChoiceKey=(String)d2wContext().valueForKey("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 ) {
        	//CHECKME why isn't this d2wContext().valueForKeyPath() in the ERX variety?
            return (NSArray<T>) d2wContext().valueForKeyPath(restrictedChoiceKey);
        }
		
        Class<T> klass = enumClass();
		
        //If no restricted choice key is provided, check for possibleChoices
        NSArray<String> possibleChoices = (NSArray<String>)d2wContext().valueForKey("possibleChoices");
        if(possibleChoices != null) {
        	NSMutableArray<T> result = new NSMutableArray<T>();
        	for(String choice : possibleChoices) {
        		result.add(Enum.valueOf(klass, choice));
        	}
        	return result;
        }
        
		//If possibleChoices does not exist, return all enum values as choices
		return new NSArray<T>(klass.getEnumConstants());
	}

	/**
	 * This method will first return destinationDisplayKey binding. If it is null,
	 * then it will return a localizable key like Color.RED or Status.NORMAL.
	 * @return the display string or key
	 */
	public String enumDisplayString() {
		if(enumItem() instanceof String) {
			return (String)enumItem();
		} else {
			StringBuilder sb = new StringBuilder();
			String destinationDisplayKey = (String)d2wContext().valueForKey("destinationDisplayKey");
			if(destinationDisplayKey == null) {
				sb.append(enumClass().getSimpleName());
				sb.append(NSKeyValueCodingAdditions.KeyPathSeparator);
				sb.append(((T)enumItem()).name());
			} else {
				String displayString = (String)NSKeyValueCodingAdditions.Utility.valueForKeyPath(enumItem(), destinationDisplayKey);
				sb.append(displayString);
			}
			return sb.toString();
		}
	}

	/**
	 * @return the enumItem
	 */
	public Object enumItem() {
		return enumItem;
	}

	/**
	 * @param enumItem the enumItem to set
	 */
	public void setEnumItem(Object enumItem) {
		this.enumItem = enumItem;
	}

	/**
	 * @return the labelID
	 */
	public String labelID() {
		if(labelID == null) {
			labelID = ERXStringUtilities.safeIdentifierName(context().elementID(), "id", '_');
		}
		return labelID;
	}
}