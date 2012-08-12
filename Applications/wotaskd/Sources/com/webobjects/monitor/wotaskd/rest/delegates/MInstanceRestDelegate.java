package com.webobjects.monitor.wotaskd.rest.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MInstance;

import er.extensions.eof.ERXQ;
import er.rest.ERXRestContext;

public class MInstanceRestDelegate extends JavaMonitorRestDelegate {
  public Object primaryKeyForObject(Object obj, ERXRestContext context) {
    NSArray<MInstance> objects = ERXQ.filtered(siteConfig().instanceArray(), ERXQ.is("applicationName", ((MInstance)obj).applicationName()).and(ERXQ.is("id", ((MInstance)obj).id())));
    return objects.size() == 0 ? null : objects.objectAtIndex(0);
  }

  public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return new MInstance(((MInstance)id).dictionaryForArchive(), siteConfig());
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return (siteConfig().instanceWithName(null));
  }

}
