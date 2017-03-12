package com.secretpal.model;

public class SPSecretPal extends _SPSecretPal {
  public SPMembership giverMembership() {
    return event().group().membershipForPerson(giver());
  }

  public SPMembership receiverMembership() {
    return event().group().membershipForPerson(receiver());
  }
}
