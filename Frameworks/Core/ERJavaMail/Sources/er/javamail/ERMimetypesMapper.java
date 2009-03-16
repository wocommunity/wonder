/*
 $Id$

 ERMimetypesMapper.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOResourceManager;

public class ERMimetypesMapper {
	private static MimetypesFileTypeMap mimetypesMapper = null;

	protected static MimetypesFileTypeMap mapper() {
		if (mimetypesMapper == null) {
			WOResourceManager resourceManager = WOApplication.application().resourceManager();
			InputStream is = resourceManager.inputStreamForResourceNamed("mime.types", "ERJavaMail", null);

			mimetypesMapper = new MimetypesFileTypeMap(is);
			is = null;
		}

		return mimetypesMapper;
	}

	public static String mimeContentTypeForPath(String path) {
		return ERMimetypesMapper.mapper().getContentType(path);
	}

}
