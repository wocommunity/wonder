#if ($entity.packageName)
package $entity.packageName;

#end
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public#if (${entity.abstractEntity}) abstract#end class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(${entity.classNameWithoutPackage}.class);
}
