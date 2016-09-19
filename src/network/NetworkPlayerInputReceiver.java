package network;

import game.PlayerMove;
import game.controll.PlayerInputReceiver;

public class NetworkPlayerInputReceiver implements PlayerInputReceiver {

	private final ClientConnection clientConnection;
	
	public NetworkPlayerInputReceiver(ClientConnection clientConnection) {
		this.clientConnection = clientConnection;
	}
	
	@Override
	public PlayerMove waitForMove() {
		return clientConnection.requestPlayerMove();
	}

}
