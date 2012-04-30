package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface ISpaceObserver {

	public void onNewSpaceObject(ICarPart carpart);
	
}
