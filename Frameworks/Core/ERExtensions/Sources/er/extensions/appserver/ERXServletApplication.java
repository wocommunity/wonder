package er.extensions.appserver;

import javax.servlet.ServletContext;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

/**
 * <p>If you are deploying in a servlet container like Tomcat, this application class
 * allows you automatically serve static resources from the "static" directory using
 * the static resource servlet that is built into Tomcat and Jetty.
 * <p>
 * When building you need to copy the static resources into the "static" folder by
 * adding some lines to the end of the "ssdd" target in build.xml:
 * <ul>
 * <li>1) Change the WOAppMode to Deployment</li>
 * <li>2) Copy "WebServerResources/" to "/static/YourApp.woa/Contents/WebServerResources/" directly inside the root of the .war file</li>
 * <li>3) Copy "*.Framework/WebServerResources/" to "/static/Frameworks/*.Framework/WebServerResources/"</li>
 * </ul>
 *
 * @see <a href="http://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/catalina/servlets/DefaultServlet.html">Tomcat 7.0 DefaultServlet Docs</a>
 * 
 * @author john
 *
 */
public class ERXServletApplication extends ERXApplication {

    private boolean didSetBaseUrl = false;
    
    @Override
    public WOResponse dispatchRequest(WORequest request) {
    	if (!didSetBaseUrl) {
    		didSetBaseUrl = true;
    		ServletContext servletContext = (ServletContext) request.userInfo().get("ServletContext");
    		if (servletContext != null) {
    			setApplicationBaseURL(servletContext.getContextPath() + "/static/"); // "static" is the built-in static resource servlet for Tomcat and Jetty
    			setFrameworksBaseURL(servletContext.getContextPath() + "/static/Frameworks/");
    		}
    	}
    	
    	return super.dispatchRequest(request);
    }

}
