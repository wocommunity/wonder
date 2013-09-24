package er.extensions.enums;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.formatters.ERXDecimalFormatSymbols;

/**
 * http://en.wikipedia.org/wiki/ISO_4217
 * 
 * er.extensions.enums.ERXMoneyEnums
 */
public enum ERXMoneyEnums {

  BAM("marka", "fening", "KM", "", ",", ".", 100), /* Bosnia-Herzegowina */
  BDT("taka", "poisha", "৳", "", ",", ".", 100), /* Bangladeshi taka */
  BGN("lev", "stotinki", "лв", "", ",", ".", 100), /* Bulgaria */
  CAD("dollar", "cent", "＄", "", ".", ",", 100), /* Canada */
  CZK("koruna", "haléř", "Kč", "", ",", ".", 100), /* Czech Republic */
  DKK("krone", "øre", "kr", "", ",", ".", 100), /* Denmark */
  EUR("euro", "cent", "€", "", ",", ".", 100), /* Europian Union */
  GBP("pound", "penny", "£", "", ",", ".", 100), /* Great Britain */
  HKD("dollar", "cent", "$", "", ",", ".", 100), /* Hong Kong dollar */
  HRK("kuna", "lipa", "kn", "", ",", ".", 100), /* Croatia */
  HUF("forint", "fillér", "Ft", "", ",", ".", 100), /* Hungary */
  INR("rupee", "paisa", "₹", "", ",", ".", 100), /* Indian rupee */
  KRW("won", "", "₩", "", ",", ".", 1), /* South Korean won */
  LTL("litas", "centas", "Lt", "", ",", ".", 100), /* Republic of Lithuania */
  LVL("lats", "santīms", "Ls", "s", ",", ".", 100), /* Republic of Latvia */
  MYR("ringgit", "sen", "RM", "", ",", ".", 100), /* Malaysian ringgit */
  PHP("peso", "sentimo", "₱", "", ",", ".", 100), /* Philippine peso */
  PLN("złoty", "grosz", "zł", "", ",", ".", 100), /* Poland */
  RON("lei", "bani", "", "", ",", ".", 100), /* Rumania */
  RSD("dinar", "para", "РСД", "", ",", ".", 100), /* Serbia */
  RUB("ruble", "kopek", "руб", "", ",", ".", 100), /* Russia */
  SDG("pound", "qirush", "", "", ",", ".", 100), /* Sudan */
  SGD("dollar", "cent", "S$", "", ",", ".", 100), /* Singapore dollar */
  SYP("pound", "piastre", "", "", ",", ".", 100), /* Syrian Arab Republic */
  TWD("dollars", "cents", "＄", "", ".", ",", 100), /* New Taiwan dollar */
  UAH("hryvnia", "kopiyka", "₴", "", ",", ".", 100), /* Ukraine */
  USD("dollar", "cent", "＄", "", ".", ",", 100), /* USA */
  VND("dong", "hào", "₫", "", ",", ".", 100), /* Vietnamese dong */
  YEN("yen", "", "￥", "", ".", ",", 1); /* Japan */

  //********************************************************************
  //  Constructor
  //********************************************************************

  ERXMoneyEnums(
      String unitName,
      String centName, 
      String prefixSymbol, 
      String suffixSymbol, 
      String decimal_point, 
      String group_separator, 
      int scale) {
    this.unitName = unitName;
    this.centName = centName;
    this.prefixSymbol = prefixSymbol;
    this.suffixSymbol = suffixSymbol;
    this.decimal_point = decimal_point;
    this.group_separator = group_separator;
    this.scale = scale;
    formatter = formatterCreator();
    simpleFormatter = simpleFormatterCreator();
  } 

  /** Full Name for Localize */
  public String fullName() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName());
    sb.append(NSKeyValueCodingAdditions.KeyPathSeparator);
    sb.append(name());
    return sb.toString();
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String unitName() {
    return unitName;
  }
  private final String unitName;

  public String centName() {
    return centName;
  }
  private final String centName;

  public String prefixSymbol() {
    return prefixSymbol;
  }
  private final String prefixSymbol;

  public String suffixSymbol() {
    return suffixSymbol;
  }
  private final String suffixSymbol;

  public String decimal_point() {
    return decimal_point;
  }
  private final String decimal_point; 

  public String group_separator() {
    return group_separator;
  }
  private final String group_separator;

  public int scale() {
    return scale;
  }
  private final int scale;

  public DecimalFormat formatter() {
    return formatter;
  }
  private final DecimalFormat formatter;

  public DecimalFormat simpleFormatter() {
    return simpleFormatter;
  }
  private final DecimalFormat simpleFormatter;

  //********************************************************************
  //  Private Classes
  //********************************************************************

  private DecimalFormat simpleFormatterCreator() {
    String fms = creator();
    DecimalFormat formater = new DecimalFormat(fms);  

    DecimalFormatSymbols dfs = ERXDecimalFormatSymbols.decimalFormatSymbols(decimal_point());
    formater.setDecimalFormatSymbols(dfs);

    int i = log10(scale());
    formater.setMinimumFractionDigits(i);
    formater.setMaximumFractionDigits(i);

    return formater;
  }

  private DecimalFormat formatterCreator() {
    String fms = creator();
    DecimalFormat formater = new DecimalFormat(prefixSymbol() + fms + suffixSymbol());  

    DecimalFormatSymbols dfs = ERXDecimalFormatSymbols.decimalFormatSymbols(decimal_point());
    formater.setDecimalFormatSymbols(dfs);

    int i = log10(scale());
    formater.setMinimumFractionDigits(i);
    formater.setMaximumFractionDigits(i);

    return formater;
  }

  private String creator() {
    long whole = 99999999999990l;
    long divisors[] = { 1, 1000, 1000000, (long)1E9, (long)1E12,(long)1E15, (long)1E18};
    int group_no  = log10(whole) / 3;
    int group_val = (int)(whole / divisors[group_no]);

    String fms = "" + group_val; // Append leftmost 3-digits
    while (group_no > 0) { // For each remaining 3-digit group
      fms = fms + ","; // Insert punctuation   
      whole -= group_val * divisors[group_no--]; // Compute new remainder
      group_val = (short)(whole/divisors[group_no]); // Get next 3-digit value
      if (group_val < 100)
        fms = fms + "0"; // Insert embedded 0's
      if (group_val <  10)
        fms = fms + "0"; //   as needed
      fms = fms + group_val;  // Append group value
    }
    return fms.replace("9", "#");
  }

  private static short log10(long x) {
    short result; // of decimal digits in an integer
    for (result=0; x>=10; result++, x/=10); // Decimal "shift" and count
    return result;
  }
}
