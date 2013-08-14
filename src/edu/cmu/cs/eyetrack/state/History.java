package edu.cmu.cs.eyetrack.state;

import java.util.ArrayList;

public class History {

	private ArrayList<Trial> history;
	
	public History() {
		history = new ArrayList<Trial>();
	}
	
	protected void addTrial(Trial trial) {
		history.add(trial);
	}

}
