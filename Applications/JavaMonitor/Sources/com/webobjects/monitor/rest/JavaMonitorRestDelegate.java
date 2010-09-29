package com.webobjects.monitor.rest;

import com.webobjects.monitor._private.MSiteConfig;
import com.webobjects.monitor.application.WOTaskdHandler;

import er.rest.ERXAbstractRestDelegate;

public abstract class JavaMonitorRestDelegate extends ERXAbstractRestDelegate {

    protected MSiteConfig siteConfig() {
        return WOTaskdHandler.siteConfig();
    }

}
