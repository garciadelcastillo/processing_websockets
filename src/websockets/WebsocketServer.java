package websockets;

import java.lang.reflect.Method;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import processing.core.PApplet;

/**
 *
 * @author Lasse Steenbock Vestergaard
 * @author Abe Pazos (changes)
 *
 */
public class WebsocketServer {
	private Method websocketServerEvent;
	private Method websocketServerEventBinary;
	private WebsocketServerController serverController;

	private static int MAX_MSG_SIZE = 65536;

	/**
	 *
	 * The websocket server object that is initiated directly in the Processing sketch
	 *
	 * @param parent Processing's PApplet object
	 * @param port The port number you want the websocket server to initiate its connection on
	 * @param uri The uri you want your server to respond to. Ex. /john (if the port is set to ex. 8025, then the full URI would be ws://localhost:8025/john).
	 */
	public WebsocketServer(PApplet parent, int port, String uri){
		this(parent, parent, port, uri);
	}

	/**
	 *
	 * @param parent Processing's PApplet object
	 * @param callbacks The object implementing .webSocketServerEvent()
	 * @param port The port number you want the websocket server to initiate its connection on
	 * @param uri The uri you want your server to respond to. Ex. /john (if the port is set to ex. 8025, then the full URI would be ws://localhost:8025/john).
	 */
	public WebsocketServer(PApplet parent, Object callbacks, int port,
						   String uri) {

		parent.registerMethod("dispose", this);

		try {
			websocketServerEvent = callbacks.getClass().getMethod(
					"webSocketServerEvent", String.class);
			websocketServerEventBinary = callbacks.getClass().getMethod(
					"webSocketServerEvent", byte[].class, int.class, int.class);
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}

		Server server = new Server(port);
		serverController = new WebsocketServerController(callbacks,
				websocketServerEvent, websocketServerEventBinary);

		WebSocketHandler wsHandler = new WebSocketHandler() {

			@Override
			public void configure(WebSocketServletFactory factory){
				factory.getPolicy().setMaxTextMessageSize(MAX_MSG_SIZE);
				factory.getPolicy().setMaxBinaryMessageSize(MAX_MSG_SIZE);
				factory.setCreator(new WebsocketServerCreator(serverController));
			}
		};

		ContextHandler contextHandler = new ContextHandler(uri);
		contextHandler.setAllowNullPathInfo(true); // disable redirect from /ws to /ws/
		contextHandler.setHandler(wsHandler);

		server.setHandler(contextHandler);

		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Send String message to all connected clients
	 *
	 * @param message The message content as a String
	 */
	public void sendMessage(String message) {
		serverController.writeAllMembers(message);
	}

	/**
	 *
	 * Send byte[] message to all connected clients
	 *
	 * @param data The message content as a byte[]
	 */
	public void sendMessage(byte[] data) {
		serverController.writeAllMembers(data);
	}

	/**
	 * Set the max message size in bytes, 64Kb by default
	 * @param bytes
	 */
	public static void setMaxMessageSize(int bytes) {
		MAX_MSG_SIZE = bytes;
	}

	/**
	 *
	 */
	public void dispose(){
		// Anything in here will be called automatically when
	    // the parent sketch shuts down. For instance, this might
	    // shut down a thread used by this library.
	}
}
