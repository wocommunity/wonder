package er.rest;

import com.webobjects.eocontrol.EOClassDescription;

/**
 * ERXAbstractRestDelegate is the default implementation of the IERXRestDelegate interface that can handle looking up delegates for non-eo classes, etc.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractRestDelegate implements IERXRestDelegate {
	public ERXAbstractRestDelegate() {
	}

	@Override
	public boolean __hasNumericPrimaryKeys(EOClassDescription classDescription) {
		return false;
	}
}