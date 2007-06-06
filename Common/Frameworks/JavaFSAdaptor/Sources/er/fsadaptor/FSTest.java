
/* FSTest - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.fsadaptor;

import java.io.File;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

public final class FSTest {
    private FSTest() {
        /* empty */
    }

    public static void main(String[] args) {
        System.out.println(EOModelGroup.defaultGroup());
        /*
         * com.webobjects.eoaccess.EOAdaptor anAdaptor = new FSAdaptor("FS");
         * EOAdaptorContext aContext = anAdaptor.createAdaptorContext();
         * EOAdaptorChannel aChannel = aContext.createAdaptorChannel(); EOModel
         * aModel = aChannel.describeModelWithTableNames(null);
         */
        // EOModelGroup.defaultGroup().addModel(aModel);
        EOQualifier aPathQualifier = new EOKeyValueQualifier("parent", EOQualifier.QualifierOperatorEqual, args.length > 0 ? args[0] : System.getProperty("user.home"));
        EOQualifier aNameQualifier = new EOKeyValueQualifier("name", (EOQualifier.QualifierOperatorCaseInsensitiveLike), "*M*");
        EOQualifier aNotQualifier = new EONotQualifier(aNameQualifier);
        EOQualifier aQualifier = new EOAndQualifier(new NSArray(new Object[] { aPathQualifier, aNotQualifier }));
        EOFetchSpecification aFetchSpecification = new EOFetchSpecification("FSDirectory", aQualifier, null);
        EOEditingContext anEditingContext = new EOEditingContext();
        NSArray someObjects = anEditingContext.objectsWithFetchSpecification(aFetchSpecification);

        System.out.println("Fetch result for '" + aQualifier + "': " + someObjects.valueForKey("name"));

        if (someObjects != null) {
            EOEnterpriseObject anObject = (EOEnterpriseObject) someObjects.lastObject();
            NSArray someFiles = (NSArray) anObject.valueForKey("files");
            NSArray someDirectories = (NSArray) anObject.valueForKey("directories");

            System.out.println("anObject name: " + anObject.valueForKey("name"));
            System.out.println("someFiles.count: " + someFiles.count());
            System.out.println("someFiles.name: " + someFiles.valueForKey("name"));
            System.out.println("someFiles.content.length: " + someFiles.valueForKeyPath("content.length"));
            System.out.println("someDirectories.count: " + someDirectories.count());
            System.out.println("someDirectories.name: " + someDirectories.valueForKey("name"));
        }
        EOClassDescription aClassDescription = EOClassDescription.classDescriptionForEntityName("FSDirectory");
        EOEnterpriseObject anObject = aClassDescription.createInstanceWithEditingContext(anEditingContext, null);
        anObject.takeValueForKey((System.getProperty("user.home") + File.separator + "FSItemInsertTest"), "absolutePath");

        anEditingContext.insertObject(anObject);
        anEditingContext.saveChanges();

        anEditingContext.deleteObject(anObject);
        anEditingContext.saveChanges();
    }
}
