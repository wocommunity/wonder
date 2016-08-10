package com.webobjects.monitor.wotaskd.rest.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.monitor._private.MSiteConfig;

import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXStringUtilities;

public class MSiteConfigController extends JavaMonitorController {

  public MSiteConfigController(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults updateAction() throws Throwable {
    checkPassword();
    if (siteConfig().hostArray().count() == 0) {
      throw new IllegalStateException("You cannot update the SiteConfig before adding a host.");
    }
    MSiteConfig siteConfig = (MSiteConfig) object(ERXKeyFilter.filterWithAttributes());
    update(siteConfig, ERXKeyFilter.filterWithAttributes());
    pushValues(siteConfig);
    return response(siteConfig, ERXKeyFilter.filterWithAttributes());
  }

  private void pushValues(MSiteConfig newSiteConfig) {
    // Grab the new and current hashed passwords. Any new password coming in has already been hashed
    // and if we don't have a new password we need the old hashed one to put back into the SiteConfig
    // once we've blatted the values with the new incoming values.
    String newHashedPassword = newSiteConfig.password();
    String currentHashedPassword = siteConfig().password();

    if (!ERXStringUtilities.stringIsNullOrEmpty(newHashedPassword)) {
      // This is needed to populate the passwordDictionary in the request posted to wotaskd.
      siteConfig()._setOldPassword();
    }

    // Now we've cached the new value remove it from the newSiteConfig.
    newSiteConfig.values().removeObjectForKey("password");

    // Build a dictionary of new values. Because we might only be updating a few  values (and not the whole 
    // SiteConfig) we'll start with all the current values, less the password which we've already cached.
    NSMutableDictionary newValues = siteConfig().values();
    newValues.removeObjectForKey("password");

    // Overwrite and/or add the new incoming values.
    newValues.addEntriesFromDictionary(newSiteConfig.values());

    // Push the complete set of new values into the current SiteConfig object.
    siteConfig().updateValues(newValues);

    // OK, let's check what needs to be done with the password. If we've got a new one set that, otherwise
    // if we've got an old one put that back into the SiteConfig.
    if (!ERXStringUtilities.stringIsNullOrEmpty(newHashedPassword)) {
      siteConfig().values().takeValueForKey(newHashedPassword, "password");
    } else if (!ERXStringUtilities.stringIsNullOrEmpty(currentHashedPassword)) {
      siteConfig().values().takeValueForKey(currentHashedPassword, "password");
    }

    if (!ERXStringUtilities.stringIsNullOrEmpty(newHashedPassword)) {
      siteConfig()._resetOldPassword();
    }
  }

}
