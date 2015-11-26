package er.extensions.jspservlet;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.jspservlet.WOServletAdaptor;

public class ERXServletAdaptor extends WOServletAdaptor
{
	private static final long serialVersionUID = 1L;
	
    public ERXServletAdaptor() throws ServletException {
		super();
	}

	static void invokeApplicationSetupMethod(final ServletContext servletContext) throws UnavailableException {
        final ClassLoader classLoader = WOServletAdaptor.class.getClassLoader();
        try {
            final String applicationClassName = servletContext.getInitParameter("WOApplicationClass");
            if (applicationClassName == null || "".equals(applicationClassName)) {
                throw new UnavailableException("WOApplicationClass must be defined. Verify your web.xml configuration.");
            }
            
            final Class<?> applicationClass = classLoader.loadClass(applicationClassName);
            final Method method = applicationClass.getMethod("setup", String[].class);
            method.invoke(null, new Object[] {new String[0]});
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new UnavailableException("Error initializing ERXServletAdaptor: " + e.getMessage());
        }
    }
    
	@Override
    public void init() throws ServletException {
        invokeApplicationSetupMethod(this.getServletContext());
        super.init();
        
        // Fix for wocommunity/wonder#642
		NSNotificationCenter.defaultCenter().postNotification(WOApplication.ApplicationDidFinishLaunchingNotification, this);
    }
}