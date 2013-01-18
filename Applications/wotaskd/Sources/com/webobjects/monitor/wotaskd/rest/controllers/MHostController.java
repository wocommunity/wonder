package com.webobjects.monitor.wotaskd.rest.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.monitor._private.MHost;

import er.extensions.eof.ERXKeyFilter;

public class MHostController extends JavaMonitorController {

  public MHostController(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults createAction() throws Throwable {
    checkPassword();
    MHost host = create(ERXKeyFilter.filterWithAttributes());
    siteConfig().addHost_M(host);
    return response(host, ERXKeyFilter.filterWithAttributes());
  }

  @Override
  public WOActionResults indexAction() throws Throwable {
    checkPassword();
    return response(siteConfig().hostArray(), ERXKeyFilter.filterWithAttributes());
  }

  @Override
  public WOActionResults showAction() throws Throwable {
    checkPassword();
    MHost host = siteConfig().hostWithName((String) routeObjectForKey("mHost"));		
    return response(host, ERXKeyFilter.filterWithAttributes());
  }

}
