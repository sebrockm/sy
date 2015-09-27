package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;

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

	private final JFrame frame;
	private final ImageComponent mapImage;
	
	public MainWindow() throws IOException {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		mapImage = new ImageComponent();
		frame.add(mapImage);
		
		frame.setVisible(true);
		
		// set window to the middle of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize(screenSize.width / 2, screenSize.height / 2);
		frame.setLocation(screenSize.width / 4, screenSize.height / 4);
	}
}
