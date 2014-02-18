package er.directtoweb.components.misc;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * This stateless component: <p>
 *  - displays an image with a given src, width & height <br/>
 *  - allows to zoom to a given height and width.
 * 
 * <p>
 * @binding src - source url for the image to be displaed. mandatory for this component to work properly.
 * @binding height - height of the image to be displayed in the page. 
 * @binding width- width of the image to be displayed in the page.
 * @binding zoomWidth - width of the zoomed image. defaults to 200.
 * @binding zoomHeight - height of the zoomed image. defaults to 200.
 * @binding thumbnailSrc - source url for the thumbnail. if null, src binding with height and width is used.
 * @binding title - title for the image. used as tooltip in thumbnail mode [optional]
 * @binding enableZoomForImage - true/false. if true, clicking on thumbnail will popup the full size image. If false, clicking is not allowed. 
 * 
 * @author rajaram
 *
 */
public class ERDZoomableImage extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final Logger log = Logger.getLogger(ERDZoomableImage.class);
    
    public ERDZoomableImage(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return Boolean.FALSE;
    }
    
    /**
     * @return {@link String} - source of the zoomable image.
     */
    public String imageSrc() {
        return (String) valueForBinding("src");
    }
    
    /**
     * @return {@link Integer} width to zoom to (defaults to 200)
     */
    public Integer zoomWidth() {
        return ERXValueUtilities.intValueWithDefault(valueForBinding("zoomWidth"), 200);
    }
    
    /**
     * 
     * @return {@link Integer} height to zoom to (defaults to 200)
     */
    public Integer zoomHeight() {
        return ERXValueUtilities.intValueWithDefault(valueForBinding("zoomHeight"), 200);
    }
    
    /**
     * @return {@link String}  - src for the thumbnail to be displayed in the page. defaults to imageSrc, if null.
     */
    public String thumnailSrc() {
        String result = (String)valueForBinding("thumbnailSrc");
        if(ERXStringUtilities.stringIsNullOrEmpty(result)) {
            result = imageSrc();
        }
        
        return result;
    }
    
    /**
     * @return {@link Boolean} true, if {@link #thumnailSrc()} returns an empty or null string
     *                         false, otherwise
     */
    public boolean isThumbnailSrcNullOrEmpty() {
        return ERXStringUtilities.stringIsNullOrEmpty(thumnailSrc());
    }
    
    /**
     * controlled by key: enableZoomForImage
     * and if either thumbnailSrc or height or width is present (there is no point zooming without these properties passed in)
     * @return {@link Boolean} - true, if zooming is allowed. false, otherwise.
     */
    public Boolean enableZoom() {
        String thumbnailSrc = thumbnailSrcNoFallBack();
        boolean isThumbnailSrcOrHeightOrWidthPresent =  (thumbnailSrc != null && thumbnailSrc.length() >0) || valueForBinding("height") != null || valueForBinding("width") != null;
        return ERXValueUtilities.booleanValue(valueForBinding("enableZoomForImage")) && isThumbnailSrcOrHeightOrWidthPresent;
    }
    
    String thumbnailSrcNoFallBack() {
        return (String) valueForBinding("thumbnailSrc");
    }
    
    public Boolean disableZoom() {
        return !enableZoom();
    }

    public String jsToZoomImage() {
        StringBuilder sb = new StringBuilder();
        if(enableZoom()) {
            sb.append ("ZoomableImage.openImageFromURL('");
            sb.append(imageSrc()); 
            sb.append("',");
            sb.append(zoomHeight() + "," + zoomWidth());
            sb.append(");");
        }
        
        return sb.toString();
    }
    
    /**
     * @return String to use when imageSrc() turns out to be null
     */
    public String noImageString() {
        return (String) valueForBinding("noImageString");
    }
}
