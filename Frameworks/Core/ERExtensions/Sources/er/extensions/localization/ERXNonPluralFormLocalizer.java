//
// ERXNonPluralFormLocalizer.java
// Project ERExtensions
//
// Created by tatsuya on Wed May 01 2002
//
package er.extensions.localization;

import org.apache.log4j.Logger;

/**
 * <span class="en">
 *  ERXNonPluralFormLocalizer is a subclass of {@link ERXLocalizer}.<br/>
 *  <br/>
 *  Overrides <code>plurifiedString</code> from its super class 
 *  and cancels all plural form translations includind the one provided by 
 *  <code>plurifiedStringWithTemplateForKey</code>.
 *  <br/>
 *  Good for languages that don't have plural forms (such as Japanese).
 *  </span>
 *  
 *  <span class="ja">
 *  ERXNonPluralFormLocalizerは{@link ERXLocalizer}のサブクラスである<br>
 *  <br>
 *  スーパークラスの<code>plurifiedString</code>をオーバーライドします。
 *  全ての複数形を翻訳対象から取り除きます。
 *  <code>plurifiedStringWithTemplateForKey</code>を含む
 *  <br>
 *  複数形を持っていない言語のためです。 (例、日本語)
 *  </span>
 */
public class ERXNonPluralFormLocalizer extends ERXLocalizer {
  
    static final Logger log = Logger.getLogger(ERXNonPluralFormLocalizer.class);

    public ERXNonPluralFormLocalizer(String aLanguage) { 
        super(aLanguage); 
    }
    
    /**
     * <span class="ja">
     * 複数形の文字列を戻します (スーパークラス参照) 
     * 
     * @param name - 翻訳対象キー
     * @param count - 数
     * 
     * @return ローカライズ済み文字列
     * </span>
     */
    @Override
    public String plurifiedString(String name, int count) { return name; }

    @Override
    public String toString() { return "<ERXNonPluralFormLocalizer "+language+">"; }
}
