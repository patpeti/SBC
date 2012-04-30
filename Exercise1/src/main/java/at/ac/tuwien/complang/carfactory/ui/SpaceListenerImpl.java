package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public class SpaceListenerImpl implements ISpaceListener{
	private ISpaceObserver gui;

	public void onObjectWrittenInSpace(ICarPart carPart) {
		// TODO do something if new object added into the space
		System.out.println("#LISTENER#: Object created: ");
		gui.onNewSpaceObject(carPart);
	}

	public void setSpaceObserver(ISpaceObserver gui) {
		this.gui = gui;
		
	}

}
