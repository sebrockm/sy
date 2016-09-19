package gui;

import game.GameStatus;
import game.Player;
import game.PlayerMove;
import game.controll.InputReceiver;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import network.ClientToServerConnection;

public class MainWindow {

	/**
	 * Temporary main().
	 * @param args 
	 */
	public static void main(String[] args) {
		try {
			new MainWindow();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final JFrame frame = new JFrame();
	private final GamePlayComponent gamePlay;
	private final JMenuItem newGameMenuItem;
	private final JMenuItem joinGameMenuItem;
	
	private GameStatus gameStatus = null;
	private InputReceiver inputReceiver;
	
	public MainWindow() throws IOException {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		gamePlay = new GamePlayComponent();
		frame.add(gamePlay);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = menuBar.add(new JMenu("Game"));
		newGameMenuItem = gameMenu.add(new JMenuItem("New Game"));
		joinGameMenuItem = gameMenu.add(new JMenuItem("Join Game"));
		frame.setJMenuBar(menuBar);
		
		newGameMenuItem.addActionListener(new ActionListener() {
			private final Object monitor = new Object();
			
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (monitor) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							gameStatus = new NewGameWindow(frame).waitForGameStatus();
							if(gameStatus != null)
								startGame();
						}
					}).start();
				}
			}
		});
		
		joinGameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String hostAddress = (String)JOptionPane.showInputDialog(
	                    frame,  "Enter Server IP Address:",
	                    "Enter Server IP Address",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null, null, "127.0.0.1");
				
				try {
					new ClientToServerConnection(hostAddress);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
		});
		
		setDefaultFramePosition();
		frame.setVisible(true);
	}
	
	private void startGame() {
		if(gameStatus == null) {
			JOptionPane.showMessageDialog(frame, "Create a new game first.");
			return;
		}
		
		gamePlay.startGame(gameStatus);
		
		inputReceiver = new InputReceiver(gamePlay, gameStatus);
		for (Player player : gameStatus.getPlayers())
			inputReceiver.addLocalPlayer(player);
		
		while (!gameStatus.isGameEnd()) {
			PlayerMove move = inputReceiver.waitForPlayerMove();
			gameStatus.moveCurrentPlayer(move);
		}
	}
	
	private void setDefaultFramePosition() {
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		Dimension twoThirdsSize = new Dimension(frame.getWidth() * 2 / 3, frame.getHeight() * 2 / 3);
		frame.setPreferredSize(twoThirdsSize);
	}
}
