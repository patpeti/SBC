package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public class SpaceListenerImpl implements ISpaceListener{
	private ProductionUI gui;

	public void onObjectWrittenInSpace(ICarPart carPart) {
		// TODO do something if new object added into the space
		System.out.println("#LISTENER#: Object created: ");
		gui.onNewSpaceObject(carPart);
	}

	public void setGui(ProductionUI gui) {
		this.gui = gui;
		
	}

}
