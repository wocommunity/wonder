package com.secretpal.model;

import org.apache.log4j.Logger;

import er.extensions.eof.ERXEOControlUtilities;

public class SPWish extends _SPWish {
  private static Logger log = Logger.getLogger(SPWish.class);
  
  public boolean isDesire() {
	  return suggestedBy().equals(suggestedFor());
  }
  
  public boolean canDelete(SPPerson currentPerson) {
	  return currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(suggestedBy(), currentPerson);
  }
}
