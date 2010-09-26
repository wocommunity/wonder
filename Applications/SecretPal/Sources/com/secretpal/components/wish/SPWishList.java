package com.secretpal.components.wish;

import com.secretpal.components.application.SPComponent;
import com.secretpal.model.SPWish;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;

public class SPWishList extends SPComponent {
	public SPWish _wish;

	public SPWishList(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public NSArray<SPWish> list() {
		return (NSArray<SPWish>) valueForBinding("list");
	}

	public WOActionResults deleteWish() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		SPWish wish = _wish.localInstanceIn(editingContext);
		wish.delete();
		editingContext.saveChanges();
		return null;
	}

	public WOActionResults togglePurchased() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		SPWish wish = _wish.localInstanceIn(editingContext);
		wish.setPurchased(Boolean.valueOf(!wish.purchased().booleanValue()));
		editingContext.saveChanges();
		return null;
	}
	
	public boolean isMe() {
		return booleanValueForBinding("me");
	}
	
	public boolean showPurchased() {
		return !isMe() && _wish.purchased().booleanValue();
	}
	
	public boolean canDelete() {
		return _wish.canDelete(session().currentPerson().localInstanceIn(_wish.editingContext()));
	}
	
	public String divClass() {
		return "wishList " + stringValueForBinding("class");
	}
	
	public String itemName() {
		return stringValueForBinding("itemName");
	}
}