package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDAssignment;
import er.r2d2w.ERR2d2w;

public class R2DDerivedCountSortAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public R2DDerivedCountSortAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDerivedCountSortAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDerivedCountSortAssignment(unarchiver);
	}

	public NSArray<Object> dependentKeys(String keyPath) {
		return new NSArray<Object>(new Object[] { D2WModel.PropertyKeyKey });
	}

    public Object sortKeyForList(D2WContext c) {
        return new StringBuilder(c.propertyKey()).append(ERR2d2w.DERIVED_COUNT).toString();
    }
    
    public Boolean propertyIsSortable(D2WContext c) {
    	String dcAttName = new StringBuilder(c.propertyKey()).append(ERR2d2w.DERIVED_COUNT).toString();
    	return (c.entity().attributeNamed(dcAttName) == null)?false:true;
    }
	
}
