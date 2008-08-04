package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;

/**
 * Implemented by an object that contains a GID and needs to be able to turn
 * into an EO container in a call to ERXEOControlUtilities.convertGIDToEO.
 * 
 * @author mschrag
 */
public interface IERXGIDContainer {
	/**
	 * Returns an EO variant of this object.
	 * 
	 * @param editingContext
	 *            the editing context to resolve EOs with
	 * @return an EO variant of this object
	 */
	public IERXEOContainer toEOContainer(EOEditingContext editingContext);
}