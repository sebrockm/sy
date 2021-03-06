package gui;

import game.GameStatus;
import game.MrXPlayer;
import game.Player;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import map.data.GraphData;

public class GamePlayComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static final String mapPath = "res/sy_map.jpg";
	private static final String dataPath = "res/sy_map.data";
	private static final double SCALE_FACTOR = 1.1;
	private static final double MAX_SCALE = 5;
	private static final double MIN_SCALE = 0.3;
	
	private final BufferedImage mapImage;
	private GraphData graphData;
	private double scale;
	private double topLeftCornerX;
	private double topLeftCornerY;
	
	private Shape highlightedArea = null;

	private GameStatus gameStatus = null;
	
	public GamePlayComponent() throws IOException {
		super();
		
		setDefaultScale();
		topLeftCornerX = 0;
		topLeftCornerY = 0;
		
		try {
			graphData = GraphData.loadFromFile(dataPath);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		mapImage = ImageIO.read(new File(mapPath));
		setVisible(true);
		setDoubleBuffered(true);
		
		GamePlayMouseInputAdapter mouseInputAdapter = new GamePlayMouseInputAdapter(this);
		addMouseListener(mouseInputAdapter);
		addMouseMotionListener(mouseInputAdapter);
		addMouseWheelListener(mouseInputAdapter);
	}
	
	@Override
	protected void paintComponent(Graphics g) {		
		super.paintComponent(g);
		if(!(g instanceof Graphics2D)) 
			return;
		
        Graphics2D g2 = (Graphics2D)g;
        
        AffineTransform at = fromImageToOuterTransform();     
        g2.drawRenderedImage(mapImage, at);
           
        drawPlayerTokens(g2);
        
        if(highlightedArea != null) {
	        drawHighlightedArea(highlightedArea, g2);
        }
        
        drawPlayerInfo(g2);
	}
	
	private void drawPlayerInfo(Graphics2D g2) {
		if(gameStatus == null)
			return;
		
		final int fontSize = 17;
		Font font = new Font("Dialog", Font.PLAIN, fontSize);
        g2.setFont(font);
        
        final String header = "Name         Taxi Bus U-Ground Black Double";
        final String starDummy = "*Name         Taxi Bus U-Ground Black Double";
        final String taxiDummy = "Taxi Bus U-Ground Black Double";
        final String busDummy = "Bus U-Ground Black Double";
        final String uGroundDummy = "U-Ground Black Double";
        final String blackDummy = "Black Double";
        final String doubleDummy = "Double";
        
        final float starOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(starDummy, g2).getWidth();
        final float nameOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(header, g2).getWidth();
        final float taxiOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(taxiDummy, g2).getWidth();
        final float busOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(busDummy, g2).getWidth();
        final float uGroundOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(uGroundDummy, g2).getWidth();
        final float blackOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(blackDummy, g2).getWidth();
        final float doubleOffset = getWidth() - (float)g2.getFontMetrics().getStringBounds(doubleDummy, g2).getWidth();
        
        g2.setColor(Color.BLACK);
        g2.drawString(header, nameOffset, fontSize);
        
        for(int i = 0; i < gameStatus.getPlayers().size(); ++i) {
        	Player player = gameStatus.getPlayers().get(i);
        	
        	float yOffset = fontSize * (i + 2.3f);
        	g2.setColor(player.getColor());
        	
        	if (player == gameStatus.getCurrentPlayer())
        		g2.drawString("*", starOffset, yOffset);
        	
        	g2.drawString(player.getName(), nameOffset, yOffset);
        	g2.drawString(player.getNumberOfTaxiTickets() + "", taxiOffset, yOffset);
        	g2.drawString(player.getNumberOfBusTickets() + "", busOffset, yOffset);
        	g2.drawString(player.getNumberOfUndergroundTickets() + "", uGroundOffset, yOffset);
        	if(player instanceof MrXPlayer) {
        		MrXPlayer mrX = (MrXPlayer) player;
        		g2.drawString(mrX.getNumberOfBlackTickets() + "", blackOffset, yOffset);
        		g2.drawString(mrX.getNumberOfDoubleMoves() + "", doubleOffset, yOffset);
        	}
        }
	}
	
	private void drawPlayerTokens(Graphics2D g2) {
		if(gameStatus == null)
			return;
		
		for(Player player : gameStatus.getPlayers()) {
			//if(!player.isVisible())
				//continue;
			
			Rectangle2D r = graphData.getArea(player.getCurrentStationId()).getBounds2D();
			double atY = r.getMinY() - TokenShape.getHeight() + r.getHeight();
	        AffineTransform at = AffineTransform.getTranslateInstance(r.getMinX(), atY);
	        
	        Shape transformedToken = at.createTransformedShape(TokenShape.getInstance());
			at = fromImageToOuterTransform();
	        transformedToken = at.createTransformedShape(transformedToken);
	
	        Rectangle2D bb = transformedToken.getBounds2D();
	        float cx = (float)bb.getCenterX();
	        
	        g2.setPaint(new GradientPaint(cx, (float)bb.getMaxY(), Color.BLACK, cx, (float)bb.getMinY(), player.getColor()));
	        g2.fill(transformedToken);
		}
	}
	
	private void drawHighlightedArea(Shape areaOnImage, Graphics2D g2) {
		Color lastColor = g2.getColor();
        
        Shape areaOnScreen = fromImageToOuterTransform().createTransformedShape(areaOnImage);     
        Rectangle2D bounds = areaOnScreen.getBounds2D();
        
        AffineTransform at = fromOuterToImageTransform();
        
        for(int y = 0; y < bounds.getHeight(); ++y) {
    		for(int x = 0; x < bounds.getWidth(); ++x) {
    			Point2D screenP = new Point2D.Double(bounds.getMinX() + x, bounds.getMinY() + y);
    			if(!areaOnScreen.contains(screenP))
    				continue;
    			
    			Point2D imageP = at.transform(screenP, null);
    			Color pixelColor = new Color(mapImage.getRGB((int)imageP.getX(), (int)imageP.getY()), true).brighter();
    			g2.setColor(pixelColor);
    			g2.draw(new Rectangle((int)screenP.getX(), (int)screenP.getY(), 1, 1));
    		}
        }
        
        g2.setColor(lastColor);
	}
	
	public GraphData getGraphData() {
		return graphData;
	}
	
	public AffineTransform fromOuterToImageTransform() {
		AffineTransform at = AffineTransform.getScaleInstance(1/scale, 1/scale);
		at.translate(-topLeftCornerX, -topLeftCornerY);
		return at;
	}
	
	public AffineTransform fromImageToOuterTransform() {
		AffineTransform at = AffineTransform.getTranslateInstance(topLeftCornerX, topLeftCornerY);
        at.scale(scale, scale);
		return at;
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
	
	public double getCurrentZoomFactor() {
		return scale;
	}
	
	public void setDefaultScale() {
		scale = 1;
	}
	
	public void translate(double dx, double dy) {
		topLeftCornerX += dx;
		topLeftCornerY += dy;
		repaint();
	}
	
	public void startGame(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
		
		gameStatus.setGameEndCallback(new GameStatus.GameEndCallback() {
			
			@Override
			public void mrXWins() {
				JOptionPane.showMessageDialog(GamePlayComponent.this,
						"Mr. X was able to get away!", "End of Game", JOptionPane.DEFAULT_OPTION);
				GamePlayComponent.this.gameStatus = null;
			}
			
			@Override
			public void agentsWin() {
				JOptionPane.showMessageDialog(GamePlayComponent.this, 
						GamePlayComponent.this.gameStatus.getCurrentPlayer().getName() + " caught Mr. X!",
						"End of Game", JOptionPane.DEFAULT_OPTION);
				GamePlayComponent.this.gameStatus = null;
			}
		});
	}
	
	public boolean setHighlightAt(int x, int y) {
		Shape area = lookupStationArea(x, y);
		
		if (area == highlightedArea)
			return false;
		
		highlightedArea = area;
		repaint();
		return true;
	}
	
	public boolean isGameStarted() {
		return gameStatus != null;
	}
	
	private Shape lookupStationArea(int x, int y) {
		Point2D imageP = fromOuterToImageTransform().transform(new Point2D.Double(x, y), null);
		int stationId = graphData.getNodeAtPosition(imageP.getX(), imageP.getY());
		if (stationId == 0)
			return null;
		return graphData.getArea(stationId);
	}
}
