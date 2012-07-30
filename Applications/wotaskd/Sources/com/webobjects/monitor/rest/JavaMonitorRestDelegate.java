package com.webobjects.monitor.rest;

import com.webobjects.appserver.WOApplication;
import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.wotaskd.Application;

import er.rest.ERXAbstractRestDelegate;

public abstract class JavaMonitorRestDelegate extends ERXAbstractRestDelegate {

    protected MSiteConfig siteConfig() {
        return ( (Application) WOApplication.application()).siteConfig();
    }

}
