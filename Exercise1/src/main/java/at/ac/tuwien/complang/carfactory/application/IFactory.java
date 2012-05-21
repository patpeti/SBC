package at.ac.tuwien.complang.carfactory.application;

import java.util.Observer;

public interface IFactory {
	void start();
	void stop();
	void init(int count);
	boolean isRunning();
	/**
	 * Used to register a java.util.Observer that will
	 * be notified of state changes in the factory.
	 * @param observer The Observer to register with
	 * this factory.
	 */
	void addObserver(Observer observer);
}
