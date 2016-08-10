package er.extensions.foundation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import er.extensions.eof.ERXKeyGlobalID;

/**
 * Fixes errors in serialized streams so they can still handle the classes before the packaging changes in 5.0.
 * @author ak
 *
 */
// ENHANCEME option to set other classes
public class ERXMappingObjectStream extends ObjectInputStream {

	public ERXMappingObjectStream(InputStream inputstream) throws IOException {
		super(inputstream);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass objectstreamclass) throws IOException, ClassNotFoundException {
		Class<?> result = null;
		if(objectstreamclass.getName().equals("er.extensions.ERXMutableArray")) {
			return ERXMutableArray.class;
		}
		if(objectstreamclass.getName().equals("er.extensions.ERXMutableDictionary")) {
			return ERXMutableDictionary.class;
		}
		if(objectstreamclass.getName().equals("er.extensions.ERXKeyGlobalID")) {
			return ERXKeyGlobalID.class;
		}
		result = super.resolveClass(objectstreamclass);
		return result;
	}

}
