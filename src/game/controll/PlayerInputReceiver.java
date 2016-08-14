package game.controll;

import game.PlayerMove;

public interface PlayerInputReceiver {
	public PlayerMove waitForMove();
}
