package er.distribution;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eodistribution.EODistributionContext;
import com.webobjects.eodistribution.common.ERDistributionUtils;
import com.webobjects.eodistribution.common._EONotificationCarrier;
import com.webobjects.eodistribution.common._EOReferenceRecordingCoder;
import com.webobjects.eodistribution.common._EOServerInvocation;
import com.webobjects.eodistribution.common._EOServerReturnValue;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.distribution.common.ERReferenceRecordingCoder;

@SuppressWarnings("deprecation")
public class ERDistributionContext extends EODistributionContext {

	public static final Logger log = Logger.getLogger(ERDistributionContext.class);

	public ERDistributionContext(WOSession session) {
		super(session);
		
		try {
			_EOReferenceRecordingCoder newCoder = new ERReferenceRecordingCoder(true);
			newCoder.setDelegate(this);
			Field coderField = EODistributionContext.class.getDeclaredField("_coder");
			coderField.setAccessible(true);
			coderField.set(this, newCoder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ERDistributionContext(WOSession session, EOEditingContext editingContext) {
		super(session, editingContext);
	}
	
	public static long[] DEBUG_GROUPS = {
		NSLog.DebugGroupApplicationGeneration,
		NSLog.DebugGroupArchiving,
		NSLog.DebugGroupAssociations,
		NSLog.DebugGroupComponentBindings,
		NSLog.DebugGroupComponents,
		NSLog.DebugGroupControllers,
		NSLog.DebugGroupDatabaseAccess,
		NSLog.DebugGroupDeployment,
		NSLog.DebugGroupEnterpriseObjects,
		NSLog.DebugGroupFormatting,
		NSLog.DebugGroupIO,
		NSLog.DebugGroupJSPServlets,
		NSLog.DebugGroupKeyValueCoding,
		NSLog.DebugGroupModel,
		NSLog.DebugGroupMultithreading,
		NSLog.DebugGroupParsing,
		NSLog.DebugGroupQualifiers,
		NSLog.DebugGroupReflection,
		NSLog.DebugGroupRequestHandling,
		NSLog.DebugGroupResources,
		NSLog.DebugGroupRules,
		NSLog.DebugGroupSQLGeneration,
		NSLog.DebugGroupTiming,
		NSLog.DebugGroupUserInterface,
		NSLog.DebugGroupValidation,
		NSLog.DebugGroupWebObjects,
		NSLog.DebugGroupWebServices
	};
	
	public static long allowedDebugGroups() {
		long result = 0;
		for (Long group : DEBUG_GROUPS) {
			if (NSLog.debugLoggingAllowedForGroups(group)) {
				result |= group;
			}
		}
		return result;
	}
	
	/**
	 * Enables NSLog logging so errors are not silently ignored
	 */
	@Override
	public NSData responseToClientMessage(NSData message) {
		int savedLogLevel = -2;
		long savedDebugGroups = 0;
		
		if (!NSLog.debugLoggingAllowedForLevelAndGroups(NSLog.DebugLevelInformational, NSLog.DebugGroupIO)) {
			// force IO debugging on so that server exceptions are always logged
			savedLogLevel = NSLog.allowedDebugLevel();
			savedDebugGroups = allowedDebugGroups();
			
			NSLog.setAllowedDebugLevel(NSLog.DebugLevelInformational);
			NSLog.allowDebugLoggingForGroups(allowedDebugGroups() | NSLog.DebugGroupIO);
		}
		
		NSData result = super.responseToClientMessage(message);
	
		if (savedLogLevel != -2) {
			NSLog.setAllowedDebugLevel(savedLogLevel);
			NSLog.allowDebugLoggingForGroups(savedDebugGroups);
		}
		
		return result;
	}
	
	/**
	 * Adds request and response logging
	 */
	@Override
	public NSArray<_EOServerReturnValue> _processClientRequest(@SuppressWarnings("rawtypes") NSArray invocations) {
		int count = invocations.count();
		NSMutableArray<_EOServerReturnValue> results = new NSMutableArray<_EOServerReturnValue>(count);

		for (int i = 0; i < count; ++i) {
			_EOServerInvocation invocation = (_EOServerInvocation)invocations.objectAtIndex(i);
			
			if (!"clientSideRequestGetNotifications".equals(ERDistributionUtils.method(invocation))) {
				log.debug("request:  " + ERDistributionUtils.invocationToString(invocation).replace("\n", ""));
			}
			
			_EOServerReturnValue result = null;
			try {
				result = invocation.doInvokeWithTarget(this);
				if (result == null) {
					log.error("result was null");
				} else if (result.holdsServerException()) {
					log.error(result.serverExceptionClassName() + ": " + result.serverExceptionMessage());
				} else if (log.isDebugEnabled()) {
					logReturnValue(result);
				}
			} catch (RuntimeException e) {
				log.error(e.getMessage(), e);
				throw e;
			}
			
			results.addObject(result);
		}
		return results;
	}
	
	/**
	 * Looks inside otherwise opaque _EOServerReturnValue object and logs the response in greater detail
	 */
	protected void logReturnValue(_EOServerReturnValue result) {
		String message = messageForReturnValue(result);
		if (message != null) {
			log.debug(message);
		}
	}

	/**
	 * Looks inside otherwise opaque _EOServerReturnValue object and logs the response in greater detail
	 */
	protected String messageForReturnValue(_EOServerReturnValue result) {
		if (result.returnValue() instanceof NSArray<?>) { // probably a fetch specification
			StringBuilder message = new StringBuilder();
			NSArray<Object> arrayResult = (NSArray<Object>)result.returnValue();
			for (Object object : arrayResult) {
				if (object instanceof EOEnterpriseObject) {
					EOEnterpriseObject eo = (EOEnterpriseObject)object;
					message.append(eo.toString()).append(" | ").append(eo.snapshot()).append('\n');
				} else {
					message.append(object);
				}
			}
			return "response: \n" + message.toString().trim();
		} else if (result.returnValue() instanceof EOEnterpriseObject) { // probably a fault
			return "response: \n" + result.returnValue().toString() + " | " + ((EOEnterpriseObject)result.returnValue()).snapshot();
		} else if (result.returnValue() instanceof _EONotificationCarrier) { // most often this is the EOObjectsChangedInObjectStoreNotification, which contains updated values for changed records
			_EONotificationCarrier notificationCarrier = (_EONotificationCarrier) result.returnValue();
			NSArray notifications = notificationCarrier.notifications();
			if (!notifications.isEmpty() && notificationCarrier.propertySnapshots() != null) {
				StringBuilder message = new StringBuilder();
				for (Object gid : notificationCarrier.propertySnapshots().keySet()) {
					NSArray snapshot = (NSArray) notificationCarrier.propertySnapshots().get(gid); 
					message.append(gid.toString()).append(" = ").append(snapshot).append('\n');
				}
				message.append("This also contains GIDs for all the toMany relationship related to the objects.");
				return "response: " + result.returnValue().toString().replace("\n", "") + "\n" + message.toString().trim();
			} else {
				return "response: " + result.returnValue().toString().replace("\n", "");
			}
		} else if (result.returnValue() != null) {
			return "response: " + result.returnValue().toString().replace("\n", "");
		}
		return null;
	}

}
