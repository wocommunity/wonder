package com.secretpal.components.wish;

import com.secretpal.components.application.SPComponent;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPWish;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;

public class SPMembershipWishes extends SPComponent {
	public String _suggestion;
	
	public SPMembershipWishes(WOContext context) {
		super(context);
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String personName() {
		String personName = membership().personName();
		if (isMe()) {
			personName += " (That's You!)";
		}
		if (isSecretPal()) {
			personName += " (Your Secret Pal!)";
		}
		return personName;
	}
	
	public SPMembership membership() {
		return (SPMembership) valueForBinding("membership");
	}

	public boolean isMe() {
		return ERXEOControlUtilities.eoEquals(session().currentPerson(), membership().person());
	}
	
	public boolean isSecretPal() {
		return booleanValueForBinding("secretPal");
	}
	
	public String addSuggestionFunctionName() {
		return "addSuggestionFor" + membership().person().primaryKeyInTransaction();
	}
	
	public String addSuggestionFunctionCall() {
		return addSuggestionFunctionName() + "()";
	}
	
	public String sectionClass() {
		String sectionClass;
		if (isMe()) {
			sectionClass = "section";
			/*
			if (membership().person().desires().count() == 0) {
				sectionClass = "section";
			}
			else {
				sectionClass = "section";
			}
			*/
		}
		else {
			sectionClass = "section" + (isSecretPal() ? " callout2" : "");
		}
		return sectionClass;
	}

	public WOActionResults addSuggestion() {
		if (_suggestion != null && _suggestion.length() > 0) {
			EOEditingContext editingContext = ERXEC.newEditingContext();
			SPWish wish = SPWish.createSPWish(editingContext, Boolean.FALSE, session().currentPerson().localInstanceIn(editingContext), membership().person().localInstanceIn(editingContext));
			wish.setDescription(_suggestion);
			_suggestion = null;
			editingContext.saveChanges();
		}
		return null;
	}
}