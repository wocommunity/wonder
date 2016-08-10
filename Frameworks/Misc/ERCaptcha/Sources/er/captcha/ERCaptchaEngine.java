package er.captcha;

import java.awt.image.ImageFilter;

import com.jhlabs.image.WaterFilter;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FunkyBackgroundGenerator;
import com.octo.captcha.component.image.color.RandomRangeColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.BaffleTextDecorator;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

import er.extensions.foundation.ERXProperties;

/**
 * ERCaptchaEngine is the default captcha engine for ERCaptcha.
 * 
 * @author mschrag
 */
public class ERCaptchaEngine extends ListImageCaptchaEngine {

	public static String WORD_GENERATOR = "er.captcha.ERCaptchaEngine.wordgenerator";

	public ERCaptchaEngine() {
	}

	@Override
	protected void buildInitialFactories() {
		WaterFilter water = new WaterFilter();
		water.setAmplitude(1);
		water.setAntialias(true);
		water.setPhase(10);
		water.setWavelength(70);

		FontGenerator shearedFont = new RandomFontGenerator(Integer.valueOf(30), Integer.valueOf(35));
		// BackgroundGenerator back = new UniColorBackgroundGenerator(Integer.valueOf(250), Integer.valueOf(150), Color.white);
		BackgroundGenerator back = new FunkyBackgroundGenerator(Integer.valueOf(250), Integer.valueOf(150));
		RandomRangeColorGenerator randomWordColorGenerator = new RandomRangeColorGenerator(new int[] { 10, 30 }, new int[] { 10, 40 }, new int[] { 10, 12 });
		RandomRangeColorGenerator randomBaffleColorGenerator = new RandomRangeColorGenerator(new int[] { 70, 120 }, new int[] { 50, 90 }, new int[] { 90, 150 }, new int[] { 100, 200 });
		TextPaster randomPaster = new DecoratedRandomTextPaster(Integer.valueOf(6), Integer.valueOf(7), randomWordColorGenerator, new TextDecorator[] { new BaffleTextDecorator(Integer.valueOf(1), randomBaffleColorGenerator) });
		ImageDeformation backDef = new ImageDeformationByFilters(new ImageFilter[0]);
		ImageDeformation textDef = new ImageDeformationByFilters(new ImageFilter[0]);
		ImageDeformation postDef = new ImageDeformationByFilters(new ImageFilter[] { water });
		WordToImage word2image = new DeformedComposedWordToImage(shearedFont, back, randomPaster, backDef, textDef, postDef);

		WordGenerator dictionaryWords = new RandomWordGenerator(ERXProperties.stringForKeyWithDefault(WORD_GENERATOR, "BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz2346789"));
		addFactory(new GimpyFactory(dictionaryWords, word2image));
	}
}
