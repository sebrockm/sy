package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class GamePlayMouseInputAdapter extends MouseInputAdapter {
	private int lastX;
	private int lastY;
	private final GamePlayComponent gamePlayComponent;
	
	public GamePlayMouseInputAdapter(GamePlayComponent gamePlayComponent) {
		this.gamePlayComponent = gamePlayComponent;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(SwingUtilities.isLeftMouseButton(e)) {
			lastX = e.getX();
			lastY = e.getY();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		gamePlayComponent.setHighlightAt(e.getX(), e.getY());
		if(SwingUtilities.isLeftMouseButton(e)) {
			gamePlayComponent.translate(e.getX() - lastX, e.getY() - lastY);
			lastX = e.getX();
			lastY = e.getY();
			gamePlayComponent.repaint();
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.getWheelRotation() < 0)
			for(int i = 0; i < -e.getWheelRotation(); ++i)
				gamePlayComponent.zoomIn(e.getX(), e.getY());
		else
			for(int i = 0; i < e.getWheelRotation(); ++i)
				gamePlayComponent.zoomOut(e.getX(), e.getY());
		
		gamePlayComponent.setHighlightAt(e.getX(), e.getY());
		gamePlayComponent.repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if(gamePlayComponent.setHighlightAt(e.getX(), e.getY()))
			gamePlayComponent.repaint();
	}
}
