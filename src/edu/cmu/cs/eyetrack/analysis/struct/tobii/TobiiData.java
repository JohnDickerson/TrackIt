package edu.cmu.cs.eyetrack.analysis.struct.tobii;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;

public class TobiiData {

	private TobiiEventMap eventMap;
	
	private Map<Integer, Trajectory<TobiiFrame>> trajectories;
	
	
	public TobiiData(TobiiEventMap eventMap) {
		this.eventMap = eventMap;	
		this.trajectories = new HashMap<Integer, Trajectory<TobiiFrame>>();
	}
	
	public TobiiData() {
		this(null);
	}

	public TobiiHeader getHeader() {
		return eventMap.getHeader();
	}

	public Map<Integer, Trajectory<TobiiFrame>> getTrajectories() {
		return trajectories;
	}

	public void registerTrajectory(Integer trialID, Trajectory<TobiiFrame> trajectory) {
		trajectories.put(trialID, trajectory);
	}
	
	// Grabs the Trajectory whose start time is closest to the passed-in timestamp
	// O(n) naive search for now, assume we don't have many trajectories
	public Trajectory<TobiiFrame> getClosestTrajectoryTo(long timestamp) {
		
		Trajectory<TobiiFrame> bestTraj = null;
		long bestDelta = Long.MAX_VALUE;
		
		for(Trajectory<TobiiFrame> traj : trajectories.values()) {
			
			Map.Entry<Long, TobiiFrame> startFrame = traj.getFrameEqualHigher(0L);
			if(null == startFrame) {
				// Somebody was wildly jamming the spacebar, no frames recorded between one Space and the next
				continue;
			}
			
			long trajStart = startFrame.getKey();
			long currDelta = Math.abs( trajStart - timestamp );
			
			if( currDelta < bestDelta ) {
				bestDelta = currDelta;
				bestTraj = traj;
			}
			
		}
		
		if(bestDelta == Long.MAX_VALUE) {
			System.err.println("Could not find a matching Tobii timestamp; skipping.");
			return null;
		} else {
			System.err.println("For Track-It timestamp " + timestamp + ", found match at Tobii timestamp " + bestDelta + " away.");
			return bestTraj;
		}
	}
}
