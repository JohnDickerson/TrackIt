package edu.cmu.cs.eyetrack.state;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.Period;

import edu.cmu.cs.eyetrack.gui.shapes.Stimulus;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory;
import edu.cmu.cs.eyetrack.gui.shapes.StimulusFactory.StimulusType;
import edu.cmu.cs.eyetrack.helper.Util;
import edu.cmu.cs.eyetrack.io.CSVWritable;

public class Settings implements CSVWritable {

	private User user;
	private Experiment experiment;
	public static enum MemoryCheckType {mNONE, m2X2, mNXN};
	public static enum MotionConstraintType {PIXEL, GRID};
	public static enum MotionInterpolationType {LINEAR, RANDOM};
	
	public Settings() {
	}

	public Experiment getExperiment() {
		return experiment;
	}

	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String[] getCSVHeader() {
		return new String[] {
				"Track-It Version",
				"Name",
				"Gender",
				"Birthdate",
				"Age",
				"Test Date",
				"Test Location",
				"Number of Distractors",
				"Object Speed",
				"Trial Type",
				"Trial Count",
				"Trial Length",
				"Uses Random Target",
				"FPS",
				"Seed",
				"Grid X Size",
				"Grid Y Size",
				"Uses Background Images",
				"Background Image Directory",
				"Uses Memory Check",
				"Uses Fullscreen",
				"Motion Constraint Type",
				"Motion Interpolation Type",
		};
	}

	@Override
	public List<String[]> getCSVData() {

		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {
				Util.getTrackItVersion(),
				user.getName(),
				String.valueOf(user.getGender()),
				dateFormatter.format(user.getBirthdate()),
				String.valueOf(user.getAge()),
				dateFormatter.format(user.getTestDate()),
				user.getTestLocation(),
				String.valueOf(experiment.getNumDistractors()),
				String.valueOf(experiment.getObjectSpeed()),
				String.valueOf(experiment.getTrialType()),
				String.valueOf(experiment.getTrialCount()),
				String.valueOf(experiment.getTrialLength()),
				String.valueOf(experiment.getUsesRandomTarget()),
				String.valueOf(experiment.getFPS()),
				String.valueOf(experiment.getSeed()),
				String.valueOf(experiment.getGridXSize()),
				String.valueOf(experiment.getGridYSize()),
				String.valueOf(experiment.getUsesBGImages()),
				experiment.getImageDirectory(),
				String.valueOf(experiment.getMemCheckType()),
				String.valueOf(experiment.getUsesFullscreen()),
				String.valueOf(experiment.getMotionConstraintType()),
				String.valueOf(experiment.getMotionInterpolationType()),
		});

		return data;
	}


	// Data regarding the test setup itself
	public class Experiment {

		private int numDistractors;
		private double objectSpeed;
		private TrialType trialType;
		private int trialCount;
		private double trialLength;
		private boolean usesRandomTarget;
		private Stimulus canonicalTarget;
		private double fps;
		private long seed;
		private int gridXSize;
		private int gridYSize;
		private int pixelWidth;
		private int pixelHeight;
		private int insetX;
		private int insetY;
		private boolean usesBGImages;
		private String bgImageDirectory;
		private MemoryCheckType memCheckType;
		private boolean usesFullscreen;
		private MotionConstraintType motionConstraintType;
		private MotionInterpolationType motionInterpolationType;
		
		public Experiment(int numDistractors, double objectSpeed, TrialType trialType, int trialCount, double trialLength, boolean usesRandomTarget, Stimulus canonicalTarget, double fps, long seed, int gridXSize, int gridYSize, int pixelWidth, int pixelHeight, int insetX, int insetY, boolean usesBGImages, String bgImageDirectory, MemoryCheckType memCheckType, boolean usesFullscreen, MotionConstraintType motionConstraintType, MotionInterpolationType motionInterpolationType) {
			this.numDistractors = numDistractors;
			this.objectSpeed = objectSpeed;
			this.trialType = trialType;
			this.trialCount = trialCount;
			this.trialLength = trialLength;
			this.usesRandomTarget = usesRandomTarget;
			this.canonicalTarget = canonicalTarget;
			this.fps = fps;
			this.seed = seed;
			this.gridXSize = gridXSize;
			this.gridYSize = gridYSize;
			this.pixelWidth = pixelWidth;
			this.pixelHeight = pixelHeight;
			this.insetX = insetX;
			this.insetY = insetY;
			this.usesBGImages = usesBGImages;
			this.bgImageDirectory = bgImageDirectory;
			this.memCheckType = memCheckType;
			this.usesFullscreen = usesFullscreen;
			this.motionConstraintType = motionConstraintType;
			this.motionInterpolationType = motionInterpolationType;
		}

		public void updateInsets(Dimension fullscreenSize) {
			//pixelWidth = (int) trialSize.getWidth();
			//pixelHeight = (int) trialSize.getHeight();
			insetX = (int) (0.5*(fullscreenSize.getWidth() - pixelWidth));
			insetY = (int) (0.5*(fullscreenSize.getHeight() - pixelHeight));
		}

		public int getInsetX() {
			return insetX;
		}

		public int getInsetY() {
			return insetY;
		}

		public int getPixelWidth() {
			return pixelWidth;
		}

		public int getPixelHeight() {
			return pixelHeight;
		}

		public int getNumDistractors() {
			return numDistractors;
		}

		public double getObjectSpeed() {
			return objectSpeed;
		}

		public TrialType getTrialType() {
			return trialType;
		}

		public int getTrialCount() {
			return trialCount;
		}

		public double getTrialLength() {
			return trialLength;
		}

		public double getFPS() {
			return fps;
		}

		public long getSeed() {
			return seed;
		}

		public int getGridXSize() {
			return gridXSize;
		}

		public int getGridYSize() {
			return gridYSize;
		}

		public boolean getUsesBGImages() {
			return usesBGImages;
		}

		public String getImageDirectory() {
			return bgImageDirectory;
		}

		public MemoryCheckType getMemCheckType() {
			return memCheckType;
		}
		
		public boolean getUsesFullscreen() {
			return usesFullscreen;
		}
		
		public boolean getUsesRandomTarget() {
			return usesRandomTarget;
		}
		
		public MotionConstraintType getMotionConstraintType() { 
			return motionConstraintType;
		}
		
		public MotionInterpolationType getMotionInterpolationType() { 
			return motionInterpolationType;
		}
		
		public Stimulus getCanonicalTarget() {
			return StimulusFactory.getInstance().getRegisteredExample(StimulusType.TARGET, canonicalTarget);
		}
	}

	// Data regarding the human tester
	public class User {

		private String name;
		private Gender gender;
		private Date birthdate;
		private Date testDate;
		private String testLocation;

		public User(String name, Gender gender, Date birthdate, Date testDate, String testLocation) {
			this.name = name;
			this.gender = gender;
			this.birthdate = birthdate;
			this.testDate = testDate;
			this.testLocation = testLocation;
		}

		public String getName() {
			return name;
		}

		public Gender getGender() {
			return gender;
		}

		public Date getBirthdate() {
			return birthdate;
		}

		public Date getTestDate() {
			return testDate;
		}

		public String getTestLocation() {
			return testLocation;
		}

		public int getAge() {
			return new Period(birthdate.getTime(), testDate.getTime()).getYears();
		}
	}


	// Gender list; can be updated if need be (?!?!)
	public static enum Gender {
		MALE("Male"), FEMALE("Female"), UNKNOWN("UNKNOWN");

		private final String displayName;

		Gender(String displayName) {
			this.displayName = displayName;
		}

		public String toString() {
			return displayName;
		}

		public static Gender getGender(String query) {
			if(query.equalsIgnoreCase(MALE.toString())) {
				return MALE;
			} else if(query.equalsIgnoreCase(FEMALE.toString())) {
				return FEMALE;
			} else {
				return UNKNOWN;
			}
		}
	}

	// Test type list (all distractors look alike, all look different, etc)
	public static enum TrialType {
		ALL_SAME("All Same"), ALL_DIFF("All Different"), SAME_AS_TARGET("Same as Target"), UNKNOWN("Unknown");

		private final String displayName;

		TrialType(String displayName) {
			this.displayName = displayName;
		}

		public String toString() {
			return displayName;
		}

		public static TrialType getTrialType(String query) {
			if(query.equalsIgnoreCase(ALL_SAME.toString())) {
				return ALL_SAME;
			} else if(query.equalsIgnoreCase(ALL_DIFF.toString())) {
				return ALL_DIFF;
			} else if(query.equalsIgnoreCase(SAME_AS_TARGET.toString())) {
				return SAME_AS_TARGET;
			} else {
				return UNKNOWN;
			}
		}
	}
}
