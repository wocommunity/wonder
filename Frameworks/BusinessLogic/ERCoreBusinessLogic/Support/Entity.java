#if ($entity.packageName)
package $entity.packageName;

#end
import com.webobjects.eocontrol.*;

public class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(${entity.classNameWithoutPackage}.class);

    public static final ${entity.classNameWithoutPackage}Clazz clazz = new ${entity.classNameWithoutPackage}Clazz();
    public static class ${entity.classNameWithoutPackage}Clazz extends ${entity.prefixClassNameWithOptionalPackage}.${entity.prefixClassNameWithOptionalPackage}Clazz {
        /* more clazz methods here */
    }

    public interface Key extends ${entity.prefixClassNameWithOptionalPackage}.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
