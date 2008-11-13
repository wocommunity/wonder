package er.sproutcore.views.collection;

import java.util.Set;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxValue;
import er.sproutcore.views.SCProperty;
import er.sproutcore.views.SCView;

public class SCCollectionView extends SCView {
	public SCCollectionView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected void addProperties() {
		super.addProperties();
		addProperty("content");
		addProperty("selection");
		addProperty("toggle", "useToggleSelection");
		addProperty("selectable", "isSelectable");
		addProperty("enabled", "isEnabled");
		addProperty("act_on_select");
		addProperty("example_view", AjaxOption.SCRIPT);
		addProperty("example_group_view", AjaxOption.SCRIPT);
		addProperty("content_value_key");
		addProperty("group_visible_key");
		addProperty("group_title_key");
		addProperty("content_value_editable", "contentValueIsEditable");
		addPropertyWithDefault("accepts_first_responder", true);
		addProperty("can_reorder_content");
		addProperty("can_delete_content");

		addProperty("content_icon_key");

		// Unless the developer passes something specific, automatically enable
		// has_content_icon if either icon property is specified.
		addPropertyWithDefault("has_content_icon", propertyNamed("content_icon_key").isBound());

		// Unless the developer passes something specific, automatically enable
		// content branc if content_is_branch_property is defined.
		addProperty("content_is_branch_key");
		addPropertyWithDefault("has_content_branch", propertyNamed("content_is_branch_key").isBound());

		addProperty("content_unread_count_key");
		addProperty("content_action_key");

		addProperty(new SCProperty("groupBy", associationNamed("group"), null, AjaxOption.ARRAY, true) {
			@Override
			public String javascriptValue(Object value) {
				if (!(value instanceof NSArray)) {
					value = new NSArray(value);
				}
				return new AjaxValue(AjaxOption.ARRAY, value).javascriptValue();
			}
		});

		addProperty(new SCProperty("action", associationNamed("action"), null, AjaxOption.SCRIPT, true) {
			@Override
			public String javascriptValue(Object value) {
				return "function(ev) { return " + value + "(this, ev); }";
			}
		});
	}

	@Override
	public Set<String> cssNames(WOContext context) {
		Set<String> cssNames = super.cssNames(context);
		cssNames.add("sc-collection-view");
		return cssNames;
	}
}
