package com.webobjects.monitor.application;
/*
© Copyright 2006- 2007 Apple Computer, Inc. All rights reserved.

IMPORTANT:  This Apple software is supplied to you by Apple Computer, Inc. (ÒAppleÓ) in consideration of your agreement to the following terms, and your use, installation, modification or redistribution of this Apple software constitutes acceptance of these terms.  If you do not agree with these terms, please do not use, install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and subject to these terms, Apple grants you a personal, non-exclusive license, under AppleÕs copyrights in this original Apple software (the ÒApple SoftwareÓ), to use, reproduce, modify and redistribute the Apple Software, with or without modifications, in source and/or binary forms; provided that if you redistribute the Apple Software in its entirety and without modifications, you must retain this notice and the following text and disclaimers in all such redistributions of the Apple Software.  Neither the name, trademarks, service marks or logos of Apple Computer, Inc. may be used to endorse or promote products derived from the Apple Software without specific prior written permission from Apple.  Except as expressly stated in this notice, no other rights or licenses, express or implied, are granted by Apple herein, including but not limited to any patent rights that may be infringed by your derivative works or by other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS. 

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN  ADVISED OF THE POSSIBILITY OF 
SUCH DAMAGE.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSData;

public class MigrationPage extends MonitorComponent {
	/**
	 * serialVersionUID
	 */
	private static final long	serialVersionUID	= -1317986389844426074L;
	public String host;
	public String username;
	
	// for SSH identity file upload
    public NSData sshIdentityContent;
	public String sshIdentityFilepath;

	public String remoteFilepath;
	public Boolean shouldRestartApache;

	// for UI
	public String migrationStackTrace = null;
	
	// internal use
	public String adaptorConfigContent;
	public String adaptorConfigLocalFilepath;// = "/tmp/http-webobjects.conf";

    public MigrationPage(WOContext aWocontext) {
        super(aWocontext);
    }

	public String adaptorConfigContent(){
		final Application app = (Application)WOApplication.application();
		adaptorConfigContent = app._siteConfig.generateHttpWebObjectsConfig().toString();
		return adaptorConfigContent;
	}
    
	public WOComponent migrate()
    {
	
		FileOutputStream adaptorConfigFileOutputStream = null;
		FileOutputStream sshIdentityFileOutputStream= null;
		try{
			// write the ssh identity file to local disk for scp later
//			sshIdentityFileOutputStream = new FileOutputStream(new File(sshIdentityFilepath));
//			sshIdentityContent.writeToStream(sshIdentityFileOutputStream);
//			sshIdentityFileOutputStream.close();
			// FIXME: Any security concern? SSH identify file saving to local disk
			
			// write the http-webobjects.conf file to /tmp 1st
			adaptorConfigFileOutputStream = new FileOutputStream(new File("/tmp/http-webobjects.conf"));
			FileChannel fc = adaptorConfigFileOutputStream.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(adaptorConfigContent().length());
			buffer.put(adaptorConfigContent.getBytes());
			buffer.flip();
			while ( buffer.hasRemaining()){
				fc.write(buffer);
			}
			
//			String command = "scp -i " + sshIdentityFilepath + " " + adaptorConfigLocalFilepath + " " + username + "@" + host + ":" + remoteFilepath;
//			Runtime rt = Runtime.getRuntime();
//			rt.exec(command); //FIXME: security
			if ( shouldRestartApache.booleanValue()){
//				rt.exec("ssh -i " + sshIdentityFilepath + " apachectl graceful"); //FIXME: double-check with Security team on this before turning this on. --Mankit 7.10.2006
			}
			migrationStackTrace = ""; //signify successsful migration
		}
		catch (Exception e){
			migrationStackTrace = e.getMessage();
		}
		finally{
			try{
				if ( adaptorConfigFileOutputStream != null){
					adaptorConfigFileOutputStream.close();
				}
				if ( sshIdentityFileOutputStream != null){
					sshIdentityFileOutputStream.close();
				}
			}
			catch (Exception e){
					migrationStackTrace = migrationStackTrace + "\n" + e.getMessage();
			}
		}
        return null; //reload same page.
    }
	public boolean getIsMigrationCompleted(){
		if ( migrationStackTrace != null && migrationStackTrace.length() == 0)
			return true;
		return false;
	}
	
    public String getMigrationStackTrace()
    {
        return migrationStackTrace;
    }
    public boolean getIsFailed()
    {
        return (migrationStackTrace != null && migrationStackTrace.length() > 0);
    }
}
