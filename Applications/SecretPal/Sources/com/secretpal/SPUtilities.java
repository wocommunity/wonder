package com.secretpal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.secretpal.components.person.SPGroupInvitationEmail;
import com.secretpal.components.person.SPGroupInvitationTextEmail;
import com.secretpal.components.person.SPResetPasswordEmail;
import com.secretpal.components.person.SPResetPasswordTextEmail;
import com.secretpal.model.SPGroup;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.javamail.ERMailDeliveryHTML;
import er.javamail.ERMessage;

public class SPUtilities {
	private static final Logger log = LoggerFactory.getLogger(SPUtilities.class);

	public static final String CONFIRMATION_CODE_KEY = "confirmationCode";
	public static final String RESET_PASSWORD_CODE_KEY = "resetPasswordCode";
	
	public static String confirmationUrl(SPMembership membership, WOContext context) {
		context.generateCompleteURLs();
		String confirmationUrl = context.directActionURLForActionNamed("confirm", new NSDictionary<String, Object>(membership.confirmationCode(), SPUtilities.CONFIRMATION_CODE_KEY), true, false);
		context.generateRelativeURLs();
		return confirmationUrl;
	}

	public static String resetPasswordUrl(SPPerson person, WOContext context) {
		context.generateCompleteURLs();
		String resetPasswordUrl = context.directActionURLForActionNamed("resetPassword", new NSDictionary<String, Object>(person.resetPasswordCode(), SPUtilities.RESET_PASSWORD_CODE_KEY), true, false);
		context.generateRelativeURLs();
		return resetPasswordUrl;
	}

	public static boolean sendEmailToPerson(String subject, WOComponent component, WOComponent plainTextComponent, SPPerson person, ERMessage.Delegate delegate, SPNoticeList errorNoticeList) {
		ERMailDeliveryHTML mail = new ERMailDeliveryHTML();
		mail.setComponent(component);
		mail.setAlternativeComponent(plainTextComponent);

		mail.newMail();
		boolean sentEmail = false;
		try {
			mail.setFromAddress("donotreply@secret-pal.com");
			mail.setReplyToAddress("donotreply@secret-pal.com");
			mail.setSubject(subject);
			mail.setToAddresses(new NSArray<String>(person.emailAddress()));
			mail.setDelegate(delegate);
			mail.sendMail();
			sentEmail = true;
		} catch (Exception e) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			try {
				person.localInstanceIn(editingContext).setEmailDeliveryFailure(Boolean.TRUE);
				editingContext.saveChanges();
			} finally {
				editingContext.unlock();
			}
			editingContext.dispose();
			log.error("Failed to send email to '{}'.", person.emailAddress(), e);
			errorNoticeList.addNotice("Failed to send email: " + e.getMessage());
		}
		return sentEmail;
	}

	public static boolean sendResetPasswordEmail(SPPerson person, WOContext context, SPNoticeList errorNoticeList) {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		SPPerson localPerson = person.localInstanceIn(editingContext);
		localPerson.resetPassword();
		editingContext.saveChanges();

		WOContext contextClone = (WOContext) context.clone();
		SPResetPasswordEmail resetPasswordEmail = ERXApplication.erxApplication().pageWithName(SPResetPasswordEmail.class, contextClone);
		resetPasswordEmail.setPerson(person);
		SPResetPasswordTextEmail resetPasswordTextEmail = ERXApplication.erxApplication().pageWithName(SPResetPasswordTextEmail.class, contextClone);
		resetPasswordTextEmail.setPerson(person);
		return SPUtilities.sendEmailToPerson("SecretPal Password Reset", resetPasswordEmail, resetPasswordTextEmail, person, new SPPersonEmailDelegate(person, errorNoticeList), errorNoticeList);
	}

	public static boolean sendInvitationEmail(SPMembership membership, WOContext context, SPNoticeList errorNoticeList) {
		EOEditingContext editingContext = membership.editingContext();
		membership.resetConfirmation();
		editingContext.saveChanges();

		WOContext contextClone = (WOContext) context.clone();
		SPGroupInvitationEmail validationEmail = ERXApplication.erxApplication().pageWithName(SPGroupInvitationEmail.class, contextClone);
		validationEmail.setMembership(membership);
		SPGroupInvitationTextEmail validationTextEmail = ERXApplication.erxApplication().pageWithName(SPGroupInvitationTextEmail.class, contextClone);
		validationTextEmail.setMembership(membership);
		return SPUtilities.sendEmailToPerson(membership.group().name() + " SecretPal Invitation", validationEmail, validationTextEmail, membership.person(), new SPPersonEmailDelegate(membership.person(), errorNoticeList), errorNoticeList);
	}

	public static void sendInvitationEmails(SPGroup group, String emailAddressesStr, WOContext context, SPNoticeList notificationNoticeList, SPNoticeList errorNoticeList, NSMutableArray<String> failedEmailAddresses) {
		if (emailAddressesStr != null && emailAddressesStr.length() != 0) {
			String[] emailAddresses = emailAddressesStr.split("[\\n;, ]");
			int invitationsSent = 0;
			for (String emailAddress : emailAddresses) {
				String cleansedEmailAddress = SPUtilities.cleanseEmailAddress(emailAddress);
				if (cleansedEmailAddress.length() > 0) {
					EOEditingContext editingContext = ERXEC.newEditingContext();
					SPMembership membership = group.localInstanceIn(editingContext).invite(cleansedEmailAddress);
					editingContext.saveChanges();

					if (!membership.confirmed().booleanValue()) {
						boolean sentEmail = SPUtilities.sendInvitationEmail(membership, context, errorNoticeList);
						if (sentEmail) {
							invitationsSent++;
						}
						else {
							failedEmailAddresses.addObject(membership.person().emailAddress());
						}
					} else {
						invitationsSent++;
					}

					editingContext.saveChanges();
				}
			}

			if (invitationsSent == 0) {
				errorNoticeList.addNotice("Failed to send all invitations.");
			} else {
				notificationNoticeList.addNotice("Processing " + invitationsSent + " invitation(s) ...");
			}
		}
	}
	
	public static String cleanseEmailAddress(String emailAddress) {
	   return emailAddress.replaceAll("[()'\\[\\]<> ]", "").trim();
	}

	public static class SPPersonEmailDelegate implements ERMessage.Delegate {
		private SPNoticeList _errorNoticeList;
		private Object _personGID;

		public SPPersonEmailDelegate(SPPerson person, SPNoticeList errorNoticeList) {
			_personGID = ERXEOControlUtilities.convertEOtoGID(person);
			_errorNoticeList = errorNoticeList;
		}

		public void invalidRecipients(ERMessage message, NSArray<String> invalidRecipientAddresses) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			editingContext.lock();
			try {
				SPPerson person = (SPPerson) ERXEOControlUtilities.convertGIDtoEO(editingContext, _personGID);
				_errorNoticeList.addNotice("Failed to send an email to '" + person.emailAddress() + "'.");
			} finally {
				editingContext.unlock();
				editingContext.dispose();
			}
		}

		public void deliverySucceeded(ERMessage message) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			editingContext.lock();
			try {
				SPPerson person = (SPPerson) ERXEOControlUtilities.convertGIDtoEO(editingContext, _personGID);
				person.setEmailDeliveryFailure(Boolean.FALSE);
				editingContext.saveChanges();
			} finally {
				editingContext.unlock();
				editingContext.dispose();
			}
		}

		public void deliveryFailed(ERMessage message, Throwable failure) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			editingContext.lock();
			try {
				SPPerson person = (SPPerson) ERXEOControlUtilities.convertGIDtoEO(editingContext, _personGID);
				_errorNoticeList.addNotice("Failed to send an email to '" + person.emailAddress() + "'.");
				person.setEmailDeliveryFailure(Boolean.TRUE);
				editingContext.saveChanges();
			} finally {
				editingContext.unlock();
				editingContext.dispose();
			}
			log.error("Failed to send email.", failure);
		}
	}
}
