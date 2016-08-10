//
//  ERXDutchLocalizer.java
//  ERExtensions
//
//  Created by Johan Henselmans on 08/11/09.
//  Copyright (c) 2009. All rights reserved.
//

package er.extensions.localization;

/**
 *  ERXDutchLocalizer is a subclass of {@link ERXLocalizer}.
 *  <p>
 *  Overrides <code>plurify</code> from its super class 
 *  and tries to pluralize the string according to dutch grammar rules.
 *  
 *  <pre>
 *  +en voor de meeste substantieven
 *  De bank -&gt; de banken
 *  Het boek -&gt; de boeken
 *  voorbeelden in context
 *  
 *  -s wordt –zen
 *  de buis -&gt; de buizen
 *  het huis -&gt; de huizen
 *  voorbeelden in context
 *  
 *  -f wordt –ven
 *  de korf -&gt; de korven
 *  voorbeelden in context
 *  
 *  -heid wordt –heden
 *  de grootheid -&gt; de grootheden
 *  de waarheid -&gt; de waarheden
 *  voorbeelden in context
 *  
 *  meervoud met -s
 *  
 *  +s voor substantieven met meer dan 1 lettergreep die eindigen op -e, -el, -en, -er, -em, -ie 
 *  De wafel -&gt; de wafels
 *  voorbeelden in context
 *  
 *  +s voor substantieven die eindigen op é, eau: 
 *  Het cadeau -&gt; De cadeaus
 *  Het café -&gt; de cafés
 *  
 *  +’s voor substantieven die eindigen op –a, -i, -o, -u, -y: 
 *  De paraplu -&gt; De paraplu’s
 *  voorbeelden in context
 *  
 *  +’s voor afkortingen, +'en als afkorting eindigt –s of -x: 
 *  tv -&gt; tv's
 *  GPS -&gt; GPS'en
 *  </pre>
 */
public class ERXDutchLocalizer extends ERXLocalizer {
    
    public ERXDutchLocalizer(String aLanguage) { 
        super(aLanguage);
    }
    
    @Override
    protected String plurify(String name, int count) {
        String result = name;
        if(name != null && count > 1) {
            if(result.endsWith("s")) {
                result = result.substring(0, result.length()-1)+"zen";
            } else if(result.endsWith("f")) {
                result = result.substring(0, result.length()-1)+"ven";
 			} else if(result.matches("^.....+[^e][lnrm]$")) {
                result = result.substring(0, result.length()-1)+"s";
 			} else if(result.matches("^.....+[^i][e]$")) {
                result = result.substring(0, result.length()-1)+"s";
 			} else if(result.matches("^.....+[e]$")) {
                result = result.substring(0, result.length()-1)+"s";
            } else if(result.endsWith("heid")) {
                result = result.substring(0, result.length()-2)+"den";
            } else if(result.endsWith("\u00E9")) { // here the é should be taken care of
                result = result.substring(0, result.length())+"s";
            } else if(result.endsWith("eau")) {
                result = result.substring(0, result.length())+"s";
            } else if(result.matches("^.+[aiouy]$")) {
                result = result.substring(0, result.length())+"'s";
                // these are abbreviations, but which are there?
            } else if(result.matches("^.+[v]$")) {
                result = result.substring(0, result.length())+"'s";
            } else if(result.matches("^.+[s]$")) {
                result = result.substring(0, result.length())+"'s";
                // end abbreviations
            } else {           
            	result = result.substring(0, result.length())+"en";
            }
            /*
            if(result.matches("^.+cie$"))
                return result;
            if(result.endsWith("ii")) {
                result = result.substring(0, result.length()-1);
            }
            */
        }
        return result;
    }
}
