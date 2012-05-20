package at.ac.tuwien.complang.carfactory.application;

public interface IFactory {
	void start();
	void stop();
	void init(int count);
	boolean isRunning();
}
