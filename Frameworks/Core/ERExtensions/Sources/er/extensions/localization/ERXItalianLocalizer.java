//
//  ERXItalianLocalizer.java
//  ERExtensions
//
//  Created by Giorgio Valoti on 25/11/04.
//  Copyright (c) 2004. All rights reserved.
//

package er.extensions.localization;

import org.apache.log4j.Logger;

/**
 * <div class="en">
 *  ERXItalianLocalizer is a subclass of {@link ERXLocalizer}.
 *  <p>
 *  Overrides <code>plurify</code> from its super class 
 *  and tries to pluralize the string according to italian grammar rules.
 * </div>
 * 
 * <div class="ja">
 * ERXItalianLocalizer は ERXLocalizer のサブクラスです。
 * <p>
 * スーパー・クラスの <code>plurify</code> をオーバライドし、複数の文法ルールを試します。
 * </div>
 */
public class ERXItalianLocalizer extends ERXLocalizer {
  
    static final Logger log = Logger.getLogger(ERXItalianLocalizer.class);
    
    public ERXItalianLocalizer(String aLanguage) { 
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
    protected String plurify(String name, int count) {
        String result = name;
        if(name != null && count > 1) {
            if(result.matches("^.+cie$"))
                return result;
            if(result.matches("^.+[^aeiou][gc]o$")) {
                result = result.substring(0, result.length()-1)+"hi";
            }            
            result = result.substring(0, result.length()-1)+"i";
            if(result.endsWith("ii")) {
                result = result.substring(0, result.length()-1);
            }
        }
        return result;
    }
}
