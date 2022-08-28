package er.profiling.classloader;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;

/**
 * The WeavingClassLoader is a custom URLClassLoader that implements the 
 * same functionality as the -javaagent transformation feature, passing all 
 * classes through the ClassTransformer prior to definition.
 * 
 * When this classloader is enabled as a "root loader" and not a conventional hierarchical
 * classloader it is important to use caution as it may not work as expected in some cases.
 * 
 * @author q
 *
 */
public class WeavingClassLoader extends URLClassLoader {
	public static final String ROOT_LOADER_KEY = "gluonj.WeavingClassLoader.isRootLoader";
	public static final String GLUE_NAME_KEY = "gluonj.GlueName";
  private String glueName;
	private ClassFileTransformer transformer;
	
	{
	  glueName = System.getProperty(GLUE_NAME_KEY);
	  
	  if (glueName == null) {
			System.err.println("GlueName is not defined. Transforming is disabled");
		}
	}

	public WeavingClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		if (glueName != null) {
		  try {
		    @SuppressWarnings("unchecked")
		    Class<ClassFileTransformer> clazz = (Class<ClassFileTransformer>) findClass(ClassTransformer.class.getName());
		    Constructor<ClassFileTransformer> c = clazz.getConstructor(new Class<?>[] { String.class });
		    transformer = c.newInstance(glueName);
		  } catch (Exception e) {
		    System.err.println("ClassTransformer could not be initialized. Transforming is disabled");
		    e.printStackTrace(System.err);
		  }
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null)
			return clazz;

		if (isNonTransformable(name)) {
			return super.loadClass(name, resolve);
		}

		if (isRootLoader()) {
		  try {
		    Class<?> c = findClass(name);
		    if (resolve) {
		      resolveClass(c);
		    }

		    return c;
		  } catch (ClassNotFoundException e) {
		  }
		}
    return super.loadClass(name, resolve);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> clazz = findLoadedClass(name);
		if (clazz != null)
			return clazz;

		if (isNonTransformable(name)) {
			return super.findClass(name);
		}

		String path = name.replace('.', '/').concat(".class");
		InputStream in = getSystemResourceAsStream(path);
		if (in != null) {
			try {
				byte[] b = IOUtils.toByteArray(in);
				byte[] transformed = transformer.transform(this, name, null,
						null, b);
				if (transformed == null)
					transformed = b;
				return defineClass(name, transformed, 0, transformed.length);
			} catch (Exception e) {
				return super.findClass(name);
			}
		}
		return super.findClass(name);
	}

	/**
	 * Determines if a class can be transformed based on the name. In most cases
	 * as class is non transformable because it is part of the JRE and loaded by
	 * bootstrap classloader.
	 * 
	 * @param className
	 * @return whether the class is transformable.
	 */
	protected boolean isNonTransformable(String className) {
		return transformer == null 
				|| className.startsWith("java.")
				|| className.startsWith("javax.")
				|| (className.startsWith("com.sun.") && !className
						.startsWith("com.sun.script."))
				|| className.startsWith("sun.")
				|| className.startsWith("sunw.")
				|| className.startsWith("javassist.")
				|| className.startsWith("org.xml.sax.")
				|| className.equals(glueName);
	}
	
	protected boolean isRootLoader() {
	  return Boolean.parseBoolean(System.getProperty(ROOT_LOADER_KEY, "false"));
	}

}
