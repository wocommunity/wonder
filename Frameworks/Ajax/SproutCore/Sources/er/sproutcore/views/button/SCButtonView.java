package er.sproutcore.views.button;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;
import er.sproutcore.SCItem;
import er.sproutcore.views.SCProperty;
import er.sproutcore.views.SCView;

public class SCButtonView extends SCView {
	public SCButtonView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected void addProperties() {
		super.addProperties();

		addProperty("enabled", "isEnabled");

		addProperty("action");
		addProperty("target");

		addProperty("default", "isDefault");
		addProperty("cancel", "isCancel");
		addProperty("value");
		addProperty("theme");
		addProperty("size");
		addProperty("behavior", "buttonBehavior");
		addProperty("toggle_on_value");
		addProperty("toggle_off_value");

		addProperty("key_equivalent", "keyEquivalent");

		addProperty(new SCProperty("isSelected", associationNamed("selected"), null, AjaxOption.DEFAULT, true) {
			@Override
			public String javascriptValue(Object value) {
				String javascriptValue = "mixed".equals(value) ? "SC.MIXED_STATE" : super.javascriptValue(value);
				return javascriptValue;
			}
		});
	}

	@Override
	protected Object defaultElementName() {
		return "a";
	}

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-button-view");
		cssNames.add("button");
		String theme = theme(context);
		if (theme != null) cssNames.add(theme);
		String size = size(context);
		if (size != null) cssNames.add(size);
		String image = image(context);
		if (image != null) cssNames.add(image);
		return cssNames;
	}

	@Override
	protected void pullBindings(WOContext context, SCItem item) {
		super.pullBindings(context, item);
		String theme = defaultTheme(context);
		if (theme != null && !"regular".equals(theme)) {
			item.addProperty(new SCProperty("theme"), theme);
		}
	}

	public String theme(WOContext context) {
		return (String) valueForBinding("theme", defaultTheme(context), context.component());
	}

	public String size(WOContext context) {
		return (String) valueForBinding("size", defaultSize(context), context.component());
	}

	public String image(WOContext context) {
		return (String) valueForBinding("image", context.component());
	}

	public String defaultTheme(WOContext context) {
		return "regular";
	}

	public String defaultSize(WOContext context) {
		return "normal";
	}

	protected String label(WOContext context) {
		String value = null;
		value = (String) valueForBinding("label", value, context.component());
		// value = (String) valueForBinding("value", value,
		// context.component());
		value = (String) valueForBinding("title", value, context.component());
		return value;
	}

	@Override
	protected boolean appendStyleToContainer() {
		return false;
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		String value = label(context);
		if (value == null) {
			value = "";
		}
		response.appendContentString("<span class=\"button-inner\">");
		if (value != null) {
			response.appendContentString("<span class=\"label\"");
			appendStyleToResponse(response, context);
			response.appendContentString(">" + value);
		}

		super.doAppendToResponse(response, context);

		if (value != null) {
			response.appendContentString("</span>");
		}
		response.appendContentString("</span>");
	}
}
