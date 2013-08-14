package edu.cmu.cs.eyetrack.analysis.struct.trackit;

import java.util.List;

import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.helper.Coordinate;

public class Trial {
	
	// Individual headers/specifiers for the trial
	private int id;
	private long length;
	private long startTime;
	private long frameCount;
	private String target = null;
	private Coordinate<Double> targetFinalPos;
	private Coordinate<Integer> targetFinalGridPos;
	private Coordinate<Double> userClickPos;
	private Coordinate<Integer> userClickGridPos;
	private boolean gridClickCorrect = false;
	private long gridClickRT;
	private String lineupStimulus = null;
	private boolean lineupClickCorrect = false;
	private long lineupClickRT;
	
	// Data gleaned from reading the .csv input, lets us map indexes
	// to human-readable display
	private int targetIdx;
	private List<String> stimuliNames;
	
	//private List<List<Coordinate<Double>>> positions;
	private List<Trajectory<TrackItFrame>> trajectories;
	
	
	public Trial() {
		
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(long frameCount) {
		this.frameCount = frameCount;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Coordinate<Double> getTargetFinalPos() {
		return targetFinalPos;
	}

	public void setTargetFinalPos(Coordinate<Double> targetFinalPos) {
		this.targetFinalPos = targetFinalPos;
	}

	public Coordinate<Integer> getTargetFinalGridPos() {
		return targetFinalGridPos;
	}

	public void setTargetFinalGridPos(Coordinate<Integer> targetFinalGridPos) {
		this.targetFinalGridPos = targetFinalGridPos;
	}

	public Coordinate<Double> getUserClickPos() {
		return userClickPos;
	}

	public void setUserClickPos(Coordinate<Double> userClickPos) {
		this.userClickPos = userClickPos;
	}

	public Coordinate<Integer> getUserClickGridPos() {
		return userClickGridPos;
	}

	public void setUserClickGridPos(Coordinate<Integer> userClickGridPos) {
		this.userClickGridPos = userClickGridPos;
	}

	public boolean isGridClickCorrect() {
		return gridClickCorrect;
	}

	public void setGridClickCorrect(boolean gridClickCorrect) {
		this.gridClickCorrect = gridClickCorrect;
	}

	public String getLineupStimulus() {
		return lineupStimulus;
	}

	public void setLineupStimulus(String lineupStimulus) {
		this.lineupStimulus = lineupStimulus;
	}

	public boolean isLineupClickCorrect() {
		return lineupClickCorrect;
	}

	public void setLineupClickCorrect(boolean lineupClickCorrect) {
		this.lineupClickCorrect = lineupClickCorrect;
	}

	public int getTargetIdx() {
		return targetIdx;
	}

	public void setTargetIdx(int targetIdx) {
		this.targetIdx = targetIdx;
	}

	public List<String> getStimuliNames() {
		return stimuliNames;
	}

	public String getStimulusNameAt(int idx) {
		return stimuliNames.get(idx);
	}
	
	public void setStimuliNames(List<String> stimuliNames) {
		this.stimuliNames = stimuliNames;
	}

	public List<Trajectory<TrackItFrame>> getTrajectories() {
		return trajectories;
	}

	public void setTrajectories(List<Trajectory<TrackItFrame>> trajectories) {
		this.trajectories = trajectories;
	}

	public long getGridClickRT() {
		return gridClickRT;
	}

	public void setGridClickRT(long gridClickRT) {
		this.gridClickRT = gridClickRT;
	}

	public long getLineupClickRT() {
		return lineupClickRT;
	}

	public void setLineupClickRT(long lineupClickRT) {
		this.lineupClickRT = lineupClickRT;
	}
	
}
