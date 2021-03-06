package pipe.gui.undo;

import pipe.dataLayer.DataLayer;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;

public class AddTimedInhibitorArcCommand extends TAPNElementCommand {
	private final TimedInhibitorArcComponent inhibitorArc;

	public AddTimedInhibitorArcCommand(TimedInhibitorArcComponent inhibitorArc,
			TimedArcPetriNet tapn, DataLayer guiModel, DrawingSurfaceImpl view) {
		super(tapn, guiModel, view);
		this.inhibitorArc = inhibitorArc;
	}

	@Override
	public void undo() {
		inhibitorArc.delete();
		view.repaint();
	}

	@Override
	public void redo() {
		inhibitorArc.undelete(view);
		tapn.add(inhibitorArc.underlyingTimedInhibitorArc());
		view.repaint();
	}

}
