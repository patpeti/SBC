package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface ISpaceListener {
	
	public void onObjectWrittenInSpace(ICarPart carPart);
	public void setGui(ProductionUI gui);

}
