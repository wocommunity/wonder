package com.webobjects.appserver._private;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXProperties;

/**
 * <p>This class replaces the WebObjects version to support loading a new IP as a local IP to the local hosts list.
 * This will authorize that IP to send management requests, like stopping the application, or
 * turning on the refuse new instance setting. This is necessary if WO does not automatically
 * add the needed IP to the local host list. It happens when you use private IPs and
 * is recognized as a problem on multi-homed linux servers.</p>
 * 
 * <p>When would you need to use this class? If you have a situation where app instances on
 * a specific server do not respond to WOMonitor requests to STOP the instance, REFUSE SESSIONS
 * and/or the instance summary Statistics do not display on the WOMonitor app Detail View page</p>
 * 
 * <p>This version of the class will read a supplemental list of IPs from 
 * the er.extensions.WOHostUtilities.localhostips property.</p>
 * 
 * <p>Usage: set the property above to an array of local IP addresses, for example:
 * <code>er.extensions.WOHostUtilities.localhostips=(192.168.3.160,192.168.3.161,192.168.3.162,192.168.3.162)</code>
 * You can put this property in the machine properties path, /etc/WebObjects/Properties, on each server with the ip
 * addresses assigned to that server, or you can make a list of all ip addresses for all machines in a woa cluster
 * subnet and add it to the app launch args in WOMonitor. Either way, the objective is to ensure that the app will
 * not deny management requests from wotaskd instances that are running on any of the list of supplemental ip addresses
 * that you provide</p>
 * 
 * @author Miguel Arroz (survs.com)
 *
 */

public class WOHostUtilities
{
	private static final String LOCALHOST_IPS_PROPERTY_KEY = "er.extensions.WOHostUtilities.localhostips";
	
	static volatile NSArray _localHosts = null;
	private static final Logger log = LoggerFactory.getLogger(WOHostUtilities.class);
	
	@SuppressWarnings("unchecked")
	static NSArray initLocalHosts()
	{
		NSMutableArray localNSMutableArray = new NSMutableArray();
		try
		{
			InetAddress localInetAddress1 = InetAddress.getLocalHost();
			_addInetAddress(localInetAddress1, localNSMutableArray);
		} catch (Exception localException1) {
			NSLog.err.appendln("<WOHostUtilities>: Couldn't invoke getLocalHost(): " + localException1);
		}

		try
		{
			InetAddress[] arrayOfInetAddress1 = InetAddress.getAllByName("localhost");
			_addInetAddressArray(arrayOfInetAddress1, localNSMutableArray);
		} catch (Exception localException2) {
			NSLog.err.appendln("<WOHostUtilities>: Couldn't get InetAddress for 'localhost': " + localException2);
		}
		try
		{
			InetAddress[] arrayOfInetAddress2 = InetAddress.getAllByName("127.0.0.1");
			_addInetAddressArray(arrayOfInetAddress2, localNSMutableArray);
		} catch (Exception localException3) {
			NSLog.err.appendln("<WOHostUtilities>: Couldn't get InetAddress for '127.0.0.1': " + localException3);
		}
		
		NSArray<String> ips = ERXProperties.arrayForKey( LOCALHOST_IPS_PROPERTY_KEY );
		
		if( ips != null ) {
			for ( String ip : ips ) {
				try {
					InetAddress address = InetAddress.getByName( ip );
					_addInetAddress( address, localNSMutableArray );
					log.debug("Added the address {} as a local host.", address);
				} catch (Exception e) {
					log.error("Could not add localhost IP {}", ip);
				}
			}
		}

		int i = localNSMutableArray.count();
		for (int j = 0; j < i; ++j) {
			InetAddress localInetAddress2 = (InetAddress)localNSMutableArray.objectAtIndex(j);
			try {
				InetAddress[] arrayOfInetAddress3 = InetAddress.getAllByName(localInetAddress2.getHostName());
				_addInetAddressArray(arrayOfInetAddress3, localNSMutableArray);
			} catch (Exception localException4) {
				NSLog.err.appendln("<WOHostUtilities>: Couldn't get InetAddresses for '" + localInetAddress2.getHostName() + "': " + localException4);
			}
		}

		if (NSLog.debugLoggingAllowedForLevelAndGroups(3, 4L))
			NSLog.out.appendln("<WOHostUtilities>: Initialized Local Host List: " + localNSMutableArray);

		return localNSMutableArray;
	}

	static void _addInetAddressArray(InetAddress[] paramArrayOfInetAddress, NSMutableArray paramNSMutableArray)
	{
		if (paramArrayOfInetAddress != null)
			for (int i = 0; i < paramArrayOfInetAddress.length; ++i)
				_addInetAddress(paramArrayOfInetAddress[i], paramNSMutableArray);
	}

	static void _addInetAddress(InetAddress paramInetAddress, NSMutableArray paramNSMutableArray)
	{
		if ((paramInetAddress != null) && (!(paramNSMutableArray.containsObject(paramInetAddress))))
			paramNSMutableArray.addObject(paramInetAddress);
	}

	public static NSArray getLocalHosts()
	{
		return _localHosts;
	}

	public static boolean isLocalInetAddress(InetAddress paramInetAddress, boolean paramBoolean)
	{
		if (paramInetAddress != null) {
			if (WOApplication.application()._unsetHost)
				return _isLocalInetAddress(paramInetAddress, paramBoolean);

			return paramInetAddress.equals(WOApplication.application().hostAddress());
		}

		return false;
	}

	public static boolean isAnyLocalInetAddress(InetAddress paramInetAddress, boolean paramBoolean)
	{
		return ((_isLocalInetAddress(paramInetAddress, paramBoolean)) || (WOApplication.application().hostAddress().equals(paramInetAddress)));
	}

	public static boolean _isLocalInetAddress(InetAddress paramInetAddress, boolean paramBoolean)
	{
		boolean bool = false;
		if (paramInetAddress != null) {
			bool = _localHosts.containsObject(paramInetAddress);
			if ((!(bool)) && (paramBoolean))
			{
				_localHosts = initLocalHosts();
				bool = _localHosts.containsObject(paramInetAddress);
			}
		}
		return bool;
	}
	
	static
	{
		_localHosts = initLocalHosts();
	}
}