package er.extensions;


import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.io.*;

/**
usefull class in to automatically en- and decode an NSMutableArray
 as blob into a database. ERPrototype name = mutableArray
 */
public class ERXMutableArray extends NSMutableArray {

    public static NSData toBlob(ERXMutableArray d) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bout);
        oos.writeObject(d);
        oos.close();
        NSData sp = new NSData(bout.toByteArray());
        return sp;
    }
    public static ERXMutableArray fromBlob(NSData d) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(d.bytes());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ERXMutableArray dd = (ERXMutableArray) ois.readObject();
        ois.close();
        return dd;
    }

    public NSData toBlob() throws Exception {
        return toBlob(this);
    }
}
