package er.extensions;

import java.util.Random;
import java.util.StringTokenizer;

/**
 * Provides a generator for Lorem Ipsum text.
 * 
 * @author Brooks Hollar
 */
public class ERXLoremIpsumGenerator {
	public static final String PARAGRAPH = "paragraph";
	public static final String SENTENCE = "sentence";
	public static final String WORD = "word";

	private static String[] _paragraphs = { "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Morbi commodo, ipsum sed pharetra gravida, orci magna rhoncus neque, id pulvinar odio lorem non turpis. Nullam sit amet enim. Suspendisse id velit vitae ligula volutpat condimentum. Aliquam erat volutpat. Sed quis velit. Nulla facilisi. Nulla libero. Vivamus pharetra posuere sapien. Nam consectetuer. Sed aliquam, nunc eget euismod ullamcorper, lectus nunc ullamcorper orci, fermentum bibendum enim nibh eget ipsum. Donec porttitor ligula eu dolor. Maecenas vitae nulla consequat libero cursus venenatis. Nam magna enim, accumsan eu, blandit sed, blandit a, eros.",
			"Quisque facilisis erat a dui. Nam malesuada ornare dolor. Cras gravida, diam sit amet rhoncus ornare, erat elit consectetuer erat, id egestas pede nibh eget odio. Proin tincidunt, velit vel porta elementum, magna diam molestie sapien, non aliquet massa pede eu diam. Aliquam iaculis. Fusce et ipsum et nulla tristique facilisis. Donec eget sem sit amet ligula viverra gravida. Etiam vehicula urna vel turpis. Suspendisse sagittis ante a urna. Morbi a est quis orci consequat rutrum. Nullam egestas feugiat felis. Integer adipiscing semper ligula. Nunc molestie, nisl sit amet cursus convallis, sapien lectus pretium metus, vitae pretium enim wisi id lectus. Donec vestibulum. Etiam vel nibh. Nulla facilisi. Mauris pharetra. Donec augue. Fusce ultrices, neque id dignissim ultrices, tellus mauris dictum elit, vel lacinia enim metus eu nunc.",
			"Proin at eros non eros adipiscing mollis. Donec semper turpis sed diam. Sed consequat ligula nec tortor. Integer eget sem. Ut vitae enim eu est vehicula gravida. Morbi ipsum ipsum, porta nec, tempor id, auctor vitae, purus. Pellentesque neque. Nulla luctus erat vitae libero. Integer nec enim. Phasellus aliquam enim et tortor. Quisque aliquet, quam elementum condimentum feugiat, tellus odio consectetuer wisi, vel nonummy sem neque in elit. Curabitur eleifend wisi iaculis ipsum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In non velit non ligula laoreet ultrices. Praesent ultricies facilisis nisl. Vivamus luctus elit sit amet mi. Phasellus pellentesque, erat eget elementum volutpat, dolor nisl porta neque, vitae sodales ipsum nibh in ligula. Maecenas mattis pulvinar diam. Curabitur sed leo.",
			"Nulla facilisi. In vel sem. Morbi id urna in diam dignissim feugiat. Proin molestie tortor eu velit. Aliquam erat volutpat. Nullam ultrices, diam tempus vulputate egestas, eros pede varius leo, sed imperdiet lectus est ornare odio. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin consectetuer velit in dui. Phasellus wisi purus, interdum vitae, rutrum accumsan, viverra in, velit. Sed enim risus, congue non, tristique in, commodo eu, metus. Aenean tortor mi, imperdiet id, gravida eu, posuere eu, felis. Mauris sollicitudin, turpis in hendrerit sodales, lectus ipsum pellentesque ligula, sit amet scelerisque urna nibh ut arcu. Aliquam in lacus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla placerat aliquam wisi. Mauris viverra odio. Quisque fermentum pulvinar odio. Proin posuere est vitae ligula. Etiam euismod. Cras a eros.",
			"Nunc auctor bibendum eros. Maecenas porta accumsan mauris. Etiam enim enim, elementum sed, bibendum quis, rhoncus non, metus. Fusce neque dolor, adipiscing sed, consectetuer et, lacinia sit amet, quam. Suspendisse wisi quam, consectetuer in, blandit sed, suscipit eu, eros. Etiam ligula enim, tempor ut, blandit nec, mollis eu, lectus. Nam cursus. Vivamus iaculis. Aenean risus purus, pharetra in, blandit quis, gravida a, turpis. Donec nisl. Aenean eget mi. Fusce mattis est id diam. Phasellus faucibus interdum sapien. Duis quis nunc. Sed enim.",
			"Pellentesque vel dui sed orci faucibus iaculis. Suspendisse dictum magna id purus tincidunt rutrum. Nulla congue. Vivamus sit amet lorem posuere dui vulputate ornare. Phasellus mattis sollicitudin ligula. Duis dignissim felis et urna. Integer adipiscing congue metus. Nam pede. Etiam non wisi. Sed accumsan dolor ac augue. Pellentesque eget lectus. Aliquam nec dolor nec tellus ornare venenatis. Nullam blandit placerat sem. Curabitur quis ipsum. Mauris nisl tellus, aliquet eu, suscipit eu, ullamcorper quis, magna. Mauris elementum, pede at sodales vestibulum, nulla tortor congue massa, quis pellentesque odio dui id est. Cras faucibus augue.",
			"Suspendisse vestibulum dignissim quam. Integer vel augue. Phasellus nulla purus, interdum ac, venenatis non, varius rutrum, leo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Duis a eros. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos. Fusce magna mi, porttitor quis, convallis eget, sodales ac, urna. Phasellus luctus venenatis magna. Vivamus eget lacus. Nunc tincidunt convallis tortor. Duis eros mi, dictum vel, fringilla sit amet, fermentum id, sem. Phasellus nunc enim, faucibus ut, laoreet in, consequat id, metus. Vivamus dignissim. Cras lobortis tempor velit. Phasellus nec diam ac nisl lacinia tristique. Nullam nec metus id mi dictum dignissim. Nullam quis wisi non sem lobortis condimentum. Phasellus pulvinar, nulla non aliquam eleifend, tortor wisi scelerisque felis, in sollicitudin arcu ante lacinia leo.",
			"Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar facilisis. Ut felis.",
			"Cras sed ante. Phasellus in massa. Curabitur dolor eros, gravida et, hendrerit ac, cursus non, massa. Aliquam lorem. In hac habitasse platea dictumst. Cras eu mauris. Quisque lacus. Donec ipsum. Nullam vitae sem at nunc pharetra ultricies. Vivamus elit eros, ullamcorper a, adipiscing sit amet, porttitor ut, nibh. Maecenas adipiscing mollis massa. Nunc ut dui eget nulla venenatis aliquet. Sed luctus posuere justo. Cras vehicula varius turpis. Vivamus eros metus, tristique sit amet, molestie dignissim, malesuada et, urna.", "Cras dictum. Maecenas ut turpis. In vitae erat ac orci dignissim eleifend. Nunc quis justo. Sed vel ipsum in purus tincidunt pharetra. Sed pulvinar, felis id consectetuer malesuada, enim nisl mattis elit, a facilisis tortor nibh quis leo. Sed augue lacus, pretium vitae, molestie eget, rhoncus quis, elit. Donec in augue. Fusce orci wisi, ornare id, mollis vel, lacinia vel, massa." };

	private static Random _random = new Random(System.currentTimeMillis());

	/**
	 * Returns the entire Lorem text.
	 * 
	 * @return a string of all ten paragraphs of Lorem
	 */
	public static String all() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < _paragraphs.length; i++) {
			buff.append(_paragraphs[i]);
			buff.append("\n\n");
		}
		return buff.toString();
	}

	/**
	 * Returns the first paragram from Lorem Ipsum text.
	 * 
	 * @return a string of the first paragraph of Lorem
	 */
	public static String firstParagraph() {
		return _paragraphs[0];
	}

	/**
	 * Returns the first sentence of the first paragraph from Lorem text.
	 * 
	 * @return the string "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
	 */
	public static String firstSentence() {
		return _paragraphs[0].substring(0, _paragraphs[0].indexOf('.') + 1);
	}

	/**
	 * Returns a list of single sentences from the Lorem text.
	 * 
	 * @param size
	 *            the number of items to insert into the list
	 * @return an array of strings of single sentences
	 */
	public static String[] list(int size) {
		return list(size, 1);
	}

	/**
	 * Returns a list of a given number of sentences from the Lorem text.
	 * 
	 * @param size
	 *            the number of items to insert into the list
	 * @return an array of strings of sentences
	 */
	public static String[] list(int size, int numberOfSentences) {
		String[] list = new String[size];
		for (int ii = 0; ii < size; ii++) {
			list[ii] = sentences(numberOfSentences);
		}
		return list;
	}

	/**
	 * Returns a random paragraph of Lorem text.
	 * 
	 * @return a string of one random paragraph of Lorem
	 */
	public static String paragraph() {
		return _paragraphs[_random.nextInt(_paragraphs.length)];
	}

	/**
	 * Returns a particular paragraph from Lorem text.
	 * 
	 * @param number
	 *            the paragraph number to return, should be between 0 and 9
	 * @return a string of a particular Lorem paragraph
	 */
	public static String paragraph(int number) {
		if (number > 9 || number < 0) {
			number = 0;
		}
		return _paragraphs[number];
	}

	/**
	 * Returns any number of random paragraphs of Lorem text.
	 * 
	 * @param numParagraphs
	 *            the number of paragraphs to return
	 * @return a string of a number of Lorem paragraphs, each seperated by a blank line.
	 */
	public static String paragraphs(int numParagraphs) {
		StringBuffer buff = new StringBuffer();
		for (int ii = 0; ii < numParagraphs; ii++) {
			buff.append(_paragraphs[_random.nextInt(_paragraphs.length)]);
			buff.append("\n\n");
		}
		return buff.toString();
	}

	/**
	 * Returns a random sentence from the Lorem text.
	 * 
	 * @return a string of a random sentence
	 */
	public static String randomSentence() {
		return sentence(paragraph(), _random.nextInt(20) + 1);
	}

	/**
	 * Selects a particular sentence from a particular paragraph. If the <b>sentenceNumber</b> is greater than the
	 * number of sentences in the paragraph, it "wraps" around.
	 * 
	 * @param paragraph
	 *            the paragraph to pull from
	 * @param sentenceNumber
	 *            the sentence number to extrapolate
	 * @return a string of a particular sentence from a given paragraph
	 */
	private static String sentence(String paragraph, int sentenceNumber) {
		String token = null;
		StringTokenizer tokenizer = new StringTokenizer(paragraph, ".");
		int goTo = sentenceNumber % tokenizer.countTokens();

		for (int ii = 0; ii <= goTo; ii++) { // abh maybe wrong
			token = tokenizer.nextToken();
		}

		if (token == null) {
			token = firstSentence();
		}
		return token.trim() + ".";
	}

	/**
	 * Returns a given number of random sentences from the Lorem text.
	 * 
	 * @param numberSentences
	 *            the number of sentences to select
	 * @return a string of a given number of randomly choosen sentences.
	 */
	public static String sentences(int numberSentences) {
		StringBuffer buff = new StringBuffer();
		for (int ii = 0; ii < numberSentences; ii++) {
			buff.append(randomSentence() + "  ");
		}
		return buff.toString();
	}

	/**
	 * Returns a single, randomly choosen, lowercase word from Lorem text.
	 * 
	 * @return a string of a single, random word
	 */
	public static String word() {
		String para = paragraph();
		int start = _random.nextInt(para.length() - 20);
		start = para.indexOf(' ', start);

		return para.substring(start, para.indexOf(' ', start + 1)).replaceAll("[.,;\\s]*$", "").toLowerCase().trim();
	}

	/**
	 * Returns a specific number of random, lowercase, space-delimited words from Lorem text.
	 * 
	 * @param numWords
	 *            the number of words to return
	 * @return a string of space-delimited words
	 */
	public static String words(int numWords) {
		StringBuffer buff = new StringBuffer();
		for (int ii = 0; ii < numWords; ii++) {
			buff.append(word() + " ");
		}
		return buff.toString();
	}

	/**
	 * Returns a number of random, lowercase, space-delimited words between <b>min</b> and <b>max</b>.
	 * 
	 * @param min
	 *            the minimum number of words
	 * @param max
	 *            the maximum nunmber of words
	 * @param maxLength
	 * 			  the string will be truncated to this length, if longer
	 * @return a string of space-delimited, randomly choosen words where length() <= maxLength
	 */
	public static String words(int min, int max, int maxLength) {
		if (min < 0) {
			min = 1;
		}
		if (max < min) {
			max = min + 1;
		}
		String foo = words(_random.nextInt(min) + max - min + 1);
		int cutAt = foo.length() > maxLength ? maxLength : foo.length();
		return foo.substring(0, cutAt);
	}
	
	/**
	 * Returns a number of random, lowercase, space-delimited words between <b>min</b> and <b>max</b>.
	 * 
	 * @param min
	 *            the minimum number of words
	 * @param max
	 *            the maximum nunmber of words
	 * @return a string of space-delimited, randomly choosen words
	 */
	public static String words(int min, int max) {
		if (min < 0) {
			min = 1;
		}
		if (max < min) {
			max = min + 1;
		}
		return words(_random.nextInt(min) + max - min + 1);
	}


	/**
	 * Generates lorem ipsum text using an enumerated type.
	 * 
	 * @param type
	 *            "paragraph", "sentence", or "word"
	 * @param count
	 *            the number of the type to generate
	 * @return lorem ipsum text
	 */
	public static String generate(String type, int count) {
		String loremIpsum;
		if (ERXLoremIpsumGenerator.PARAGRAPH.equals(type)) {
			loremIpsum = ERXLoremIpsumGenerator.paragraphs(count);
		}
		else if (ERXLoremIpsumGenerator.SENTENCE.equals(type)) {
			loremIpsum = ERXLoremIpsumGenerator.sentences(count);
		}
		else if (ERXLoremIpsumGenerator.WORD.equals(type)) {
			loremIpsum = ERXLoremIpsumGenerator.words(count);
		}
		else {
			throw new IllegalArgumentException("Type must one either 'paragraph', 'sentence', or 'word'.");
		}
		return loremIpsum;
	}
}
