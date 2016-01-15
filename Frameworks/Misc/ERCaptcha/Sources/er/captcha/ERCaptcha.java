package er.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.captchastore.FastHashMapCaptchaStore;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

import er.extensions.foundation.ERXProperties;

/**
 * <span class="en">
 * ERCaptcha presents a captcha image to the use along with a text field for the user to identify the image.
 * 
 * @binding validated returns whether or not the user properly validated the image
 * @binding resetText if set, a submit button will be added with this text that resets the image
 * 
 * @property er.captcha.captchaEngineClass the class name of the CaptchaEngine to use
 * </span>
 * 
 * <span class="ja">
 * ERCaptcha は JCaptcha ( http://jcaptcha.sourceforge.net/ ) ライブラリーの上に位置します。
 * ダイナミック・イメージ生成の為に、 AWT クラスを使用している為アプリケーションを配布時で -Djava.awt.headless=true として実行する必要があります。

 * ERCaptcha はキャプチャ・イメージと確認用のユーザが使用できるテキスト・フィールドを表現します。
 * 
 * @binding validated - ユーザがイメージを有効で認識できたかどうかを戻します。
 * @binding resetText - セットされている場合には、イメージをリセットする為のサブミット・ボタンが表示されます。
 * 
 * @property er.captcha.captchaEngineClass - 使用する CaptchaEngine のクラス名
 * </span>
 * @author mschrag
 */
public class ERCaptcha extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERCaptcha.class);
	private static ImageCaptchaService _captchaService;
	private NSData _captcha;
	private String _response;

	public static ImageCaptchaService captchaService() {
		try {
			if (_captchaService == null) {
				String captchaEngineClass = ERXProperties.stringForKeyWithDefault("er.captcha.captchaEngineClass", ERCaptchaEngine.class.getName());
				CaptchaEngine captchaEngine = (CaptchaEngine) Class.forName(captchaEngineClass).newInstance();
				_captchaService = new DefaultManageableImageCaptchaService(new FastHashMapCaptchaStore(), captchaEngine, 180, 100000, 75000);
			}
			return _captchaService;
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to initialize captcha engine.", t);
		}
	}

	public ERCaptcha(WOContext context) {
		super(context);
	}

	@Override
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

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		super.takeValuesFromRequest(request, context);
		if (context.wasFormSubmitted()) {
			Boolean validated = Boolean.FALSE;
			try {
				validated = ERCaptcha.captchaService().validateResponseForID(context.elementID(), _response);
			}
			catch (CaptchaServiceException e) {
				log.error("Captcha service failed.", e);
			}
			finally {
				_captcha = null;
			}
			setValueForBinding(validated, "validated");
		}
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (_captcha == null) {
			try {
				BufferedImage challenge = ERCaptcha.captchaService().getImageChallengeForID(context.elementID());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(challenge, "jpg", baos);
				_captcha = new NSData(baos.toByteArray());
			} catch (Throwable e) {
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
