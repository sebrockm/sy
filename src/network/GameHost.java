package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameHost {
	private final ServerSocket serverSocket;
	
	public GameHost(int port) throws IOException
	{
		serverSocket = new ServerSocket(port);
	}
	
	public ClientConnection waitForIncomingClientConnection() {
		try {
			Socket socket = serverSocket.accept();
			return new ClientConnection(socket);
		} catch (IOException e) {
			return null;
		}
	}
}
