/**
 * 
 */
package com.webobjects.eoaccess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXEntity provides a basic subclass of EOEntity providing
 * a simple fix for vertical inheritance.
 * 
 * <p><b>Note:</b> If you plan on subclassing EOEntity or ERXEntity you
 * MUST put your subclass in the same package if you want it
 * to work. There are numerous default and protected instance
 * methods within EOEntity itself that will fail to resolve at
 * runtime if your subclass is in another package!
 * 
 * @see EOEntity
 * @author ldeck
 */
public class ERXEntity extends EOEntity {

	private static final Pattern NeededByEOFPattern = Pattern.compile( "\\QNeededByEOF\\E(\\d+)" );
	
	/**
	 * Creates and returns a new ERXEntity.
	 */
	public ERXEntity() {
		super();
	}

	/**
	 * Creates and returns a new EOEntity initialized from the
	 * property list plist belonging to the EOModel owner.
	 * plist is dictionary containing only property list data
	 * types (that is, NSDictionary, NSArray, NSData, and String).
	 * This constructor is used by EOModeler when it reads in an
	 * EOModel from a file.
	 * 
	 * @param plist - A dictionary of property list values from which to initialize the new EOEntity object.
	 * @param owner - The EOModel to which the newly created entity belongs.
	 * 
	 * @see EOPropertyListEncoding#encodeIntoPropertyList(NSMutableDictionary propertyList)
	 * @see EOPropertyListEncoding#awakeWithPropertyList(NSDictionary propertyList)
	 */
	public ERXEntity(NSDictionary plist, Object owner) {
		super(plist, owner);
	}
	
	/**
	 * ldeck radar bug#6302622.
	 * 
	 * <p>Relating two sub-entities in vertical inheritance can fail to resolve
	 *  the foreign key for inserts. i.e., NeededByEOF<index> was not dealt with.
	 *  The simple fix is to return the primary key attribute at the given index.
	 * 
	 * @see com.webobjects.eoaccess.EOEntity#anyAttributeNamed(java.lang.String)
	 */
	@Override
	public EOAttribute anyAttributeNamed(String name) {
		Matcher matcher = null;
		if (name != null && (matcher = NeededByEOFPattern.matcher(name)).matches()) {
			int neededIndex = Integer.valueOf(matcher.group(1));
			if (neededIndex >= primaryKeyAttributeNames().count()) {
				throw new IllegalStateException("No matching primary key found for entity'" + name() + "' with attribute'" + name + "'");
			}
			return (EOAttribute) primaryKeyAttributes().objectAtIndex(neededIndex);
		}
		return super.anyAttributeNamed(name);
	}
	
	/**
	 * Sets the class description for the instance.
	 * 
	 * @param classDescription - the EOClassDescription to associate with the receiver.
	 */
	public void setClassDescription(EOClassDescription classDescription) {
		this._classDescription = classDescription;
	}

}
