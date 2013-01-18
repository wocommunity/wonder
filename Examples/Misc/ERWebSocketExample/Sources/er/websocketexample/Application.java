package er.websocketexample;

import er.extensions.appserver.ERXApplication;
import er.websocketexample.websockets.ExampleWebSocketFactory;
import er.woadaptor.websockets.WebSocketStore;

public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
	}
	
	@Override
	public void finishInitialization() {
		super.finishInitialization();
		WebSocketStore.defaultWebSocketStore().setFactory(new ExampleWebSocketFactory());
	}
}
