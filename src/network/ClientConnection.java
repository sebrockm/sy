package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class ClientConnection {
	private class Ping implements Serializable {
		private static final long serialVersionUID = 2544201995473409008L;
	}
	
	private final ObjectInputStream inputStream;
	private final ObjectOutputStream outputStream;
	
	public ClientConnection(Socket socket) throws IOException {
		inputStream = new ObjectInputStream(socket.getInputStream());
		outputStream = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public boolean Ping() {
		try {
			outputStream.writeObject(new Ping());
			return inputStream.readObject().getClass().equals(Ping.class);
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}
}
