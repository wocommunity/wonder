package er.extensions.eof;

/**
 * Implemented by an object that contains an EO and needs to be able to turn
 * into a "gid-safe" container in a call to
 * ERXEOControlUtilities.convertEOToGID.
 * 
 * @author mschrag
 */
public interface IERXEOContainer {
	/**
	 * Returns a gid variant of this object.
	 * 
	 * @return a gid variant of this object
	 */
	public IERXGIDContainer toGIDContainer();
}