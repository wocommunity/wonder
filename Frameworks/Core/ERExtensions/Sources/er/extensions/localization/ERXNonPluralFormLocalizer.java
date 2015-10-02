//
// ERXNonPluralFormLocalizer.java
// Project ERExtensions
//
// Created by tatsuya on Wed May 01 2002
//
package er.extensions.localization;

import org.apache.log4j.Logger;

/**
 * <div class="en">
 *  ERXNonPluralFormLocalizer is a subclass of {@link ERXLocalizer}.
 *  <p>
 *  Overrides <code>plurifiedString</code> from its super class 
 *  and cancels all plural form translations including the one provided by 
 *  <code>plurifiedStringWithTemplateForKey</code>.
 *  <p>
 *  Good for languages that don't have plural forms (such as Japanese).
 *  </div>
 *  
 *  <div class="ja">
 *  ERXNonPluralFormLocalizerは{@link ERXLocalizer}のサブクラスである
 *  <p>
 *  スーパークラスの<code>plurifiedString</code>をオーバーライドします。
 *  全ての複数形を翻訳対象から取り除きます。
 *  <code>plurifiedStringWithTemplateForKey</code>を含む
 *  <p>
 *  複数形を持っていない言語のためです。 (例、日本語)
 *  </div>
 */
public class ERXNonPluralFormLocalizer extends ERXLocalizer {
  
    static final Logger log = Logger.getLogger(ERXNonPluralFormLocalizer.class);

    public ERXNonPluralFormLocalizer(String aLanguage) { 
        super(aLanguage); 
    }
    
    /**
     * <div class="ja">
     * 複数形の文字列を戻します (スーパークラス参照) 
     * 
     * @param name - 翻訳対象キー
     * @param count - 数
     * 
     * @return ローカライズ済み文字列
     * </div>
     */
    @Override
    public String plurifiedString(String name, int count) { return name; }

    @Override
    public String toString() { return "<ERXNonPluralFormLocalizer "+language+">"; }
}
