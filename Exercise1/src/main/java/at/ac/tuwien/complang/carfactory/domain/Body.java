package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

public class Body implements Serializable {
	//Static fields
	private static long next_id = 0;
	
	//Fields
	private long id;
	
	public Body() {
		next_id++;
		this.id = Body.next_id;
	}

	public long getId() {
		// TODO Auto-generated method stub
		return id;
	};
}
