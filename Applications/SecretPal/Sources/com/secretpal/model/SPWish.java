package com.secretpal.model;

import er.extensions.eof.ERXEOControlUtilities;

public class SPWish extends _SPWish {
  public boolean isDesire() {
	  return suggestedBy().equals(suggestedFor());
  }
  
  public boolean canDelete(SPPerson currentPerson) {
	  return currentPerson.admin().booleanValue() || ERXEOControlUtilities.eoEquals(suggestedBy(), currentPerson);
  }
}
