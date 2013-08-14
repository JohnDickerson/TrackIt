package edu.cmu.cs.eyetrack.analysis.struct;

import edu.cmu.cs.eyetrack.helper.Coordinate;

public abstract class Frame implements Comparable<Frame> {

	public abstract Coordinate<Double> getPosition();
	public abstract Long getTimestamp();
	
	public int compareTo(Frame f) {
		return getTimestamp().compareTo(f.getTimestamp());
	}
}
