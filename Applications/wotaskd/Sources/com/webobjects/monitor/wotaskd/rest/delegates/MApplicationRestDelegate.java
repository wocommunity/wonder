package com.webobjects.monitor.wotaskd.rest.delegates;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.monitor._private.MApplication;

import er.extensions.eof.ERXQ;
import er.rest.ERXRestContext;

public class MApplicationRestDelegate extends JavaMonitorRestDelegate {
  public Object primaryKeyForObject(Object obj, ERXRestContext context) {
    NSArray<MApplication> objects = ERXQ.filtered(siteConfig().applicationArray(), ERXQ.is("name", obj));
    return objects.size() == 0 ? null : objects.objectAtIndex(0);
  }

  public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return new MApplication((String)id, siteConfig());
  }

  public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context) {
    return (siteConfig().applicationWithName((String)id));
  }

}
