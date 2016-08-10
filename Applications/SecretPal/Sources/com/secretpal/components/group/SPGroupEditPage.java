package com.secretpal.components.group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.secretpal.SPUtilities;
import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPEvent;
import com.secretpal.model.SPGroup;
import com.secretpal.model.SPMembership;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEC;

public class SPGroupEditPage extends SPPage {
  private static final Logger log = LoggerFactory.getLogger(SPUtilities.class);
  private SPGroup _group;
  public SPMembership _membership;
  public String _inviteEmailAddresses;
  public SPEvent _event;

  public SPGroupEditPage(WOContext context) {
    super(context);
  }

  @Override
  protected void checkAccess() throws SecurityException {
    super.checkAccess();
    if (!_group.canEdit(session().currentPerson().localInstanceIn(_group.editingContext()))) {
      throw new SecurityException("You do not have permission to edit this group.");
    }
  }

  public void setGroup(SPGroup group) {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    _group = group.localInstanceIn(editingContext);
  }

  public SPGroup group() {
    return _group;
  }

  public boolean canDeleteMembership() {
    return _membership.canDelete(session().currentPerson().localInstanceIn(_group.editingContext()));
  }

  public String statusImage() {
    String statusImage;
    if (_membership.person().emailDeliveryFailure().booleanValue()) {
      statusImage = "icon_err.png";
    }
    else if (!_membership.confirmed().booleanValue()) {
      statusImage = "icon_warn.png";
    }
    else {
      statusImage = "icon_ok.png";
    }
    return statusImage;
  }

  public String statusMessage() {
    String statusMessage;
    if (_membership.person().emailDeliveryFailure().booleanValue()) {
      statusMessage = "Email delivery failed.";
    }
    else if (!_membership.confirmed().booleanValue()) {
      statusMessage = "Waiting for member to accept invitation.";
    }
    else {
      statusMessage = "Everything's good!";
    }
    return statusMessage;
  }

  public String confirmationUrl() {
    return SPUtilities.confirmationUrl(_membership, context());
  }

  public WOActionResults deleteMembership() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    _membership.localInstanceIn(editingContext).delete();
    editingContext.saveChanges();
    return null;
  }

  public WOActionResults sendMembershipInvites() {
    NSMutableArray<String> failedEmailAddresses = new NSMutableArray<String>();
    SPUtilities.sendInvitationEmails(_group, _inviteEmailAddresses, context(), session().notifications(), session().errors(), failedEmailAddresses);
    if (failedEmailAddresses.count() == 0) {
      _inviteEmailAddresses = null;
    }
    else {
      _inviteEmailAddresses = failedEmailAddresses.componentsJoinedByString("\n");
    }
    return null;
  }

  public WOActionResults resendInvitation() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    SPMembership localMembership = _membership.localInstanceIn(editingContext);
    try {
      SPUtilities.sendInvitationEmail(localMembership, context(), session().errors());
      session().notifications().addNotice("Processing invitation to '" + localMembership.personName() + "'.");
    }
    catch (Exception e) {
      localMembership.person().setEmailDeliveryFailure(Boolean.TRUE);
      session().errors().addNotice("Failed to send invitation to '" + localMembership.personName() + "': " + e.getMessage());
      log.error("Failed to send invitation to '{}'.", localMembership.personName(), e);
    }
    return null;
  }

  public WOActionResults saveGroup() {
    if (session().errors().hasNotices()) {
      return null;
    }

    _group.editingContext().saveChanges();
    SPGroupPage groupPage = pageWithName(SPGroupPage.class);
    groupPage.setGroup(_group);
    return groupPage;
  }

  public WOActionResults deleteGroup() {
    _group.delete();
    _group.editingContext().saveChanges();
    return pageWithName(SPHomePage.class);
  }
}