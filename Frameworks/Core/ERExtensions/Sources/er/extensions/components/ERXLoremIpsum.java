package er.extensions.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * <span class="en">
 * ERXLoremIpsum provides a component wrapper around the ERXLoremIpsumGenerator.
 * 
 * @binding type "word", "sentence", or "paragraph", defaults to <i>paragraph</i>
 * @binding count the number of words, sentences, or paragraphs to generate, defaults to <i>1</i>
 * </span>
 * 
 * <span class="ja">
 * ERXLoremIpsum は ERXLoremIpsumGenerator のコンポーネント・ラッパーです。
 * 
 * @binding type - "word", "sentence", 又は "paragraph"
 * @binding count - 生成する (ワード = words, 文 = sentences, 又は 段落 = paragraphs) 数
 * </span>
 * 
 * @author mschrag
 */
public class ERXLoremIpsum extends ERXDynamicElement {
	public ERXLoremIpsum(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String type = stringValueForBinding("type", ERXLoremIpsumGenerator.PARAGRAPH, component);
		int count = integerValueForBinding("count", 1, component);
		String loremIpsum = ERXLoremIpsumGenerator.generate(type, count).replaceAll("\n\n", "<br />");
		response.appendContentString(loremIpsum);
	}
}
