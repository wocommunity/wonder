package er.extensions;


import java.io.*;

import com.webobjects.foundation.*;

/**
usefull class in to automatically en- and decode an NSMutableDictionary
 as blob into a database. ERPrototype name = mutableDictionary
*/
public class ERXMutableDictionary extends NSMutableDictionary {
    public static final long serialVersionUID = 8091318522043166356L;

    public static NSData toBlob(ERXMutableDictionary d) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bout);
        oos.writeObject(d);
        oos.close();
        NSData sp = new NSData(bout.toByteArray());
        return sp;
    }
    public static ERXMutableDictionary fromBlob(NSData d) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ERXMutableDictionary dd = (ERXMutableDictionary) ois.readObject();
        ois.close();
        return dd;
    }

    public NSData toBlob() throws Exception {
        return toBlob(this);
    }

    public ERXMutableDictionary(NSDictionary d) {
        super(d);
    }
    public ERXMutableDictionary() {
        super();
    }

    public Object clone() {
        return new ERXMutableDictionary(this);
    }
}
