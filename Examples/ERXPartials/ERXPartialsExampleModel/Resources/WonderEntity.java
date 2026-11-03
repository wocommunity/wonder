#if ($entity.packageName)
package $entity.packageName;

#end
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {
  private static Logger log = LoggerFactory.getLogger(${entity.classNameWithoutPackage}.class);
}
