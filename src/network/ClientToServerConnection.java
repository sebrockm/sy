package network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientToServerConnection {
	private final Socket socket;
	
	public ClientToServerConnection(String hostAddress) throws UnknownHostException, IOException {
		socket = new Socket(hostAddress, ProtocolHelper.PORT);
	}
}
