package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameHost {
	private final ServerSocket serverSocket;
	
	public GameHost() throws IOException {
		serverSocket = new ServerSocket(ProtocolHelper.PORT);
	}
	
	public ServerToClientConnection waitForIncomingClientConnection() {
		try {
			Socket socket = serverSocket.accept();
			return new ServerToClientConnection(socket);
		} catch (IOException e) {
			return null;
		}
	}
}
