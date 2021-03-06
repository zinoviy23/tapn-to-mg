/*
 * Created on 07-Mar-2004
 * Author is Michael Camacho
 */
package pipe.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.gui.CreateGui;
import pipe.gui.graphicElements.AnnotationNote;

public class EditAnnotationBorderAction extends AbstractAction {

	private static final long serialVersionUID = -2849415077610764209L;
	private AnnotationNote selected;

	public EditAnnotationBorderAction(AnnotationNote component) {
		selected = component;
	}

	/** Action for editing the text in an AnnotationNote */
	public void actionPerformed(ActionEvent e) {
		CreateGui.getDrawingSurface().getUndoManager().addNewEdit(
				selected.showBorder(!selected.isShowingBorder()));
	}

}
