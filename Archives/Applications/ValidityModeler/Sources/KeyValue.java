/* KeyValue.java created by root on Sat 24-Mar-2001 */



public class KeyValue{
    public String key="";
    public String val="";

    public KeyValue(String k, String v){
        key=k;
        val = v;
    }

    public String key(){
        return key;
    }
    
    public String val(){
        return val;
    }

    public void setKey(String k){
        key = k;
    }
    
    public void setVal(String k){
        val = k;
    }
    

}
