package at.ac.tuwien.complang.carfactory.application;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;

public interface IFacade {
	IFactory getInstance(ProducerType type);
}
