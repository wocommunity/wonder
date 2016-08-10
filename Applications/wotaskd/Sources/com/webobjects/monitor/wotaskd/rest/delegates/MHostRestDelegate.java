package com.webobjects.monitor.wotaskd.rest.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MHost;

import er.extensions.eof.ERXQ;
import er.rest.ERXRestContext;

public class MHostRestDelegate extends JavaMonitorRestDelegate {
  public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return new MHost(siteConfig(), (String)id, MHost.MAC_HOST_TYPE);
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return (siteConfig().hostWithName((String)id));
  }

  public Object primaryKeyForObject(Object obj, ERXRestContext context) {
    NSArray<MHost> objects = ERXQ.filtered(siteConfig().hostArray(), ERXQ.is("name", obj));
    return objects.size() == 0 ? null : objects.objectAtIndex(0);
  }

}
