package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Simple podcast RSS feed provider. The actual item goes into the component content. Based on ERXRssPage.
 * Some of the attributes are iTunes specific (see http://www.apple.com/itunes/podcasts/specs.html)
 * 
 * @binding feedTitle the title of the RSS feed
 * @binding feedUrl the URL of the website associated with the RSS feed
 * @binding feedLanguage two-letter language code, ISO 639-1 Alpha-2 format (eg : en-us, fr-ca, etc.)
 * @binding feedCopyright copyright notice
 * @binding feedDescription the description of the RSS feed
 * @binding feedSubtitle the contents of this tag are shown in the Description column in iTunes
 * @binding feedAuthor the content of this tag is shown in the Artist column in iTunes
 * @binding feedOwnerName this tag contains information that will be used to contact the owner of the podcast for communication specifically about their podcast
 * @binding feedOwnerEmail this tag contains information that will be used to contact the owner of the podcast for communication specifically about their podcast
 * @binding feedImageUrl this tag specifies the artwork for your podcast. Put the URL to the image in the href attribute. 
 * @binding feedCategory use a top level &lt;itunes:category&gt; to specify the browse category
 * @binding list the list of items to show in the feed
 * @binding item the repetition item binding for the feed items
 * @binding itemGuid the GUID of the current item
 * @binding itemTitle the title of the current item
 * @binding itemLink the link associated with the current item
 * @binding itemPubDate the publish date of the current item
 * @binding itemAuthor the author of the item
 * @binding itemSubtitle the contents of this tag are shown in the Description column in iTunes
 * @binding itemEncloseUrl url to the podcast
 * @binding itemEnclosureLength length, in bytes, of the podcast
 * @binding itemEnclosureMimeType MIME type of the podcast
 * 
 * @author Pascal Robert
 * @author ak (original author of ERXRssPage)
 */
public class ERXPodcastRssPage extends ERXRssPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXPodcastRssPage(WOContext context) {
        super(context);
    }
    
}