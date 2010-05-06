package er.pdf.builder;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElementFactory;
import org.xhtmlrenderer.render.BlockBox;

import com.lowagie.text.Image;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver._private.WOURLValuedElementData;
import com.webobjects.foundation.NSData;

/**
 * ERPDFReplacedElementFactory handles replacement of dynamically generated WOImage data.
 * 
 * @author qdolan
 */
public class ERPDFReplacedElementFactory extends ITextReplacedElementFactory {

  public ERPDFReplacedElementFactory(ITextOutputDevice outputDevice) {
    super(outputDevice);
  }

  @Override
  public ReplacedElement createReplacedElement(LayoutContext context, BlockBox box, UserAgentCallback callback, int cssWidth, int cssHeight) {
    Element element = box.getElement();

    if (element.getNodeName().equals("img") 
        && element.getAttribute("src").matches(".*/wr\\?wodata=[\\-0-9]*$")) {
      String key = element.getAttribute("src").replaceFirst(".*/wr\\?wodata=(.*)", "$1");
      try {
        NSData data = cachedDataForKey(key);
        FSImage fsImage = new ITextFSImage(Image.getInstance(data.bytes()));
        if (cssWidth != -1 || cssHeight != -1) {
          fsImage.scale(cssWidth, cssHeight);
        }
        return new ITextImageElement(fsImage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return super.createReplacedElement(context, box, callback, cssWidth, cssHeight);
  }

  private NSData cachedDataForKey(String key) {
    WOResourceManager resourceManager =  WOApplication.application().resourceManager();
    WOURLValuedElementData elementData = resourceManager._cachedDataForKey(key);
    if (elementData.isTemporary()) {
      resourceManager.removeDataForKey(key, null);
    }
    return elementData.data();
  }
}
