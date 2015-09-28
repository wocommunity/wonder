package com.webobjects.monitor.application;

import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MInstance;

public class ModProxyPage extends MonitorComponent
{
	private static final long serialVersionUID = 1L;

	public NSArray<String> loadBalencers = new NSArray<String>("byrequests", "bytraffic", "bybusyness");
	public String loadBalancerItem;
	public String loadBalancer = "byrequests";

	public Integer timeout = Integer.valueOf(0);

	public ModProxyPage(WOContext aWocontext)
	{
		super(aWocontext);
	}

	public WOActionResults reload()
	{
		return null;
	}

	public String modProxyContent()
	{
		return _generateModProxyConfig();
	}

	public String modRewriteContent()
	{
		return _generateModRewriteConfig();
	}

	private String _generateModProxyConfig()
	{
		StringBuilder result = new StringBuilder();
		
		result.append("#\n");
		result.append("# Common configuration (if not already set)\n");
		result.append("#\n");
		result.append("ProxyRequests Off\nProxyVia Full\n");
		result.append("#\n");
		result.append("# Give us a name\n");
		result.append("#\n");
		result.append("RequestHeader append x-webobjects-adaptor-version \"mod_proxy\"\n\n\n");

		result.append("#\n");
		result.append("# Balancer routes\n");
		result.append("#\n");

		for (Enumeration<MApplication> e = siteConfig().applicationArray().objectEnumerator(); e.hasMoreElements();)
		{
			MApplication anApp = e.nextElement();
			anApp.extractAdaptorValuesFromSiteConfig();

			String tmpAdaptor = siteConfig().woAdaptor();
			tmpAdaptor = StringUtils.removeEnd(tmpAdaptor, "/");

			NSArray<String> tmpPath = NSArray.componentsSeparatedByString(tmpAdaptor, "/");

			int count = tmpPath.count();
			String adaptorPath = "/" + tmpPath.get(count - 2) + "/" + tmpPath.get(count - 1) + "/";

			result.append("<Proxy balancer://" + anApp.name() + ".woa>\n");

			NSMutableArray<String> reversePathes = new NSMutableArray<String>();

			for (Enumeration<MInstance> e2 = anApp.instanceArray().objectEnumerator(); e2.hasMoreElements();)
			{
				MInstance anInst = e2.nextElement();

				anInst.extractAdaptorValuesFromApplication();

				String host = anInst.values().valueForKey("hostName").toString();
				String port = anInst.values().valueForKey("port").toString();

				String url = "http://" + host + ":" + port + adaptorPath + anApp.name() + ".woa";

				result.append("\tBalancerMember ");
				result.append(url);
				result.append(" route=");
				result.append(_proxyBalancerRoute(anApp.name(), host, port));
				result.append('\n');

				reversePathes.add(url);
			}

			result.append("</Proxy>\n");
			result.append("ProxyPass ");
			result.append(adaptorPath);
			result.append(anApp.name());
			result.append(".woa balancer://");
			result.append(anApp.name());
			result.append(".woa stickysession=");
			result.append(_proxyBalancerCookieName(anApp.name()));
			result.append(" nofailover=On\n");

			for (int i = 0; i < reversePathes.count(); i++)
			{
				String url = reversePathes.objectAtIndex(i);
				result.append("ProxyPassReverse / ");
				result.append(url);
				result.append('\n');
			}
			result.append('\n');

		}

		result.append("#\n");
		result.append("# Balancer configuration\n");
		result.append("#\n");
		for (Enumeration<MApplication> e = siteConfig().applicationArray().objectEnumerator(); e.hasMoreElements();)
		{
			MApplication anApp = e.nextElement();
			anApp.extractAdaptorValuesFromSiteConfig();
			String name = anApp.name();
			result.append("ProxySet balancer://" + name + ".woa");
			if (timeout != null && timeout.intValue() > 0)
			{
				result.append(" timeout=");
				result.append(timeout);
			}
			if (loadBalancer != null)
			{
				result.append(" lbmethod=");
				result.append(loadBalancer);
			}
			else
			{
				result.append(" lbmethod=byrequests");
			}
			result.append('\n');
		}

		result.append("#\n");
		result.append("#\n");
		result.append("#\n");

		result.append('\n');
		return result.toString();
	}

	private static String _proxyBalancerRoute(String name, String host, String port)
	{
		String proxyBalancerRoute = null;

		proxyBalancerRoute = (name + "_" + port).toLowerCase();
		proxyBalancerRoute = proxyBalancerRoute.replace('.', '_');

		return proxyBalancerRoute;
	}

	private static String _proxyBalancerCookieName(String name)
	{
		String proxyBalancerCookieName = null;

		proxyBalancerCookieName = ("routeid_" + name).toLowerCase();
		proxyBalancerCookieName = proxyBalancerCookieName.replace('.', '_');

		return proxyBalancerCookieName;
	}

	private String _generateModRewriteConfig()
	{
		StringBuilder result = new StringBuilder();
		result.append("This is the content of the apache conf file\n\n\n");
		result.append("#\n");
		result.append("# Rewrite Engine\n");
		result.append("#\n");
		result.append("RewriteEngine On\n\n");
		result.append("# Rewrite rules\n");

		NSMutableArray<String> rewriteRules = new NSMutableArray<String>();
		NSMutableArray<String> properitesRules = new NSMutableArray<String>();

		for (Enumeration<MApplication> e = siteConfig().applicationArray().objectEnumerator(); e.hasMoreElements();)
		{
			MApplication anApp = e.nextElement();
			anApp.extractAdaptorValuesFromSiteConfig();

			String tmpAdaptor = siteConfig().woAdaptor();
			tmpAdaptor = StringUtils.removeEnd(tmpAdaptor, "/");

			NSArray<String> tmpPath = NSArray.componentsSeparatedByString(tmpAdaptor, "/");

			int count = tmpPath.count();
			String adaptorPath = "/" + tmpPath.get(count - 2) + "/" + tmpPath.get(count - 1) + "/";

			rewriteRules.add("RewriteRule ^/" + anApp.name().toLowerCase() + "(.*)$ " + adaptorPath + anApp.name() + ".woa");

			properitesRules.add("er.extensions.ERXApplication.replaceApplicationPath.pattern=" + adaptorPath + anApp.name() + ".woa");
			properitesRules.add("er.extensions.ERXApplication.replaceApplicationPath.replace=/" + anApp.name().toLowerCase());
		}

		result.append(rewriteRules.componentsJoinedByString("\n"));
		result.append("\n");
		result.append("\n");
		result.append("\n");
		result.append("This is the content of the application properties file\n\n\n");
		result.append(properitesRules.componentsJoinedByString("\n"));

		result.append("\n");

		result.append('\n');
		return result.toString();
	}
}
