package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface ISpaceListener{
	
	public void onObjectWrittenInSpace(ICarPart carPart);
	public void setSpaceObserver(ISpaceObserver gui);
	

}
