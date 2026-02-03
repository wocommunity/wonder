package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;

public class R2D2WEditEnum<T extends Enum<T>> extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WEditEnum(WOContext context) {
        super(context);
    }
	private String labelID;
	private Class<T> clazz;
	private Object enumItem;

	public void reset() {
		labelID = null;
		clazz = null;
		enumItem = null;
		super.reset();
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
	
	/**
	 * Finds the enum class
	 * @return The enum class
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> enumClass() {
		if(clazz == null) {
			EOAttribute attr = (EOAttribute)d2wContext().valueForKey("smartAttribute");
			clazz = _NSUtilities.classWithName(attr.className());
		}
		return clazz;
	}

	@SuppressWarnings("unchecked")
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

}