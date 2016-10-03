package network;

import game.GameStatus;
import game.PlayerMove;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientToServerConnection {
	private final Socket socket;
	private final ObjectOutputStream outputStream;
	private final ObjectInputStream inputStream;
	
	public ClientToServerConnection(String hostAddress) throws UnknownHostException, IOException {
		socket = new Socket(hostAddress, ProtocolHelper.PORT);
		outputStream = new ObjectOutputStream(socket.getOutputStream());
		inputStream = new ObjectInputStream(socket.getInputStream());
	}
	
	public boolean ping() {
		try {
			outputStream.writeObject(ProtocolHelper.PING);
			outputStream.flush();
			return inputStream.readObject().equals(ProtocolHelper.SUCCESS);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean sendName(String name) {
		try {
			outputStream.writeObject(name);
			outputStream.flush();
			return inputStream.readObject().equals(ProtocolHelper.SUCCESS);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean sendMove(PlayerMove move) {
		try {
			outputStream.writeObject(move);
			outputStream.flush();
			return inputStream.readObject().equals(ProtocolHelper.SUCCESS);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public GameStatus requestGameStatus() {
		try {
			outputStream.writeObject(ProtocolHelper.GAME_STATUS_REQUEST);
			outputStream.flush();
			return (GameStatus)inputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
