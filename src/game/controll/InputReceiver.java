package game.controll;

import game.GameStatus;
import game.Player;
import game.PlayerMove;
import gui.GamePlayComponent;

import java.util.HashMap;

public class InputReceiver {
	private final HashMap<Player, PlayerInputReceiver> playerInputReceivers;
	private final GamePlayComponent gamePlayComponent;
	private final GameStatus gameStatus;
	
	public InputReceiver(GamePlayComponent gamePlayComponent, GameStatus gameStatus) {
		playerInputReceivers = new HashMap<Player, PlayerInputReceiver>();
		this.gamePlayComponent = gamePlayComponent;
		this.gameStatus = gameStatus;
	}
	
	public PlayerMove waitForPlayerMove() {
		PlayerInputReceiver inputReceiver = playerInputReceivers.get(gameStatus.getCurrentPlayer());
		if (inputReceiver == null)
			throw new IllegalArgumentException("Unknown Player.");
		
		return inputReceiver.waitForMove();
	}
	
	public void addLocalPlayer(Player player) {
		playerInputReceivers.put(player, new LocalPlayerInputReceiver(player, gamePlayComponent, gameStatus));
	}
	
	public void addNetworkPlayer(Player player) {
		// TODO
	}
}
