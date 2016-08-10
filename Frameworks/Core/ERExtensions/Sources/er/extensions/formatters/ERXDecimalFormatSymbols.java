package er.extensions.formatters;

import java.text.DecimalFormatSymbols;

import er.extensions.foundation.ERXStringUtilities;

public class ERXDecimalFormatSymbols {

  //********************************************************************
  //  Properties
  //********************************************************************

  public static final char MONEY_SEPERATOR_COMMA = ',';
  public static final char MONEY_SEPERATOR_DOT = '.';

  //********************************************************************
  //  メソッド
  //********************************************************************

  public static DecimalFormatSymbols decimalCommaSymbol() {
    if(decimalCommaSymbol == null) {
      decimalCommaSymbol = new DecimalFormatSymbols();
      decimalCommaSymbol.setDecimalSeparator(MONEY_SEPERATOR_COMMA);
      decimalCommaSymbol.setGroupingSeparator(MONEY_SEPERATOR_DOT);
    }
    return decimalCommaSymbol;
  }
  private static DecimalFormatSymbols decimalCommaSymbol = null;

  public static DecimalFormatSymbols decimalDotSymbol() {
    if(decimalDotSymbol == null) {
      decimalDotSymbol = new DecimalFormatSymbols();
      decimalDotSymbol.setDecimalSeparator(MONEY_SEPERATOR_DOT);      
      decimalDotSymbol.setGroupingSeparator(MONEY_SEPERATOR_COMMA);     
    }
    return decimalDotSymbol;
  }
  private static DecimalFormatSymbols decimalDotSymbol = null;

  public static DecimalFormatSymbols decimalFormatSymbols(char c) {   
    return (ERXDecimalFormatSymbols.MONEY_SEPERATOR_COMMA == c) ? ERXDecimalFormatSymbols.decimalCommaSymbol() : ERXDecimalFormatSymbols.decimalDotSymbol();
  }

  public static DecimalFormatSymbols decimalFormatSymbols(String s) {   
    return ERXStringUtilities.stringIsNullOrEmpty(s) ? ERXDecimalFormatSymbols.decimalDotSymbol() : ERXDecimalFormatSymbols.decimalFormatSymbols(s.charAt(0));
  }
}
