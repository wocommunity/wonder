package er.extensions;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

public class ERXTestUtilities {
	public static URL resourcePathURL(String name, Class clazz) {
		try {
			URL url = clazz.getResource(name);
			if (url == null) {
				String rootPath = System.getProperty("build.root", "build");
				File resourceFile = new File(rootPath + "/ERExtensions.framework/TestResources" + name);
				if (!resourceFile.exists()) {
					resourceFile = new File("Tests/Resources" + name);
					if (!resourceFile.exists()) {
						throw new FileNotFoundException("Unable to find the property list '" + name + "'.");
					}
				}
				url = resourceFile.toURI().toURL();
			}
			return url;
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to load the resource '" + name + "'.", t);
		}
	}
	
	public static NSDictionary dictionaryFromPropertyListNamedInClass(String name, Class clazz) {
		return (NSDictionary) NSPropertyListSerialization.propertyListWithPathURL(ERXTestUtilities.resourcePathURL(name, clazz));
	}
}
