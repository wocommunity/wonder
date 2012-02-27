package er.extensions.foundation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

public class ERXEOSerializationUtilities {
	
	public static EOEnterpriseObject readEO(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readObject();
		return (EOEnterpriseObject) in.readObject();
	}
	
	public static void writeEO(ObjectOutputStream out, EOEnterpriseObject eo) throws IOException {
		EOEditingContext ec = eo==null?null:eo.editingContext();
		out.writeObject(ec);
		out.writeObject(ec==null?null:eo);
	}
}
