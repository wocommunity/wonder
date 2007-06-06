package er.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

import er.extensions.ERXStatelessComponent;

/**
 * ERCaptcha presents a captcha image to the use along with a text field
 * for the user to identify the image.
 * 
 * @binding validated returns whether or not the user properly validated the image
 * @binding resetText if set, a submit button will be added with this text that resets the image
 *  
 * @author mschrag
 */
public class ERCaptcha extends ERXStatelessComponent {
	
	private static final Logger log = Logger.getLogger(ERCaptcha.class);
	private static ImageCaptchaService _captchaService = new DefaultManageableImageCaptchaService();

	public ERCaptcha(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void setCaptcha(NSData captcha) {
		if(captcha == null) {
			session().removeObjectForKey("ERCaptcha.captcha");
		} else {
			session().setObjectForKey(captcha, "ERCaptcha.captcha");
		}
	}

	public NSData captcha() {
		return (NSData) session().objectForKey("ERCaptcha.captcha");
	}

	public String mimeType() {
		return "image/jpeg";
	}

	public void setResponse(String response) {
		if(response == null) {
			session().removeObjectForKey("ERCaptcha.response");
		} else {
			session().setObjectForKey(response, "ERCaptcha.response");
		}
	}

	public String response() {
		return (String) session().objectForKey("ERCaptcha.response");
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (context._wasFormSubmitted()) {
			Boolean validated = Boolean.FALSE;
			try {
				validated = _captchaService.validateResponseForID(context.elementID(), response());
			}
			catch (CaptchaServiceException e) {
				e.printStackTrace();
			}
			finally {
				setCaptcha(null);
			}
			setValueForBinding(validated, "validated");
		}
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		if (captcha() == null) {
			byte[] captchaChallengeAsJpeg = null;
			ByteArrayOutputStream captchaOutputStream = new ByteArrayOutputStream();
			try {
				BufferedImage challenge = _captchaService.getImageChallengeForID(context.elementID());
				JPEGImageEncoder captchaEncoder = JPEGCodec.createJPEGEncoder(captchaOutputStream);
				captchaEncoder.encode(challenge);
				setCaptcha(new NSData(captchaOutputStream.toByteArray()));
			}
			catch (Throwable e) {
				log.error("Failed to create JPEG for Captcha.", e);
			}
		}
		super.appendToResponse(response, context);
	}

	public WOActionResults resetCaptcha() {
		setCaptcha(null);
		return null;
	}
}