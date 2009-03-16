package er.ajax.example2.helper;

public class BooleanHelper {
  public String str(Boolean value, String yes, String no) {
    if (value == null || !value.booleanValue()) {
      return no;
    }
    return yes;
  }
  
	public String yesNo(Boolean value){
	  return str(value, "yes", "no");
	}
	
}
