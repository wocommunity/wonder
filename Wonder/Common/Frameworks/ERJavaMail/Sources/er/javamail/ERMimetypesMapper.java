/*
 $Id$

 ERMimetypesMapper.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOApplication;
import javax.activation.*;
import java.io.*;

import er.extensions.ERXLogger;

public class ERMimetypesMapper
{
    private static final ERXLogger log = ERXLogger.getERXLogger (ERMimetypesMapper.class);

	private static MimetypesFileTypeMap mimetypesMapper = null;
    protected static MimetypesFileTypeMap mapper () {
        if (mimetypesMapper == null) {
            WOResourceManager resourceManager = WOApplication.application ().resourceManager ();
            String path = resourceManager.pathForResourceNamed ("mime.types", "ERJavaMail", null);

            try {
                mimetypesMapper = new MimetypesFileTypeMap (path);
            } catch (IOException e) {
                log.error ("Error when opening 'mime.types' file.\nInstanciating a default MimetypesFileTypeMap ...");
                mimetypesMapper = new MimetypesFileTypeMap ();
            }
        }

        return mimetypesMapper;
    }

	public static String mimeContentTypeForPath (String path) {
		return ERMimetypesMapper.mapper ().getContentType (path);
	}

}
