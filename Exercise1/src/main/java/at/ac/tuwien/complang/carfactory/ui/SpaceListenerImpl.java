package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.application.enums.SpaceChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public class SpaceListenerImpl implements ISpaceListener{
	private ISpaceObserver gui;

	public void onObjectWrittenInSpace(ICarPart carPart) {
		
		gui.onSpaceChange(carPart, SpaceChangeType.WRITE);
	}

	public void setSpaceObserver(ISpaceObserver gui) {
		this.gui = gui;
		
	}



}
