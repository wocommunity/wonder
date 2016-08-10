package com.webobjects.monitor.wotaskd;
/*
© Copyright 2006 - 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. ("Apple") in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under Apple's copyrights in this original Apple software (the "Apple Software"), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.lang3.CharEncoding;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation._NSCollectionReaderWriterLock;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor._private.String_Extensions;
import com.webobjects.monitor.wotaskd.rest.controllers.MApplicationController;
import com.webobjects.monitor.wotaskd.rest.controllers.MHostController;
import com.webobjects.monitor.wotaskd.rest.controllers.MSiteConfigController;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteRequestHandler;

public class Application extends ERXApplication  {
    private LocalMonitor _localMonitor;
    private MSiteConfig _siteConfig;
    private ListenThread listenThread;
    private LifebeatRequestHandler _lifebeatRequestHandler;
    private Number _port;
    private int _intPort;
    private String _multicastAddress;
    private boolean _shouldWriteAdaptorConfig;
    private boolean _shouldRespondToMulticast;
    public _NSCollectionReaderWriterLock _lock;
    
	//========================================================================================
	//     JMX Instance Variables 
	// ------------------------------------------------------
	private MBeanServer _mbeanServer;		// MBean server
	private String _mbsDomain;				// JMX domain to be used for the mbean server
	private String _jmxPort = null;			// Port number for jmx listener
	private String _jmxAccessFile = null;   // Access filename for JMX client authentication (with comlete path)
	private String _jmxPasswordFile = null; // Password filename for JMX client authentication (with comlete path)

	
    static public void main(String argv[]) {
    	ERXApplication.main(argv, Application.class);
    }

    @Override
    public String defaultRequestHandlerClassName() {
        return "com.webobjects.appserver._private.WODirectActionRequestHandler";
    }

    @Override
    public String name() {
        return "wotaskd";
    }

    @Override
    public Number port() {
        if (_port == null) {
            if (super.port().intValue() > 0) {
                _port = super.port();
            } else {
                _port = Integer.valueOf(1085);
            }
            _intPort = _port.intValue();
        }
        return _port;
    }

    protected int intPort() {
        return _intPort;
    }

    public String multicastAddress() {
        return _multicastAddress;
    }

    @Override
    public boolean allowsConcurrentRequestHandling() {
        return true;
    }

    public MSiteConfig siteConfig() {
        return _siteConfig;
    }

    public void setSiteConfig(MSiteConfig aConfig) {
        // Don't need to call dataHasChanged, since a new MSiteConfig is already dirty
        _siteConfig = aConfig;
    }

    public LocalMonitor localMonitor() {
        return _localMonitor;
    }

    public boolean shouldWriteAdaptorConfig() { return _shouldWriteAdaptorConfig; }
    public boolean shouldRespondToMulticast() { return _shouldRespondToMulticast; }

    public Application() {
        super();
        _lock = new _NSCollectionReaderWriterLock();

        String dd = System.getProperties().getProperty("_DeploymentDebugging");
        if (dd != null) {
            NSLog.debug.setIsVerbose(true);
            NSLog.out.setIsVerbose(true);
            NSLog.err.setIsVerbose(true);
            NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupDeployment);
            if (!NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelInformational)) {
            	NSLog.debug.setAllowedDebugLevel(NSLog.DebugLevelInformational);
            }
        }

        com.webobjects.appserver._private.WOHttpIO._alwaysAppendContentLength = false;
        
        // Setting the ports
        _setLifebeatDestinationPort(intPort());

        // Setting the multicast Port
        _multicastAddress = System.getProperties().getProperty("WOMulticastAddress");
        if (_multicastAddress == null) {
            _multicastAddress = "239.128.14.2";
        }

        // registering the lifebeat request handler
        _lifebeatRequestHandler = new LifebeatRequestHandler();
        registerRequestHandler(_lifebeatRequestHandler, "wlb");

        // unregistering the WOComponent / WOResource request handlers
        removeRequestHandlerForKey("wo");
        removeRequestHandlerForKey("wr");
        removeRequestHandlerForKey("womp");

        // getting the siteConfig (+ all Hosts, Apps, Instances) from disk
        _siteConfig = MSiteConfig.unarchiveSiteConfig(true);
        _siteConfig.archiveSiteConfig();

        // creating the localMonitor (used to control and query instances)
        _localMonitor = new LocalMonitor();

        
        // checking to see if we should save WOConfig.xml to disk for the adaptors.
        String WOSavesAdaptorConfig = System.getProperties().getProperty("WOSavesAdaptorConfiguration");
        if (WOSavesAdaptorConfig != null) {
            _shouldWriteAdaptorConfig = String_Extensions.boolValue(WOSavesAdaptorConfig);
            if (_shouldWriteAdaptorConfig) {
                _siteConfig.archiveAdaptorConfig();
            }
        } else {
            _shouldWriteAdaptorConfig = false;
        }

        // checking to see if we should respond to adaptor multicast queries
        // we will always respond to non-multicast UDP packets
        String shouldMC = System.getProperties().getProperty("WORespondsToMulticastQuery");
        if (shouldMC != null) {
            if (!String_Extensions.boolValue(shouldMC)) {
                _shouldRespondToMulticast = false;
                NSLog.debug.appendln("Multicast Response Disabled");
            } else {
                _shouldRespondToMulticast = true;
                NSLog.debug.appendln("Multicast Response Enabled");
            }
        }

        //JMX Support
		_jmxPort = System.getProperty("WOJMXPort");
		_jmxAccessFile = System.getProperty("WOJMXAccessFile");
		_jmxPasswordFile = System.getProperty("WOJMXPasswordFile");
		if (_jmxPort != null) {
			registerMBean(SiteConfig.getInstance(), "WotaskdJMXMBean",  "SiteConfigMBean");
			setupRemoteMonitoring();
		}
		
        // Set up multicast listen thread
        createRequestListenerThread();
        
        ERXRouteRequestHandler restHandler = new ERXRouteRequestHandler();
        restHandler.addDefaultRoutes("MApplication", false, MApplicationController.class);
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/addInstance", ERXRoute.Method.Get, MApplicationController.class, "addInstance"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/deleteInstance", ERXRoute.Method.Get, MApplicationController.class, "deleteInstance"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/info", ERXRoute.Method.Get, MApplicationController.class, "info"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/info", ERXRoute.Method.Get, MApplicationController.class, "info"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/isRunning", ERXRoute.Method.Get, MApplicationController.class, "isRunning"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/isRunning", ERXRoute.Method.Get, MApplicationController.class, "isRunning"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/isStopped", ERXRoute.Method.Get, MApplicationController.class, "isStopped"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/isStopped", ERXRoute.Method.Get, MApplicationController.class, "isStopped"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/start", ERXRoute.Method.Get, MApplicationController.class, "start"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/start", ERXRoute.Method.Get, MApplicationController.class, "start"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/stop", ERXRoute.Method.Get, MApplicationController.class, "stop"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/stop", ERXRoute.Method.Get, MApplicationController.class, "stop"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/forceQuit", ERXRoute.Method.Get, MApplicationController.class, "forceQuit"));
        restHandler.insertRoute(new ERXRoute("MApplication","/mApplications/{name:MApplication}/forceQuit", ERXRoute.Method.Get, MApplicationController.class, "forceQuit"));
        restHandler.addDefaultRoutes("MHost", false, MHostController.class);
        restHandler.addDefaultRoutes("MSiteConfig", false, MSiteConfigController.class);
        restHandler.insertRoute(new ERXRoute("MSiteConfig","/mSiteConfig", ERXRoute.Method.Put, MSiteConfigController.class, "update"));

        ERXRouteRequestHandler.register(restHandler);
        
        boolean isSSHServerEnabled = ERXProperties.booleanForKeyWithDefault("er.wotaskd.sshd.enabled", false);
        
        if (isSSHServerEnabled) {
          SshServer sshd = SshServer.setUpDefaultServer();
          sshd.setPort(ERXProperties.intForKeyWithDefault("er.wotaskd.sshd.port", 6022));
          sshd.setPasswordAuthenticator(new SshPasswordAuthenticator());
          sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
          sshd.setCommandFactory(new ScpCommandFactory());
          sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystem.Factory()));
          sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/bash", "-i", "-l" }));
          try {
            sshd.start();
          }
          catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
    }
    
    public class SshPasswordAuthenticator implements PasswordAuthenticator {

      public boolean authenticate(String username, String password, ServerSession serversession) {
        return (siteConfig().compareStringWithPassword(password)) ? true: false;
      }
      
    }
    
	/**
	 * ============================================================================================
	 *						Methods Added for Enabling JMX in Wotaskd
	 * ============================================================================================
	 * This methods registers the MBean object in the MBeanServer
	 * @param objMBean		- The MBean object to register
	 * @param strDomainName - Domain name required for creating the ObjectName of the MBean
	 * @param strMBeanName  - Name of the MBean
	 */
	@Override
	public void registerMBean(Object objMBean, String strDomainName, String strMBeanName) throws IllegalArgumentException{
		if (objMBean == null)
			throw new IllegalArgumentException("Error: Could not register null to PlatformMbeanServer.");
		if (strMBeanName == null)
			throw new IllegalArgumentException("Error: MBean name could not be null.");
		
		ObjectName objName = null;
		strDomainName = (strDomainName == null) ? getJMXDomain() : strDomainName;
		
		//Create the Object Name for the MBean
		try {
			objName = new ObjectName(strDomainName + ": name=" + strMBeanName);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// Register the MBean
		try {
			getMBeanServer().registerMBean(objMBean, objName);
		} catch (IllegalAccessException e) {
			NSLog.err.appendln("ERROR: security access problem registering bean: "+objMBean+" with ObjectName: "+objName+" "+e.toString());
		} catch (InstanceAlreadyExistsException e) {
			NSLog.err.appendln("ERROR: MBean already exists bean: "+objMBean+" with ObjectName: "+objName+" "+e.toString());
		} catch (MBeanRegistrationException e) {
			NSLog.err.appendln("ERROR: error registering bean: "+objMBean+" with ObjectName: "+objName+" "+e.toString());
		} catch (NotCompliantMBeanException e) {
			NSLog.err.appendln("ERROR: error registering bean: "+objMBean+" with ObjectName: "+objName+" "+e.toString());
		} 
	}
	
	/**
	 * ============================================================================================
	 *						Methods Added for Enabling JMX in Wotaskd
	 * ============================================================================================
	 * This methods creates the JMX Domain Name by appending the hostname, application
	 * name and the port. This is called from method registerMBean() whenever domain
	 * name is passed as null.
	 * @return _mbsDomain  - String containing the Domain name to be used while registering the MBean
	 */
	@Override
    public String getJMXDomain() {
		if (_mbsDomain == null) {
			_mbsDomain = host() + "." + name() + "." + port();
		}
		return _mbsDomain;
    }

	/**
	 * ============================================================================================
	 *						Methods Added for Enabling JMX in Wotaskd
	 * ============================================================================================
	 * This methods sets up this application for remote monitoring. This method creates a new 
	 * connector server and associates it with the MBean Server. The server is started by calling
	 * the start() method. The connector server listens for the client connection requests and
	 * creates a connection for each one.
	 */
	public void setupRemoteMonitoring() {
		if (_jmxPort != null) {
			// Create an RMI connector and start it
			try {
				// Get the port difference to use when creating our new jmx listener
				int intWotaskdJmxPort = Integer.parseInt(_jmxPort);
				
				// Set up the Password and Access file
				HashMap<String, String> envPwd = new HashMap<String, String>();																
				envPwd.put("jmx.remote.x.password.file", _jmxPasswordFile);	
				envPwd.put("jmx.remote.x.access.file", _jmxAccessFile);		
				
				// setup our listener
				java.rmi.registry.LocateRegistry.createRegistry(intWotaskdJmxPort);
				JMXServiceURL jsUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"+host()+":"+intWotaskdJmxPort+"/jmxrmi");
				NSLog.debug.appendln("Setting up monitoring on url : " + jsUrl);

				// Create an RMI Connector Server
				JMXConnectorServer jmxCS = JMXConnectorServerFactory.newJMXConnectorServer(jsUrl, envPwd, getMBeanServer());

				jmxCS.start();
			} catch (Exception anException) {
				NSLog.err.appendln("Error starting remote monitoring: " + anException);
			}
		}
	}
	
	/**
	 * ============================================================================================
	 *						Methods Added for Enabling JMX in Wotaskd
	 * ============================================================================================
	 * This methods returns the platform MBean Server from the Factory
	 * @return _mbeanServer  - The platform MBeanServer 
	 */
	@Override
	public MBeanServer getMBeanServer() throws IllegalAccessException {
		if (_mbeanServer == null) {
			_mbeanServer = ManagementFactory.getPlatformMBeanServer();	
			if (_mbeanServer == null)
				throw new IllegalAccessException("Error: PlatformMBeanServer could not be accessed via ManagementFactory.");
		}
		return _mbeanServer;
    }
	
	/**
	 * Added by X-Provision Team
	 * This method reads the SiteConfig.xml file whenever it is invoked by the SiteConfigMBean
	 */
	public void readSiteConfigXML() {
		NSLog.debug.appendln("Inside readSiteConfigXML method of Application.java: Calling unarchiveSiteConfig");
		_siteConfig = MSiteConfig.unarchiveSiteConfig(true);

		NSLog.debug.appendln("Inside readSiteConfigXML method of Application.java: Calling archiveSiteConfig");
		_siteConfig.archiveSiteConfig();
	}
	

    // sleep will check if there have been changes to the siteConfig.
    // if so, it will write the new siteConfig to disk as SiteConfig.xml
    // if requested, it will also write the new adaptorConfig to disk as WOConfig.xml
    @Override
    public void sleep() {
        _lock.startReading();
        try {
            if ( (_siteConfig != null) && (_siteConfig.hasChanges()) ) {
                // archiving the siteConfig
                _siteConfig.archiveSiteConfig();
                if (_shouldWriteAdaptorConfig) {
                    _siteConfig.archiveAdaptorConfig();
                }
                _siteConfig.resetChanges();
            }
        } finally {
            _lock.endReading();
        }
    }

    // creates and starts the ListenerThread inner class
    public void createRequestListenerThread() {
        if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
            NSLog.debug.appendln("Detaching request listen thread");
        listenThread = new Application.ListenThread();
        listenThread.start();
    }

    // cleans up after the Application (specifically the ListenThread)
    @Override
    public void finalize() throws Throwable {
        listenThread.closeRequestSocket();
        listenThread.stop();
        super.finalize();
    }

    // Overridden createRequest because WO ObjC apps send 'GET /... HTTP/1.0 ' (note extra space) which doesn't parse very well.
    public WORequest createRequest(String aMethod, String aURL, String anHTTPVersion, NSDictionary someHeaders, NSData aContent, NSDictionary someInfo) {
        if ( (anHTTPVersion == null) && (aURL != null) && (aURL.endsWith(" HTTP/1.0")) ) {
            anHTTPVersion = MObject._HTTP1;
            aURL = aURL.substring(0, (aURL.length() - MObject._HTTP1.length() - 1) );
        }
        return super.createRequest(aMethod, aURL, anHTTPVersion, someHeaders, aContent, someInfo);
    }

    // overridden dispatch of requests, for faster lifebeat checking
    // if it's a lifebeat, we return a null response, and that should close the socket immediately
    @Override
    public WOResponse dispatchRequest(WORequest aRequest) {
        WORequestHandler aHandler = handlerForRequest(aRequest);
        if ( (aHandler != null) && (aHandler == _lifebeatRequestHandler) ) {
            _TheLastApplicationAccessTime = System.currentTimeMillis();
            return aHandler.handleRequest(aRequest);
        }
        return super.dispatchRequest(aRequest);
    }



    // Inner class used to listen to Multicast Queries and UDP queries
    class ListenThread extends Thread {
        MulticastSocket socket;
        InetAddress address;

        private void createRequestSocket() {
            // Create a new MulticastSocket, even if we're not listening for Multicast
            // MulticastSocket acts just like a DatagramSocket
            try {
                socket = new MulticastSocket(intPort());
                if (!WOApplication.application()._unsetHost) {
                    socket.setInterface(WOApplication.application().hostAddress());
                }
            } catch (IOException exception) {
                NSLog.err.appendln("Unable to create multicast listener socket: " + exception);
                NSLog.err.appendln("Port " + intPort() + " may be in use by another application.");
                NSLog.err.appendln("Exiting...");
                System.exit(1);
            }

            if (_shouldRespondToMulticast) {
                try {
                    address = InetAddress.getByName(multicastAddress());
                } catch (UnknownHostException exception) {
                    NSLog.err.appendln("Error resolving address: " + multicastAddress() + " - " + exception);
                    NSLog.err.appendln("Exiting...");
                    System.exit(1);
                }

                if (!address.isMulticastAddress()) {
                    NSLog.err.appendln(address + " is not a valid multicast address");
                    NSLog.err.appendln("Exiting...");
                    System.exit(1);
                }

                try {
                    socket.joinGroup(address);
                } catch (IOException exception) {
                    NSLog.err.appendln("Error joining multicast group: " + exception);
                    NSLog.err.appendln("Exiting...");
                    System.exit(1);
                }
            }
        }

        public void closeRequestSocket() {
            try {
                socket.leaveGroup(address);
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln("Leaving multicast group");
            } catch (IOException exception) {
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln("Error leaving multicast group " + exception);
                return;
            }
            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupDeployment))
                NSLog.debug.appendln("Closing request listen socket");
            socket.close();
        }

        public void sendReplyWithLengthTo(byte[] aReplyBytes, int aReplyBytesLength, DatagramPacket incomingPacket) {
            DatagramPacket outgoingPacket = new DatagramPacket(aReplyBytes, aReplyBytesLength, incomingPacket.getAddress(), incomingPacket.getPort());

            try {
                socket.send(outgoingPacket);
            } catch(IOException localException) {
                NSLog.err.appendln("Error sending reply: " + localException + " (ignored)");
            }
        }

        private boolean byteArrayStartsWith(byte[] anArray, byte[] anotherArray, int aLength) {
            for (int i = 0 ; i < aLength ; i++) {
                if (anArray[i] != anotherArray[i]) {
                    return false;
                }
            }
            return true;
        }

        // This is the main thread - we just look for a UDP packet that matches a known signature.
        public void listenForRequests() {
            try {
                String myName = WOApplication.application().host().toLowerCase() + ":" + intPort();
                
                byte[] multicastRequest;
                byte[] multicastReply;
                byte[] versionRequest;
                byte[] versionReply;
                try {
                    multicastRequest = ("GET CONFIG-URL").getBytes(CharEncoding.UTF_8);
                    multicastReply = ("http://" +  myName + '\0').getBytes(CharEncoding.UTF_8);
                    versionRequest = ("womp://queryVersion").getBytes(CharEncoding.UTF_8);
                    versionReply = ("womp://replyVersion/" + myName + ":webObjects5.0" + '\0').getBytes(CharEncoding.UTF_8);
                } catch (UnsupportedEncodingException uee) {
                    multicastRequest = ("GET CONFIG-URL").getBytes();
                    multicastReply = ("http://" +  myName + '\0').getBytes();
                    versionRequest = ("womp://queryVersion").getBytes();
                    versionReply = ("womp://replyVersion/" + myName + ":webObjects5.0" + '\0').getBytes();
                }

                int multicastRequestLength = multicastRequest.length;
                int multicast_reply_len = multicastReply.length;
                int versionRequestLength = versionRequest.length;
                int version_reply_len = versionReply.length;

                byte[] mbuffer = new byte[1000];
                DatagramPacket incomingPacket = new DatagramPacket(mbuffer, mbuffer.length);

                while (socket != null) {
                    try {
                        incomingPacket.setLength(mbuffer.length);
                        socket.receive(incomingPacket);
                        if (byteArrayStartsWith(incomingPacket.getData(), multicastRequest, multicastRequestLength)) {
                            // this responds with the DirectAction URL for getting our adaptor Config XML
                            sendReplyWithLengthTo(multicastReply, multicast_reply_len, incomingPacket);
                        } else if (byteArrayStartsWith(incomingPacket.getData(), versionRequest, versionRequestLength)) {
                            // This is if someone asks us what version we are
                            sendReplyWithLengthTo(versionReply, version_reply_len, incomingPacket);
                        } else {
                            // This is if we get an unrecognized packet.
                            String key = incomingPacket.getAddress() + ":" + incomingPacket.getPort();

                            siteConfig().globalErrorDictionary.takeValueForKey( (myName + ": Unrecognized UDP packet: " + new String(incomingPacket.getData()) + " from " + key + ". This may be an Application that conforms to an older protocol.") , key);
                            if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                                NSLog.debug.appendln(myName + ": Unrecognized UDP packet: " + new String(incomingPacket.getData()) + " from " + key + ". This may be an Application that conforms to an older protocol.");
                        }
                    } catch(IOException localException) {
                        NSLog.err.appendln("Error receiving packet: " + localException + " (ignored)");
                    }

                }

                // Hari-kiri - but should never happen, of course.
                NSLog.err.appendln("wotaskd listen thread exiting because of bad socket");
            } catch (Throwable t) {
                NSLog.err.appendln("Listen thread exiting with exception: " + t);
                if (NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelCritical, NSLog.DebugGroupDeployment))
                    NSLog.debug.appendln(t);
            }
            System.exit(1);
        }

        @Override
        public void run() {
            createRequestSocket();
            NSLog.debug.appendln("Created UDP socket; listening for requests...");
            listenForRequests();
        }
    }
}
