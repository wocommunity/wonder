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

/**
 * ERCaptcha presents a captcha image to the use along with a text field
 * for the user to identify the image.
 * 
 * @binding validated returns whether or not the user properly validated the image
 * @binding resetText if set, a submit button will be added with this text that resets the image
 *  
 * @author mschrag
 */
public class ERCaptcha extends WOComponent {
	private static final Logger log = Logger.getLogger(ERCaptcha.class);
	private static ImageCaptchaService _captchaService = new DefaultManageableImageCaptchaService();
	private NSData _captcha;
	private String _response;

	public ERCaptcha(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void setCaptcha(NSData captcha) {
		_captcha = captcha;
	}

	public NSData captcha() {
		return _captcha;
	}

	public String mimeType() {
		return "image/jpeg";
	}

	public void setResponse(String response) {
		_response = response;
	}

	public String response() {
		return _response;
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (context._wasFormSubmitted()) {
			Boolean validated = Boolean.FALSE;
			try {
				validated = _captchaService.validateResponseForID(context.elementID(), _response);
			}
			catch (CaptchaServiceException e) {
				ERCaptcha.log.error("Captcha service failed.", e);
			}
			finally {
				_captcha = null;
			}
			setValueForBinding(validated, "validated");
		}
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		if (_captcha == null) {
			byte[] captchaChallengeAsJpeg = null;
			ByteArrayOutputStream captchaOutputStream = new ByteArrayOutputStream();
			try {
				BufferedImage challenge = _captchaService.getImageChallengeForID(context.elementID());
				JPEGImageEncoder captchaEncoder = JPEGCodec.createJPEGEncoder(captchaOutputStream);
				captchaEncoder.encode(challenge);
				_captcha = new NSData(captchaOutputStream.toByteArray());
			}
			catch (Throwable e) {
				log.error("Failed to create JPEG for Captcha.", e);
			}
		}
		super.appendToResponse(response, context);
	}

	public WOActionResults resetCaptcha() {
		_captcha = null;
		return null;
	}
}