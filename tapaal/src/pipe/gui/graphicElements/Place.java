package pipe.gui.graphicElements;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;

/**
  * Class for drawing a Place
 */
public abstract class Place extends PlaceTransitionObject {

	private static final long serialVersionUID = -5155964364065651381L;


	// Value of the capacity restriction; 0 means no capacity restriction 
	protected Integer capacity = 0;

	protected static final int DIAMETER = Pipe.PLACE_TRANSITION_HEIGHT;

	// Token Width and Height
	protected static int tWidth = 5;
	protected static int tHeight = 5;

	// Token dot position offsets
	protected static int tLeftX = 7;
	protected static int tRightX = 19;
	protected static int tTopY = 7;
	protected static int tBotY = 19;
	protected static int tMiddleX = 13;
	protected static int tMiddleY = 13;

	// Ellipse2D.Double place
	protected static Ellipse2D.Double placeEllipse = new Ellipse2D.Double(0, 0,	DIAMETER, DIAMETER);
	protected static Shape proximityPlace = (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(placeEllipse);

	public Place(double positionXInput, double positionYInput, String idInput,
			Double nameOffsetXInput, Double nameOffsetYInput) {
		super(positionXInput, positionYInput, idInput,	nameOffsetXInput, nameOffsetYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCentre((int) positionX, (int) positionY);
	}

	
	public Place(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		componentWidth = DIAMETER;
		componentHeight = DIAMETER;
		setCentre((int) positionX, (int) positionY);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(1.0f));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
			//pnName.setForeground(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
			//pnName.setForeground(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.fill(placeEllipse);

		if (selected) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}
		g2.draw(placeEllipse);

		g2.setStroke(new BasicStroke(1.0f));
	}

	/**
	 * Returns the diameter of this Place at the current zoom
	 */
	private int getDiameter() {
		return (Zoomer.getZoomedValue(DIAMETER, zoom));
	}

	@Override
	public boolean contains(int x, int y) {
		double unZoomedX = Zoomer.getUnzoomedValue(x - COMPONENT_DRAW_OFFSET, zoom);
		double unZoomedY = Zoomer.getUnzoomedValue(y - COMPONENT_DRAW_OFFSET, zoom);

		Arc someArc = CreateGui.getDrawingSurface().createArc;
		if (someArc != null) { // Must be drawing a new Arc if non-NULL.
			if ((proximityPlace.contains((int) unZoomedX, (int) unZoomedY) || placeEllipse
					.contains((int) unZoomedX, (int) unZoomedY))
					&& areNotSameType(someArc.getSource())) {
				// assume we are only snapping the target...
				if (someArc.getTarget() != this) {
					someArc.setTarget(this);
				}
				someArc.updateArcPosition();
				return true;
			} else {
				if (someArc.getTarget() == this) {
					someArc.setTarget(null);
					updateConnected();
				}
				return false;
			}
		} else {
			return placeEllipse.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

	@Override
	public void updateEndPoint(Arc arc) {
		if (arc.getSource() == this) {
			// Make it calculate the angle from the centre of the place rather
			// than the current start point
			arc.setSourceLocation(positionX + (getDiameter() * 0.5), positionY
					+ (getDiameter() * 0.5));
			double angle = arc.getArcPath().getStartAngle();
			arc.setSourceLocation(positionX + centreOffsetLeft()
					- (0.5 * getDiameter() * (Math.sin(angle))), positionY
					+ centreOffsetTop()
					+ (0.5 * getDiameter() * (Math.cos(angle))));
		} else {
			// Make it calculate the angle from the centre of the place rather
			// than the current target point
			arc.setTargetLocation(positionX + (getDiameter() * 0.5), positionY
					+ (getDiameter() * 0.5));
			double angle = arc.getArcPath().getEndAngle();
			arc.setTargetLocation(positionX + centreOffsetLeft()
					- (0.5 * getDiameter() * (Math.sin(angle))), positionY
					+ centreOffsetTop()
					+ (0.5 * getDiameter() * (Math.cos(angle))));
		}
	}

	@Override
	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		update(true);
	}

	@Override
	public void addedToGui() {
		super.addedToGui();
		update(true);
	}

	@Override
	public void update(boolean displayConstantNames) {
		if (attributesVisible) {
			pnName.setText("");
		} else {
			pnName.setText("");
		}
		pnName.zoomUpdate(zoom);
		super.update(displayConstantNames);
		repaint();
	}


}
