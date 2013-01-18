package com.webobjects.monitor.wotaskd.rest.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.monitor._private.MApplication;
import com.webobjects.monitor._private.MHost;
import com.webobjects.monitor._private.MInstance;
import com.webobjects.monitor._private.MObject;
import com.webobjects.monitor._private.MonitorException;
import com.webobjects.monitor.wotaskd.DirectAction;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXQ;

public class MApplicationController extends JavaMonitorController {

  public MApplicationController(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults createAction() throws Throwable {
    checkPassword();
    ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
    MApplication application = create(filter);
    siteConfig().addApplication_W(application);
    return response(application, filter);
  }

  @Override
  public WOActionResults destroyAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("mApplication");		
    deleteApplication(application);
    return response(application, ERXKeyFilter.filterWithNone());
  }

  @Override
  public WOActionResults indexAction() throws Throwable {
    checkPassword();
    return response(siteConfig().applicationArray(), ERXKeyFilter.filterWithAttributes());
  }

  @Override
  public WOActionResults showAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("mApplication");	
    return response(application, ERXKeyFilter.filterWithAttributes());
  }

  @Override
  public WOActionResults updateAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("mApplication");		
    update(application, ERXKeyFilter.filterWithAttributes());
    return response(application, ERXKeyFilter.filterWithAttributes());
  }

  public WOActionResults addInstanceAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("name");
    // Old code. The if statement replaces this code along with the addInstanceOnAllHostsAction() method. kib 20110622
    //		addInstance(application, (MHost)routeObjectForKey("host"), false);
    if (request().stringFormValueForKey("host") != null) {
      MHost mHost = siteConfig().hostWithName(request().stringFormValueForKey("host"));
      addInstance(application, mHost, false);
    } else
      addInstance(application, null, true);
    return response(application, ERXKeyFilter.filterWithNone());
  }

  public WOActionResults deleteInstanceAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("name");
    deleteInstance(application, Integer.valueOf(request().stringFormValueForKey("id")));
    return response(application, ERXKeyFilter.filterWithNone());
  }

  public WOActionResults addInstanceOnAllHostsAction() throws Throwable {
    checkPassword();
    MApplication application = (MApplication) routeObjectForKey("name");
    addInstance(application, null, true);
    return response(application, ERXKeyFilter.filterWithNone());
  }

  private void addInstance(MApplication application, MHost host, boolean addToAllHosts) {
    try {
      if (addToAllHosts) {
        for (MHost aHost : siteConfig().hostArray()) {
          siteConfig().addInstances_M(aHost, application, 1);
        }
      } else {
        siteConfig().addInstances_M(host, application, 1);
      }
    } finally {
    }
  }

  private void deleteInstance(MApplication application, Integer instanceId) {
    final MInstance instance = application.instanceWithID(instanceId);
    try {
      siteConfig().removeInstance_M(instance);
    } finally {
    }
  }

  private void deleteApplication(MApplication application) {
    try {
      siteConfig().removeApplication_M(application);
    } finally {
    }
  }

  public WOActionResults infoAction() {
    checkPassword();
    return response(instancesArray(), instanceFilter());
  }

  protected NSArray<MInstance> instancesArray() {
    MApplication application = (MApplication) routeObjectForKey("name");
    String id = request().stringFormValueForKey("id");

    NSArray<MInstance> instances = siteConfig().instanceArray();
    if (application != null) {
      if (id != null) {
        instances = ERXQ.filtered(siteConfig().instanceArray(), ERXQ.is("applicationName", application.name()).and(ERXQ.is("id", id)));
      } else {
        instances = ERXQ.filtered(siteConfig().instanceArray(), ERXQ.is("applicationName", application.name()));
      }
    }    
    return instances;
  }

  public WOActionResults isRunningAction() {
    checkPassword();

    NSArray<MInstance> instances = instancesArray();
    String num = (String) context().request().formValueForKey("num");

    int numberOfInstancesRequested = -1;
    if (num != null && !num.equals("") && !num.equalsIgnoreCase("all")) {
      try {
        numberOfInstancesRequested = Integer.valueOf(num).intValue();
        if (numberOfInstancesRequested > instances.count()) {
          numberOfInstancesRequested = -1;
        }
      } catch (Exception e) {
        // ignore
      }
    }

    int instancesAlive = 0;
    for (MInstance minstance: instances) {
      if (minstance.state == MObject.ALIVE) {
        instancesAlive++;
      }
    }

    String status = "YES";

    if ((numberOfInstancesRequested == -1 && instancesAlive < instances.count()) || instancesAlive < numberOfInstancesRequested) {
      status = "NO";
    }

    return response(status, ERXKeyFilter.filterWithAll());
  }

  public WOActionResults isStoppedAction() {
    checkPassword();
    String status = "YES";

    for (MInstance minstance: instancesArray()) {
      if (minstance.state == MObject.DEAD)
        continue;
      status = "NO";
      break;
    }

    return response(status, ERXKeyFilter.filterWithAll());
  }
  
  public WOActionResults stopAction() throws MonitorException {
    checkPassword();
    for (MInstance minstance: instancesArray()) {
      if (minstance.state == MObject.ALIVE || minstance.state == MObject.STARTING) {
        minstance.state = MObject.STOPPING;
        if (application().localMonitor().stopInstance(minstance) == null)
          throw new MonitorException("No response to STOP " + minstance.displayName());
      }
    }
    return response(ERXHttpStatusCodes.OK);    
  }
  
  public WOActionResults startAction() {
    checkPassword();
    for (MInstance minstance: instancesArray()) {
      if (minstance.state == MObject.DEAD
          || minstance.state == MObject.STOPPING
          || minstance.state == MObject.CRASHING
          || minstance.state == MObject.UNKNOWN) {
        minstance.state = MObject.STARTING;

        String errorMsg = application().localMonitor().startInstance(minstance);
        if (errorMsg != null) {
          NSDictionary element = new NSDictionary(new Object[]{Boolean.FALSE, errorMsg}, DirectAction.errorKeys);
          return response(element, ERXKeyFilter.filterWithAttributes());    
        }    
      }
    }
    return response(ERXHttpStatusCodes.OK);    
  }
  
  public WOActionResults forceQuitAction() throws MonitorException {
    for (MInstance minstance: instancesArray()) {
      minstance.state = MObject.STOPPING;
      if (application().localMonitor().terminateInstance(minstance) == null)
        throw new MonitorException("No response to STOP " + minstance.displayName());
    }
    return response(ERXHttpStatusCodes.OK);
  }

  public ERXKeyFilter instanceFilter() {
    ERXKeyFilter filter = ERXKeyFilter.filterWithNone();
    filter.include("applicationName");
    filter.include("id");
    filter.include("host.name");
    filter.include("port");
    filter.include("deaths");
    filter.include("isRefusingNewSessions");
    filter.include("isScheduled");
    filter.include("schedulingHourlyStartTime");
    filter.include("schedulingDailyStartTime");
    filter.include("schedulingWeeklyStartTime");
    filter.include("schedulingType");
    filter.include("schedulingStartDay");
    filter.include("schedulingInterval");
    filter.include("transactions");
    filter.include("activeSessions");
    filter.include("averageIdlePeriod");
    filter.include("avgTransactionTime");
    filter.include("isAutoRecovering");
    return filter;
  }

}
