package com.secretpal.model;

import org.apache.log4j.Logger;

public class SPNoNoPal extends _SPNoNoPal {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(SPNoNoPal.class);

  public SPMembership giverMembership() {
    return event().group().membershipForPerson(giver());
  }

  public SPMembership receiverMembership() {
    return event().group().membershipForPerson(receiver());
  }
}
