package edu.cmu.cs.eyetrack.analysis.struct.trackit;

import java.util.HashMap;
import java.util.Map;

public class Experiment {

	public static enum TrialType {ALL_SAME, ALL_DIFFERENT, SAME_AS_TARGET};
	
	// Descriptors for the entire experiment
	private int numDistractors;
	private int objectSpeed;
	private TrialType type;
	private int numTrials;
	private int baseTrialLength;
	private boolean usesRandomTarget;
	private int fps;
	private int seed;
	private int gridX;
	private int gridY;
	private boolean usesBackgroundImages;
	private boolean usesMemCheck;
	
	// Sanity checker set during .csv input parsing
	private boolean parsedCorrectly = false;
	
	// Mapping of Trial ID --> Trial data
	private Map<Integer, Trial> trials;
	
	public Experiment() {
		trials = new HashMap<Integer, Trial>();
		
	}
	
	public Trial addTrial(int id, Trial t) {
		return trials.put(id, t);
	}
	
	public Map<Integer, Trial> getTrials() {
		return trials;
	}
	
	public int getNumDistractors() {
		return numDistractors;
	}
	public void setNumDistractors(int numDistractors) {
		this.numDistractors = numDistractors;
	}
	public int getObjectSpeed() {
		return objectSpeed;
	}
	public void setObjectSpeed(int objectSpeed) {
		this.objectSpeed = objectSpeed;
	}
	public TrialType getType() {
		return type;
	}
	public void setType(TrialType type) {
		this.type = type;
	}
	public int getNumTrials() {
		return numTrials;
	}
	public void setNumTrials(int numTrials) {
		this.numTrials = numTrials;
	}
	public int getBaseTrialLength() {
		return baseTrialLength;
	}
	public void setBaseTrialLength(int baseTrialLength) {
		this.baseTrialLength = baseTrialLength;
	}
	public int getFPS() {
		return fps;
	}
	public void setFPS(int fps) {
		this.fps = fps;
	}
	public int getSeed() {
		return seed;
	}
	public void setSeed(int seed) {
		this.seed = seed;
	}
	public int getGridX() {
		return gridX;
	}
	public void setGridX(int gridX) {
		this.gridX = gridX;
	}
	public int getGridY() {
		return gridY;
	}
	public void setGridY(int gridY) {
		this.gridY = gridY;
	}
	public boolean usesBackgroundImages() {
		return usesBackgroundImages;
	}
	public void setUsesBackgroundImages(boolean usesBackgroundImages) {
		this.usesBackgroundImages = usesBackgroundImages;
	}
	public boolean usesMemCheck() {
		return usesMemCheck;
	}
	public void setUsesMemCheck(boolean usesMemCheck) {
		this.usesMemCheck = usesMemCheck;
	}

	public boolean usesRandomTarget() {
		return usesRandomTarget;
	}

	public void setUsesRandomTarget(boolean usesRandomTarget) {
		this.usesRandomTarget = usesRandomTarget;
	}

	public boolean isParsedCorrectly() {
		return parsedCorrectly;
	}
	public void setParsedCorrectly(boolean parsedCorrectly) {
		this.parsedCorrectly = parsedCorrectly;
	}
}
