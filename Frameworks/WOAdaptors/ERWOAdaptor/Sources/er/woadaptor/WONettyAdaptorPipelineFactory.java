package er.woadaptor;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
  * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
  * @author Andy Taylor (andy.taylor@jboss.org)
  * @author <a href="http://gleamynode.net/">Trustin Lee</a>
  * 
  * {@link} http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpServerPipelineFactory.html
  * 
  * @author ravim ERWOAdaptor version
  */
public class WONettyAdaptorPipelineFactory implements ChannelPipelineFactory {
	public ChannelPipeline getPipeline() throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Uncomment the following line if you want HTTPS
		//SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
		//engine.setUseClientMode(false);
		//pipeline.addLast("ssl", new SslHandler(engine));

		pipeline.addLast("decoder", new HttpRequestDecoder());
		// Uncomment the following line if you don't want to handle HttpChunks.
		//pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		// Remove the following line if you don't want automatic content compression.
		pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("handler", new WONettyAdaptorRequestHandler());
		return pipeline;
	}
}
