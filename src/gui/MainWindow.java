package gui;

import game.GameStatus;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

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
	
	private GameStatus gameStatus = null;
	private final NewGameWindow newGameWindow = new NewGameWindow();
	
	public MainWindow() throws IOException {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		gamePlay = new GamePlayComponent();
		frame.add(gamePlay);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = menuBar.add(new JMenu("Game"));
		newGameMenuItem = gameMenu.add(new JMenuItem("New Game"));
		frame.setJMenuBar(menuBar);
		
		newGameMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!newGameWindow.isVisible()) {
					newGameWindow.setLocation(frame.getLocation());
					newGameWindow.setVisible(true);
					new Thread(new Runnable() {
						@Override
						public void run() {
							gameStatus = newGameWindow.waitForGameStatus();
							if(gameStatus != null)
								startGame();
						}
					}).start();
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
		
		gamePlay.setPlayers(gameStatus.getPlayers());
	}
	
	private void setDefaultFramePosition() {
		// set window to the middle of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize.width / 2, screenSize.height / 2);
		frame.setLocation(screenSize.width / 4, screenSize.height / 4);
	}
}
