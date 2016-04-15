#if ($entity.packageName)
package $entity.packageName;

#end
import com.webobjects.eocontrol.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(${entity.classNameWithoutPackage}.class);

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
