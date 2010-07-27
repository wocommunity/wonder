package er.woadaptor;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.webobjects.appserver.WONettyAdaptorRequestHandler;

/**
  * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
  * @author Andy Taylor (andy.taylor@jboss.org)
  * @author <a href="http://gleamynode.net/">Trustin Lee</a>
  * 
  * @see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpServerPipelineFactory.html">HttpServerPipelineFactory</a>
  * 
  * @author ravim ERWOAdaptor version
  * 
  * @property WOMaxIOBufferSize 	Max http chunking size. Defaults to WO default 8196 
  * 								@see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/handler/codec/http/HttpMessageDecoder.html">HttpMessageDecoder</a>
  * 								{@link org.jboss.netty.handler.codec.http.HttpRequestDecoder}
  */
public class ERWOAdaptorPipelineFactory implements ChannelPipelineFactory {
	
	public static final Integer maxChunkSize = Integer.getInteger("WOMaxIOBufferSize", 8196);		// TODO ravi: CHECKME Netty default is 8192; WO default is 8196(!?)
	
	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Uncomment the following line if you want HTTPS
		//SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
		//engine.setUseClientMode(false);
		//pipeline.addLast("ssl", new SslHandler(engine));

		pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, maxChunkSize));
		// Uncomment the following line if you don't want to handle HttpChunks.
		//pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		// Remove the following line if you don't want automatic content compression.
		pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("handler", new WONettyAdaptorRequestHandler());
		return pipeline;
	}
}
