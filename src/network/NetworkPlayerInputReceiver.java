package network;

import game.PlayerMove;
import game.controll.PlayerInputReceiver;

public class NetworkPlayerInputReceiver implements PlayerInputReceiver {

	private final ServerToClientConnection clientConnection;
	
	public NetworkPlayerInputReceiver(ServerToClientConnection clientConnection) {
		this.clientConnection = clientConnection;
	}
	
	@Override
	public PlayerMove waitForMove() {
		return clientConnection.requestPlayerMove();
	}

}
