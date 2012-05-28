package at.ac.tuwien.complang.carfactory.domain;

public enum MotorType {
	KW_80 ("80 kW Motor"),
	KW_100 ("100 kW Motor"),
	KW_160 ("160 kW Motor");

	private final String type;
	
	private MotorType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return type;
	}
}
