package er.extensions.components;

import java.util.StringTokenizer;

import org.apache.commons.lang3.RandomUtils;

/**
 * <div class="en">
 * Provides a generator for Lorem Ipsum text.
 * </div>
 * 
 * <div class="ja">
 * Lorem Ipsum テキストを生成します
 * </div>
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

	/**
   * <div class="en">
	 * Returns the entire Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum の全テキストを戻します。
   * </div>
   * 
	 * @return <div class="en">a string of all ten paragraphs of Lorem</div>
	 *         <div class="ja">Lorem Ipsum の全テキスト</div>
	 */
	public static String all() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _paragraphs.length; i++) {
			sb.append(_paragraphs[i]);
			sb.append("\n\n");
		}
		return sb.toString();
	}

	/**
   * <div class="en">
	 * Returns the first paragraph from Lorem Ipsum text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum の最初の段落を戻します。
   * </div>
   * 
	 * @return <div class="en">a string of the first paragraph of Lorem</div>
	 *         <div class="ja">Lorem Ipsum の最初の段落</div>
	 */
	public static String firstParagraph() {
		return _paragraphs[0];
	}

	/**
   * <div class="en">
	 * Returns the first sentence of the first paragraph from Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum の最初の段落の最初の文を戻します。
   * </div>
	 * 
   * @return "Lorem ipsum dolor sit amet, consectetuer adipiscing elit."
	 */
	public static String firstSentence() {
		return _paragraphs[0].substring(0, _paragraphs[0].indexOf('.') + 1);
	}

	/**
   * <div class="en">
	 * Returns a list of single sentences from the Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより文のリストを戻します。
   * </div>
	 * 
	 * @param size <div class="en">the number of items to insert into the list</div>
	 *             <div class="ja">リストに挿入するアイテム数</div>
	 * @return <div class="en">an array of strings of single sentences</div>
	 *         <div class="ja">文の文字列配列</div>
	 */
	public static String[] list(int size) {
		return list(size, 1);
	}

	/**
   * <div class="en">
	 * Returns a list of a given number of sentences from the Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより文のリストを戻します。
   * </div>
	 * 
	 * @param size <div class="en">the number of items to insert into the list</div>
	 *             <div class="ja">リストに挿入するアイテム数</div>
	 * @param numberOfSentences <div class="en">the number of sentences per item</div>
	 *                          <div class="ja">文の番号</div>
	 * @return <div class="en">an array of strings of sentences</div>
	 *         <div class="ja">文の文字列配列</div>
	 */
	public static String[] list(int size, int numberOfSentences) {
		String[] list = new String[size];
		for (int i = 0; i < size; i++) {
			list[i] = sentences(numberOfSentences);
		}
		return list;
	}

	/**
   * <div class="en">
	 * Returns a random paragraph of Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより段落を一つ戻します。
   * </div>
   * 
	 * @return <div class="en">a string of one random paragraph of Lorem</div>
	 *         <div class="ja">Lorem Ipsum テキストより段落</div>
	 */
	public static String paragraph() {
		return _paragraphs[RandomUtils.nextInt(0, _paragraphs.length)];
	}

	/**
   * <div class="en">
	 * Returns a particular paragraph from Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより指定段落を戻します。
   * </div>
	 * 
	 * @param numberOfParagraphs <div class="en">the paragraph number to return, should be between 0 and 9</div>
	 *                           <div class="ja">欲しい段落の番号。数値 0-9 間</div>
	 * @return <div class="en">a string of a particular paragraph of Lorem</div>
	 *         <div class="ja">Lorem Ipsum テキストより段落</div>
	 */
	public static String paragraph(int numberOfParagraphs) {
		if (numberOfParagraphs > 9 || numberOfParagraphs < 0) {
			numberOfParagraphs = 0;
		}
		return _paragraphs[numberOfParagraphs];
	}

	/**
   * <div class="en">
	 * Returns any number of random paragraphs of Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより指定個数文のランダム段落を戻します。
   * </div>
	 * 
	 * @param numberOfParagraphs <div class="en">the number of paragraphs to return</div>
	 *                           <div class="ja">指定個数文の段落</div>
	 * @return <div class="en">a string of a number of Lorem paragraphs, each seperated by a blank line.</div>
	 *         <div class="ja">Lorem Ipsum テキストより複数の段落、書く段落は空行で区切られる</div>
	 */
	public static String paragraphs(int numberOfParagraphs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfParagraphs; i++) {
			sb.append(paragraph());
			sb.append("\n\n");
		}
		return sb.toString();
	}

	/**
   * <div class="en">
	 * Returns a random sentence from the Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストよりランダム文を戻します。
   * </div>
   * 
	 * @return <div class="en">a string of a random sentence</div>
	 *         <div class="ja">Lorem Ipsum テキストよりランダム文</div>
	 */
	public static String sentence() {
		return sentence(paragraph(), RandomUtils.nextInt(0, 20) + 1);
	}

	/**
   * <div class="en">
	 * Selects a particular sentence from a particular paragraph. If the <b>sentenceNumber</b> is greater than the
	 * number of sentences in the paragraph, it "wraps" around.
   * </div>
   * 
   * <div class="ja">
   * 指定段落 paragraph より、指定の文番号 sentenceNumber の文を戻します。
   * <b>sentenceNumber</b> が段落の文数より、大きい場合は最初に戻って計算される。
   * </div>
	 * 
	 * @param paragraph <div class="en">the paragraph to pull from</div>
	 *                  <div class="ja">段落</div>
	 * @param numberOfSentences <div class="en">the sentence number to extrapolate</div>
	 *                          <div class="ja">文番号</div>
	 * @return <div class="en">a string of a particular sentence from a given paragraph</div>
	 *         <div class="ja">指定段落 paragraph より、指定の文番号 sentenceNumber の文</div>
	 */
	private static String sentence(String paragraph, int numberOfSentences) {
		String token = null;
		StringTokenizer tokenizer = new StringTokenizer(paragraph, ".");
		int goTo = numberOfSentences % tokenizer.countTokens();

		for (int i = 0; i <= goTo; i++) { // abh maybe wrong
			token = tokenizer.nextToken();
		}

		if (token == null) {
			token = firstSentence();
		}
		return token.trim() + ".";
	}

	/**
   * <div class="en">
	 * Returns a given number of random sentences from the Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより指定 numberSentences 番号の文を戻します
   * </div>
   * 
	 * @param numberOfSentences <div class="en">the number of sentences to select</div>
	 *                          <div class="ja">選択される文</div>
	 * @return <div class="en">a string of a given number of randomly chosen sentences.</div>
	 *         <div class="ja">Lorem Ipsum テキストより指定 numberSentences 番号の文</div>
	 */
	public static String sentences(int numberOfSentences) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfSentences; i++) {
			sb.append(sentence() + " ");
		}
		return sb.toString();
	}

	/**
   * <div class="en">
	 * Returns a single, randomly chosen, lowercase word from Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより小文字で始まるランダム・ワードを戻します
   * </div>
   * 
	 * @return <div class="en">a string of a single, random word</div>
	 *         <div class="ja">小文字で始まるランダム・ワード</div>
	 */
	public static String word() {
		String paragraph = paragraph();
		int start = paragraph.indexOf(' ', RandomUtils.nextInt(0, paragraph.length() - 20));

		return paragraph.substring(start, paragraph.indexOf(' ', start + 1)).replaceAll("[.,;\\s]*$", "").toLowerCase().trim();
	}

	/**
   * <div class="en">
	 * Returns a specific number of random, lowercase, space-delimited words from Lorem text.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより小文字で始まるランダム・ワードを numWords 数戻します。（スペース句切れ）
   * </div>
   * 
	 * @param numberOfWords <div class="en">the number of words to return</div>
	 *                      <div class="ja">ワード数</div>
	 * @return <div class="en">a string of space-delimited words</div>
	 *         <div class="ja">スペース句切れの数ワード</div>
	 */
	public static String words(int numberOfWords) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfWords; i++) {
			sb.append(word() + " ");
		}
		return sb.toString();
	}

	/**
   * <div class="en">
	 * Returns a number of random, lowercase, space-delimited words between <b>min</b> and <b>max</b>.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより小文字で始まるランダム・ワードを <b>min</b> と <b>max</b> 間を戻します。（スペース句切れ）
   * </div>
   * 
	 * @param min <div class="en">the minimum number of words</div>
	 *            <div class="ja">ワードの最小数</div>
	 * @param max <div class="en">the maximum number of words</div>
	 *            <div class="ja">ワードの最大数</div>
	 * @param maxLength <div class="en">the string will be truncated to this length, if longer</div>
	 *                  <div class="ja">文字列の最大長、必要な場合には切り取られ</div>
	 * @return <div class="en">a string of space-delimited, randomly chosen words where length() &lt;= maxLength</div>
	 *         <div class="ja">スペース句切れの数ワード length() &lt;= maxLength</div>
	 */
	public static String words(int min, int max, int maxLength) {
		String words = words(min, max);
		maxLength = words.length() > maxLength ? maxLength : words.length();
		return words.substring(0, maxLength);
	}
	
	/**
   * <div class="en">
	 * Returns a number of random, lowercase, space-delimited words between <b>min</b> and <b>max</b>.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストより小文字で始まるランダム・ワードを <b>min</b> と <b>max</b> 間を戻します。（スペース句切れ）
   * </div>
   * 
	 * @param min <div class="en">the minimum number of words</div>
	 *            <div class="ja">ワードの最小数</div>
	 * @param max <div class="en">the maximum number of words</div>
	 *            <div class="ja">ワードの最大数</div>
	 * @return <div class="en">a string of space-delimited, randomly chosen words</div>
	 *         <div class="ja">スペース句切れの数ワード</div>
	 */
	public static String words(int min, int max) {
		if (min < 0) {
			min = 1;
		}
		if (max < min) {
			max = min + 1;
		}
		return words(RandomUtils.nextInt(0, min) + max - min + 1);
	}

	/**
   * <div class="en">
	 * Generates Lorem text using an enumerated type.
   * </div>
   * 
   * <div class="ja">
   * Lorem Ipsum テキストを enumeratedタイプ で生成します。
   * </div>
   * 
	 * @param type <div class="en">"paragraph", "sentence", or "word"</div>
	 *             <div class="ja">"paragraph", "sentence", or "word"</div>
	 * @param count <div class="en">the number of the type to generate</div>
	 *              <div class="ja">生成する数</div>
	 * @return <div class="en">Lorem text</div>
	 *         <div class="ja">Lorem Ipsum テキスト</div>
	 */
	public static String generate(String type, int count) {
		String loremIpsum = null;
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
