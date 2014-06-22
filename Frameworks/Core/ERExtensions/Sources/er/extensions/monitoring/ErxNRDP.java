package er.extensions.monitoring;

import java.util.ArrayList;
import java.util.Collection;

import com.webobjects.foundation.NSLog;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.NagiosCheckSender;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.domain.State;
import ch.shamu.jsendnrdp.impl.NonBlockingNagiosCheckSender;
import er.extensions.foundation.ERXProperties;

/**
 * This class is a helper to send messages to Nagios' NRDP, 
 * a HTTP transport that allow you to send passive checks to Nagios.
 * 
 * @see <a href="http://exchange.nagios.org/directory/Addons/Passive-Checks/NRDP--2D-Nagios-Remote-Data-Processor/details">NRDP details</a>
 * @see <a href="https://github.com/m-ryan/jsend-nrdp">jsend-nrdp on GitHub</a>
 * 
 * @author probert
 *
 */

public class ErxNRDP {

	/**
	 * 
	 * @param host The name of the host in Nagios
	 * @param serviceName The name of the service in Nagios. Skip it if you are sending a check for the host instead of a service.
	 * @param serviceState State of the check (OK, WARNING, CRITICAL, UNKNOWN)
	 * @param message The actual result. You can add performance data by adding a pipe ('|') and the data after the message.
	 */
	public static void sendMessage(String host, String serviceName, State serviceState, String message) {
		
		NRDPServerConnectionSettings connectionSettings = new NRDPServerConnectionSettings(
				ERXProperties.stringForKey("erx.monitoring.nrdp.url"), 
				ERXProperties.stringForKey("erx.monitoring.nrdp.token"), 
				ERXProperties.intForKey("erx.monitoring.nrdp.timeout"));
		
		NagiosCheckSender resultSender = new NonBlockingNagiosCheckSender(
				connectionSettings, 
				ERXProperties.intForKeyWithDefault("erx.monitoring.nrdp.nbrThreads",1), 
				ERXProperties.intForKeyWithDefault("erx.monitoring.nrdp.maxQueueSize",0));
		
		NagiosCheckResult resultToSend = new NagiosCheckResult(host, serviceName, serviceState, message);
		Collection<NagiosCheckResult> resultsToSend = new ArrayList<NagiosCheckResult>();
		resultsToSend.add(resultToSend);
		try {
			resultSender.send(resultsToSend);
		}
		catch (Exception e) {
			NSLog.err.appendln("Error sending check result to nagios" + e);
		}
	}

}
