package com.secretpal.model;

public class SPNoNoPal extends _SPNoNoPal {
  public SPMembership giverMembership() {
    return event().group().membershipForPerson(giver());
  }

  public SPMembership receiverMembership() {
    return event().group().membershipForPerson(receiver());
  }
}
