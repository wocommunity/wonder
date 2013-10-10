package er.extensions.foundation;

import java.math.BigDecimal;

import er.extensions.enums.ERXMoneyEnums;

/**
 * 
 * @author ishimoto
 */
public class ERXMoney {

  //********************************************************************
  //	プロパティ
  //********************************************************************

  protected long value;
  protected ERXMoneyEnums money;

  //********************************************************************
  //	コンストラクタ
  //********************************************************************

  protected ERXMoney() {}

  public ERXMoney(final ERXMoney x) {
    value = x.value;
  }

  public ERXMoney(final BigDecimal x, ERXMoneyEnums currency) {
	  this(x.doubleValue(), currency);
  }
  
  public ERXMoney(final double x, ERXMoneyEnums currency) {
    value = Math.round(x * currency.scale());
    this.money = currency;
  }

  //********************************************************************
  //	Pseudo assignment operators
  //********************************************************************

  public ERXMoney set(final double rs) {
    value = Math.round(rs * money.scale());
    return this;
  }

  public ERXMoney set(final BigDecimal rs) {
	  value = Math.round(rs.doubleValue() * money.scale());
	  return this;
  }
  
  public ERXMoney set(final ERXMoney  rs) {
    value = rs.value;
    return this;
  }

  //********************************************************************
  //	Accessors
  //********************************************************************

  public long wholeUnits() {
    return value / money.scale();
  }

  public short cents() {
    return (short) ((value * 100) / money.scale() - (100 * wholeUnits()));
  }

  public BigDecimal asBigDecimal() {
      int centScale = log10(money.scale());
      String centString = "00000000" + cents();
	  return new BigDecimal(wholeUnits() + "." + centString.substring(centString.length() - centScale, centString.length()));
  }

  //********************************************************************
  //	Numeric utility functions
  //********************************************************************

  public ERXMoney abs() {
    ERXMoney  result = new ERXMoney(this);
    if (value < 0)
      result.value = - result.value;
    return result;
  }

  public short sign() {
    return (short) (value > 0 ?  1 : value < 0 ? -1 : 0);
  }

  //********************************************************************
  //	Relational (predicate) operations
  //********************************************************************

  public boolean equals(final ERXMoney rs) {
    return value == rs.value;
  }
  public boolean lessThan(final ERXMoney rs) {
    return value <  rs.value;
  }
  public boolean greaterThan(final ERXMoney rs) {
    return value >  rs.value;
  }

  public boolean equals(final double rs) {
    return value == rs * money.scale();
  }
  public boolean lessThan(final double rs) {
    return value < rs * money.scale();
  }
  public boolean greaterThan(final double rs) {
    return value > rs * money.scale();
  }

  //********************************************************************
  //	These three functions are to comply with Java library conventions
  //********************************************************************

  @Override
  public boolean equals(final Object rs) {
    return (rs instanceof ERXMoney) && (((ERXMoney) rs).value == value);
  }

  public int compareTo(final Object rs) {
    return sub((ERXMoney) rs).sign();
  }

  @Override
  public int hashCode() {
    return (Long.valueOf(value)).hashCode();
  }

  //********************************************************************
  //	Arithmetic operations
  //********************************************************************

  public ERXMoney addSet(final ERXMoney rs) {
    value += rs.value;
    return this;
  }
  public ERXMoney addSet(final double rs) {
    value += rs * money.scale();
    return this;
  }
  public ERXMoney addSet(final BigDecimal rs) {
	  value += rs.doubleValue() * money.scale();
	  return this;
  }
  
  public ERXMoney subSet(final ERXMoney  rs) {
    value -= rs.value;
    return this;
  }
  public ERXMoney subSet(final double rs){
    value -= rs * money.scale();
    return this;
  }
  public ERXMoney subSet(final BigDecimal rs){
	  value -= rs.doubleValue() * money.scale();
	  return this;
  }
  
  public ERXMoney mpySet(final ERXMoney rs) {
    value *= rs.value;
    return this;
  }
  public ERXMoney mpySet(final double rs) {
    value *= rs;
    return this;
  }
  public ERXMoney mpySet(final BigDecimal rs) {
	  value *= rs.doubleValue();
	  return this;
  }
  
  public ERXMoney divSet(final ERXMoney rs) {
	  value = Math.round(value / rs.value);
	  return this;
  }
  public ERXMoney divSet(final double rs) {
	  value = Math.round(value / rs);
	  return this;
  }
  public ERXMoney divSet(final BigDecimal rs) {
	  value = Math.round(value / rs.doubleValue());
	  return this;
  }

  public ERXMoney percentSet(final double rs) {
	  value = Math.round((double) value / 100 * rs);
	  return this;
  }
  public ERXMoney percentSet(final BigDecimal rs) {
	  value = Math.round((double) value / 100 * rs.doubleValue());
	  return this;
  }
  
  public ERXMoney minusSet() {
    value = - value;
    return this;
  }

  public ERXMoney add(final ERXMoney rs) {
    return new ERXMoney(this).addSet(rs);
  }
  public ERXMoney add(final double rs) {
    return new ERXMoney(this).addSet(rs);
  }

  public ERXMoney sub(final ERXMoney rs) {
    return new ERXMoney(this).subSet(rs);
  }
  public ERXMoney sub(final double rs) {
    return new ERXMoney(this).subSet(rs);
  }

  public ERXMoney mpy(final ERXMoney rs) {
    return new ERXMoney(this).mpySet(rs);
  }
  public ERXMoney mpy(final double rs) {
    return new ERXMoney(this).mpySet(rs);
  }

  public ERXMoney div(final ERXMoney rs) {
    return new ERXMoney(this).divSet(rs);
  }
  public ERXMoney div(final double rs) {
    return new ERXMoney(this).divSet(rs);
  }

  public ERXMoney minus() {
    return new ERXMoney(this).minusSet();
  }

  //********************************************************************
  //	Conversion functions
  //********************************************************************

  private static short log10(long x) {
    short result; // of decimal digits in an integer
    for (result=0; x>=10; result++, x/=10) {
    	// Decimal "shift" and count
    }
    return result;
  }

  @Override
  public String toString() {
    boolean negative = (value < 0); // Remember original sign
    value = negative ? -value : value; // Discard sign temporarily
    long whole = wholeUnits(); // Separate arg. into whole
    short cents = cents(); // and fractional monetary units
    short rest  = (short) (value - (cents + 100 * whole) * money.scale() / 100);

    String result = (negative ? "-" : ""); // Insert prefix minus, if needed
    result = result + money.prefixSymbol();  // Insert dollar sign or equiv.

    long divisors[] = { 1, 1000, 1000000, (long) 1E9, (long) 1E12,(long) 1E15, (long) 1E18 };
    int group_no  = log10(whole) / 3;
    int group_val = (int)(whole / divisors[group_no]);

    result = result + group_val; // Append leftmost 3-digits

    while (group_no > 0) { // For each remaining 3-digit group
      result = result + money.group_separator(); // Insert punctuation   
      whole -= group_val * divisors[group_no--]; // Compute new remainder
      group_val = (short) (whole/divisors[group_no]); // Get next 3-digit value
      if (group_val < 100) {
    	  result = result + "0"; // Insert embedded 0's
      }
      if (group_val <  10) {
    	  result = result + "0"; // as needed
      }
      result = result + group_val; // Append group value
    }

    // Append the fractional portion
    if(money.scale() > 1) {
      result = result + money.decimal_point(); // Append decimal point

      int centScale = log10(money.scale());
      String centString = "00000000" + cents;

      // Append cents value
      result += centString.substring(centString.length() - centScale, centString.length());
    }

    if (rest > 0) { // Test for fractional pennies    
      while ((rest *= 10) < money.scale()) {
    	  // (rest *= power(10,log10(money.scale()))) 
      }
      result = result + (rest/money.scale()); // Append fractional pennies, if any
    }
    if (negative) { // Restore original sign
      value = - value;
    }
    
    return result + money.suffixSymbol(); // Append final symbol, if any
  }
}
