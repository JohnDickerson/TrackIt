package edu.cmu.cs.eyetrack.analysis.struct;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Trajectory<E extends Frame> {

	private NavigableMap<Long, E> frames;
	private long lengthMS;
	private long rawTimestampStart;
	
	public Trajectory() {
		this(0L);
	}
	

	public Trajectory(long rawTimestampStart) {
		this.rawTimestampStart = rawTimestampStart;
		frames = new TreeMap<Long, E>();
		lengthMS = Long.MAX_VALUE;
	}
	
	public void registerFrame(E frame) {
		frames.put(frame.getTimestamp(), frame);
	}
	
	public Collection<E> getFrames() {
		return frames.values();
	}
	
	public Map.Entry<Long, E> getFrameStrictlyLower(Long timestamp) {
		return frames.lowerEntry(timestamp);
	}
	
	public Map.Entry<Long, E> getFrameEqualLower(Long timestamp) {
		return frames.floorEntry(timestamp);
	}
	
	public Map.Entry<Long, E> getFrameStrictlyHigher(Long timestamp) {
		return frames.higherEntry(timestamp);
	}
	
	public Map.Entry<Long, E> getFrameEqualHigher(Long timestamp) {
		return frames.ceilingEntry(timestamp);
	}
	
	public Map.Entry<Long, E> getClosestEqual(Long timestamp) {
		Map.Entry<Long, E> lower = getFrameEqualLower(timestamp);
		Map.Entry<Long, E> higher = getFrameEqualHigher(timestamp);
		
		if(lower == null && higher == null) {
			return null;
		} else if(lower == null) {
			return higher;
		} else if(higher == null) { 
			return lower;
		} else {
			long lowerDiff = timestamp - lower.getKey();
			long higherDiff = higher.getKey() - timestamp;
			return (lowerDiff < higherDiff) ? lower : higher;
		}
	}

	public long getLengthMS() {
		return lengthMS;
	}

	// Returns raw wall-clock time start
	public long getTimestampRecStart() {
		return rawTimestampStart;
	}
	
	public void setLengthMS(long lengthMS) {
		this.lengthMS = lengthMS;
	}
	
	
}
