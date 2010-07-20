package er.woadaptor;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.webobjects.appserver.WOAdaptor;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSDictionary;

/**
 * @author anjo (Original) Apache Mina version
 * @author ravim ERWOAdaptor version
 */
public class ERWOAdaptor extends WOAdaptor {

    private static final Logger log = Logger.getLogger(ERWOAdaptor.class);

    private int _port;

    private String _host;

	public ERWOAdaptor(String name, NSDictionary config) {
        super(name, config);

        Number number = (Number) config.objectForKey(WOProperties._PortKey);
        if (number != null)
            _port = number.intValue();
        if (_port < 0)
            _port = 0;
        WOApplication.application().setPort(_port);
        _host = (String) config.objectForKey(WOProperties._HostKey);
        WOApplication.application()._setHost(_host);
	}

	@Override
	public void registerForEvents() {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new WONettyAdaptorPipelineFactory());

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(_port));
	}

	@Override
	public void unregisterForEvents() {
		// TODO Auto-generated method stub
	}
}
