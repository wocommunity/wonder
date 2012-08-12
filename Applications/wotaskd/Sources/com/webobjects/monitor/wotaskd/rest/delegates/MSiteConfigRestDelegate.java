package com.webobjects.monitor.wotaskd.rest.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.monitor._private.MSiteConfig;

import er.rest.ERXRestContext;

public class MSiteConfigRestDelegate extends JavaMonitorRestDelegate {
  
  public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return new MSiteConfig(null);
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return siteConfig();
  }

  public Object primaryKeyForObject(Object obj, ERXRestContext context) {
    return siteConfig();
  }
}
