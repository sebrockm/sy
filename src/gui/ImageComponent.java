package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

public class ImageComponent extends JComponent {

	private static final String mapPath = "res/sy_map.jpg";
	private static final double SCALE_FACTOR = 1.1;
	private static final double MAX_SCALE = 5;
	private static final double MIN_SCALE = 0.3;
	
	private final BufferedImage mapImage;
	private double scale;
	private double topLeftCornerX;
	private double topLeftCornerY;

	public ImageComponent() throws IOException {
		super();
		
		setDefaultScale();
		topLeftCornerX = 0;
		topLeftCornerY = 0;
		
		mapImage = ImageIO.read(new File(mapPath));
		setVisible(true);
		setDoubleBuffered(true);
		
		MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
			private int lastX;
			private int lastY;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					lastX = e.getX();
					lastY = e.getY();
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					translate(e.getX() - lastX, e.getY() - lastY);
					repaint();
					lastX = e.getX();
					lastY = e.getY();
				}
			}
		};
		addMouseListener(mouseInputAdapter);
		addMouseMotionListener(mouseInputAdapter);
		
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation() < 0)
					for(int i = 0; i < -e.getWheelRotation(); ++i)
						zoomIn(e.getX(), e.getY());
				else
					for(int i = 0; i < e.getWheelRotation(); ++i)
						zoomOut(e.getX(), e.getY());
				
				repaint();
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(g instanceof Graphics2D) {
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	        
	        AffineTransform at = AffineTransform.getTranslateInstance(topLeftCornerX, topLeftCornerY);
	        at.scale(scale, scale);
	        
	        g2.drawRenderedImage(mapImage, at);
		}
	}
	
	public double fromOuterToImageCoordinateX(double x) {
		return (x - topLeftCornerX) / scale;
	}
	
	public double fromOuterToImageCoordinateY(double y) {
		return (y - topLeftCornerX) / scale; 
	}
	
	public void zoomIn(double x, double y) {
		if(scale < MAX_SCALE) {
			topLeftCornerX = x + (topLeftCornerX - x) * SCALE_FACTOR;
			topLeftCornerY = y + (topLeftCornerY - y) * SCALE_FACTOR;
			scale *= SCALE_FACTOR;
		}
	}
	
	public void zoomOut(double x, double y) {
		if(scale > MIN_SCALE) {
			topLeftCornerX = x + (topLeftCornerX - x) / SCALE_FACTOR;
			topLeftCornerY = y + (topLeftCornerY - y) / SCALE_FACTOR;
			scale /= SCALE_FACTOR;
		}
	}
	
	public void setDefaultScale() {
		scale = 1;
	}
	
	public void translate(double dx, double dy) {
		topLeftCornerX += dx;
		topLeftCornerY += dy;
	}
}
