package er.woadaptor;
import com.webobjects.appserver.WONettyAdaptor;
import com.webobjects.foundation.NSDictionary;

/**
 * Wrapper class to use er.woadaptor.ERWOAdaptor as a convenience over com.webobjects.appserver.WONettyAdaptor
 * 
 * @author anjo (Original) Apache Mina version
 * @author ravim JBoss Netty version
 */
public class ERWOAdaptor extends WONettyAdaptor {
	public ERWOAdaptor(String name, NSDictionary config) {
		super(name, config);
	}
}
