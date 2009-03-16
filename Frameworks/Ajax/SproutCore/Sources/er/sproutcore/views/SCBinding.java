package er.sproutcore.views;

import com.webobjects.appserver.WOAssociation;

import er.ajax.AjaxOption;

public class SCBinding extends SCProperty {
	public SCBinding(String name, WOAssociation association) {
		super(name, association, null, AjaxOption.STRING, true);
	}
}
