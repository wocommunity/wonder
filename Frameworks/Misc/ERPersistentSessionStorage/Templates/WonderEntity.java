#if ($entity.packageName)
package $entity.packageName;

#end
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;

public#if (${entity.abstractEntity}) abstract#end class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(${entity.classNameWithoutPackage}.class);

    public static final ${entity.classNameWithoutPackage}Clazz<${entity.classNameWithoutPackage}> clazz = new ${entity.classNameWithoutPackage}Clazz<${entity.classNameWithoutPackage}>();
    public static class ${entity.classNameWithoutPackage}Clazz<T extends ${entity.classNameWithoutPackage}> extends ${entity.prefixClassNameWithOptionalPackage}.${entity.prefixClassNameWithoutPackage}Clazz<T> {
        /* more clazz methods here */
    }

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }

}
