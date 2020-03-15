/*
 * SplitArcPointAction.java
 *
 * Created on 21-Jun-2005
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.ArcPathPoint;

/**
 * @author Nadeem
 * 
 *         This class is used to split a point on an arc into two to allow the
 *         arc to be manipulated further.
 */
public class SplitArcPointAction extends javax.swing.AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6080999798954816351L;
	private ArcPathPoint arcPathPoint;

	public SplitArcPointAction(ArcPathPoint _arcPathPoint) {
		arcPathPoint = _arcPathPoint;
	}

	public void actionPerformed(ActionEvent e) {
		CreateGui.getDrawingSurface().getUndoManager().addNewEdit(
				arcPathPoint.splitPoint());
	}

}
