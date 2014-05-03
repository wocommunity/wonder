package er.chronic.numerizer;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Numerizer {
  protected static class DirectNum {
    private Pattern _name;
    private String _number;
    
    public DirectNum(String name, String number) {
      _name = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
      _number = number;
    }
    
    public Pattern getName() {
      return _name;
    }
    
    public String getNumber() {
      return _number;
    }
  }
  
  protected static class Prefix {
    private String _name;
    private Pattern _pattern;
    private long _number;
    
    public Prefix(String name, Pattern pattern, long number) {
      _name = name;
      _pattern = pattern;
      _number = number;
    }
    
    public String getName() {
      return _name;
    }
    
    public Pattern getPattern() {
      return _pattern;
    }
    
    public long getNumber() {
      return _number;
    }
  }

  protected static class TenPrefix extends Prefix {
    public TenPrefix(String name, long number) {
      super(name, Pattern.compile("(?:" + name + ") *<num>(\\d(?=\\D|$))*", Pattern.CASE_INSENSITIVE), number);
    }
  }

  protected static class BigPrefix extends Prefix {
    public BigPrefix(String name, long number) {
      super(name, Pattern.compile("(?:<num>)?(\\d*) *" + name, Pattern.CASE_INSENSITIVE), number);
    }
  }
  
  protected static DirectNum[] DIRECT_NUMS;
  protected static TenPrefix[] TEN_PREFIXES;
  protected static BigPrefix[] BIG_PREFIXES;
   
  static {
    List<DirectNum> directNums = new LinkedList<DirectNum>();
    directNums.add(new DirectNum("eleven", "11"));
    directNums.add(new DirectNum("twelve", "12"));
    directNums.add(new DirectNum("thirteen", "13"));
    directNums.add(new DirectNum("fourteen", "14"));
    directNums.add(new DirectNum("fifteen", "15"));
    directNums.add(new DirectNum("sixteen", "16"));
    directNums.add(new DirectNum("seventeen", "17"));
    directNums.add(new DirectNum("eighteen", "18"));
    directNums.add(new DirectNum("nineteen", "19"));
    directNums.add(new DirectNum("ninteen", "19")); // Common mis-spelling
    directNums.add(new DirectNum("zero", "0"));
    directNums.add(new DirectNum("one", "1"));
    directNums.add(new DirectNum("two", "2"));
    directNums.add(new DirectNum("three", "3"));
    directNums.add(new DirectNum("four(\\W|$)", "4$1")); // The weird regex is so that it matches four but not fourty
    directNums.add(new DirectNum("five", "5"));
    directNums.add(new DirectNum("six(\\W|$)", "6$1"));
    directNums.add(new DirectNum("seven(\\W|$)", "7$1"));
    directNums.add(new DirectNum("eight(\\W|$)", "8$1"));
    directNums.add(new DirectNum("nine(\\W|$)", "9$1"));
    directNums.add(new DirectNum("ten", "10"));
    directNums.add(new DirectNum("\\ba\\b(.)", "1$1"));
    Numerizer.DIRECT_NUMS = directNums.toArray(new DirectNum[directNums.size()]);
    
    List<TenPrefix> tenPrefixes = new LinkedList<TenPrefix>();
    tenPrefixes.add(new TenPrefix("twenty", 20));
    tenPrefixes.add(new TenPrefix("thirty", 30));
    tenPrefixes.add(new TenPrefix("fourty", 40)); // Common mis-spelling
    tenPrefixes.add(new TenPrefix("forty", 40));
    tenPrefixes.add(new TenPrefix("fifty", 50));
    tenPrefixes.add(new TenPrefix("sixty", 60));
    tenPrefixes.add(new TenPrefix("seventy", 70));
    tenPrefixes.add(new TenPrefix("eighty", 80));
    tenPrefixes.add(new TenPrefix("ninety", 90));
    tenPrefixes.add(new TenPrefix("ninty", 90)); // Common mis-spelling
    Numerizer.TEN_PREFIXES = tenPrefixes.toArray(new TenPrefix[tenPrefixes.size()]);
    
    List<BigPrefix> bigPrefixes = new LinkedList<BigPrefix>();
    bigPrefixes.add(new BigPrefix("hundred", 100L));
    bigPrefixes.add(new BigPrefix("thousand", 1000L));
    bigPrefixes.add(new BigPrefix("million", 1000000L));
    bigPrefixes.add(new BigPrefix("billion", 1000000000L));
    bigPrefixes.add(new BigPrefix("trillion", 1000000000000L));
    Numerizer.BIG_PREFIXES = bigPrefixes.toArray(new BigPrefix[bigPrefixes.size()]);
  }

  private static final Pattern DEHYPHENATOR = Pattern.compile(" +|(\\D)-(\\D)");
  private static final Pattern DEHALFER = Pattern.compile("a half", Pattern.CASE_INSENSITIVE);
  private static final Pattern DEHAALFER = Pattern.compile("(\\d+)(?: | and |-)*haAlf", Pattern.CASE_INSENSITIVE);
  private static final Pattern ANDITION_PATTERN = Pattern.compile("<num>(\\d+)( | and )<num>(\\d+)(?=\\W|$)", Pattern.CASE_INSENSITIVE);
  
  // FIXES
  //string.gsub!(/ +|([^\d])-([^d])/, '\1 \2') # will mutilate hyphenated-words but shouldn't matter for date extraction
  //string.gsub!(/ +|([^\d])-([^\\d])/, '\1 \2') # will mutilate hyphenated-words but shouldn't matter for date extraction
  
  public static String numerize(String str) {
    String numerizedStr = str;
    
    // preprocess
    numerizedStr = Numerizer.DEHYPHENATOR.matcher(numerizedStr).replaceAll("$1 $2"); // will mutilate hyphenated-words but shouldn't matter for date extraction
    numerizedStr = Numerizer.DEHALFER.matcher(numerizedStr).replaceAll("haAlf"); // take the 'a' out so it doesn't turn into a 1, save the half for the end

    // easy/direct replacements
    for (DirectNum dn : Numerizer.DIRECT_NUMS) {
      numerizedStr = dn.getName().matcher(numerizedStr).replaceAll("<num>" + dn.getNumber());
    }

    // ten, twenty, etc.
    for (Prefix tp : Numerizer.TEN_PREFIXES) {
      Matcher matcher = tp.getPattern().matcher(numerizedStr);
      if (matcher.find()) {
        StringBuffer matcherBuffer = new StringBuffer();
        do {
          if (matcher.group(1) == null) {
            matcher.appendReplacement(matcherBuffer, "<num>" + String.valueOf(tp.getNumber()));
          }
          else {
            matcher.appendReplacement(matcherBuffer, "<num>" + String.valueOf(tp.getNumber() + Long.parseLong(matcher.group(1).trim())));
          }
        } while (matcher.find());
        matcher.appendTail(matcherBuffer);
        numerizedStr = matcherBuffer.toString();
      }
    }
    
    for (Prefix tp : Numerizer.TEN_PREFIXES) {
      numerizedStr = Pattern.compile(tp.getName(), Pattern.CASE_INSENSITIVE).matcher(numerizedStr).replaceAll("<num>" + tp.getNumber());
    }

    // hundreds, thousands, millions, etc.
    for (Prefix bp : Numerizer.BIG_PREFIXES) {
      Matcher matcher = bp.getPattern().matcher(numerizedStr);
      if (matcher.find()) {
        StringBuffer matcherBuffer = new StringBuffer();
        do {
          if (matcher.group(1) == null) {
            matcher.appendReplacement(matcherBuffer, "<num>" + String.valueOf(bp.getNumber()));
          }
          else {
            matcher.appendReplacement(matcherBuffer, "<num>" + String.valueOf(bp.getNumber() * Long.parseLong(matcher.group(1).trim())));
          }
        } while (matcher.find());
        matcher.appendTail(matcherBuffer);
        numerizedStr = matcherBuffer.toString();
      }
      numerizedStr = Numerizer.andition(numerizedStr);
      // combine_numbers(string) // Should to be more efficient way to do this
    }

    // fractional addition
    // I'm not combining this with the previous block as using float addition complicates the strings
    // (with extraneous .0's and such )
    Matcher matcher = Numerizer.DEHAALFER.matcher(numerizedStr);
    if (matcher.find()) {
      StringBuffer matcherBuffer = new StringBuffer();
      do {
        matcher.appendReplacement(matcherBuffer, String.valueOf(Float.parseFloat(matcher.group(1).trim()) + 0.5f));
      } while (matcher.find());
      matcher.appendTail(matcherBuffer);
      numerizedStr = matcherBuffer.toString();
    }
    //string.gsub!(/(\d+)(?: | and |-)*haAlf/i) { ($1.to_f + 0.5).to_s }

    numerizedStr = numerizedStr.replaceAll("<num>", "");
    return numerizedStr;
  }

  public static String andition(String str) {
    StringBuilder anditionStr = new StringBuilder(str);
    Matcher matcher = Numerizer.ANDITION_PATTERN.matcher(anditionStr);
    while (matcher.find()) {
      if (matcher.group(2).equalsIgnoreCase(" and ") || (matcher.group(1).length() > matcher.group(3).length() && matcher.group(1).matches("^.+0+$"))) {
        anditionStr.replace(matcher.start(), matcher.end(), "<num>" + String.valueOf(Integer.parseInt(matcher.group(1).trim()) + Integer.parseInt(matcher.group(3).trim())));
        matcher = Numerizer.ANDITION_PATTERN.matcher(anditionStr);
      }
    }
    return anditionStr.toString(); 
  }
}
