package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.application.enums.SpaceChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface ISpaceObserver {
	public void addPart(ICarPart carpart, SpaceChangeType changeType);
}
