package network;

import game.PlayerMove;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerToClientConnection {	
	private final ObjectInputStream inputStream;
	private final ObjectOutputStream outputStream;
	
	public ServerToClientConnection(Socket socket) throws IOException {
		inputStream = new ObjectInputStream(socket.getInputStream());
		outputStream = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public boolean Ping() {
		try {
			outputStream.writeObject(ProtocolHelper.PING);
			return inputStream.readObject().equals(ProtocolHelper.PING);
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
	}
	
	public PlayerMove requestPlayerMove() {
		try {
			outputStream.writeObject(ProtocolHelper.MOVE_REQUEST);
			return (PlayerMove)inputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
