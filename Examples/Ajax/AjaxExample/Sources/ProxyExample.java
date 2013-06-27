import org.apache.log4j.Logger;

import com.metaparadigm.dict.DictClient;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ProxyExample extends WOComponent {
    private Logger log=Logger.getLogger(ProxyExample.class);
    private int _counter = 0;
    private DictClient _dict;
    
    public ProxyExample(WOContext context) {
        super(context);
    }

    public int addMore(int x) {
    	log.info("ProxyExample.addMore(" +x+ ")");
        return _counter = _counter + x;
    }
    
    public int add() {
    	log.info("ProxyExample.add()");
        return ++_counter;
    }
    
    public DictClient dict() {
        if (_dict == null) {
        	//_dict = new DictClient("dict.die.net");
        	_dict = new DictClient("dict.org");
        }
        return _dict;
    }
}
