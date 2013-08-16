package edu.cmu.cs.eyetrack.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;
import edu.cmu.cs.eyetrack.analysis.io.Loader;
import edu.cmu.cs.eyetrack.analysis.score.PointByPointScorer;
import edu.cmu.cs.eyetrack.analysis.score.Scorer;
import edu.cmu.cs.eyetrack.analysis.score.compare.DeltaOffsetPointComparator;
import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.score.distance.EuclideanDistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiData;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiEventMap;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Experiment;
import edu.cmu.cs.eyetrack.helper.Util;

/**
 * 
 * @author spook
 * Deals with data from the pilot runs of Track-It, where we pre-recorded runs of Track-It and tested subjects
 * against them on Tobii using *videos* of these pre-recorded runs.  Thus, each run of a subject is compared against
 * an overall "gold standard" .csv output from the video-recorded Track-It run.  Later, we changed this so that Track-It
 * ran live in Tobii, so each run plays against its own gold standard (see, e.g., AnalysisTester.java)
 */
public class AnalysisTesterDec2011 {


	public static void main(String args[]) {

		//
		// Sets paths for our dev systems based on Windows/Linux
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
		String subjRootDir, goldPath;
		if(isWindows) {
			subjRootDir = "C:\\sideprojects\\TrackIt\\experiments\\real_run_data\\";
			goldPath = "C:\\sideprojects\\TrackIt\\experiments\\gold_standard\\all_same_test1.csv";
			Util.dPrintln("Detected Windows-based operating system");
		} else {
			subjRootDir = "/home/spook/sideprojects/TrackIt/experiments/real_run_data/";
			goldPath = "/home/spook/sideprojects/TrackIt/experiments/gold_standard/all_same_test1.csv";
			Util.dPrintln("Detected *nix-based operating system");
		}



		//
		// Load the actual trajectories for each object in this task, taken straight from Track-It
		Experiment goldExperiment = null;
		try {
			Util.dPrintln("Attempting to load gold standard data from " + goldPath);
			goldExperiment = Loader.loadGoldStandard(new File(goldPath));
		} catch(IOException e) {
			System.err.println("IO Error loading gold standard file " + goldPath);
			e.printStackTrace();
			return;
		}

		Util.dPrintln("Loaded " + goldPath);
		Util.dPrintln("Found " + goldExperiment.getNumTrials() + " trials total.");
		
		

		// Start traversal at root.  This root is organized as follows:
		// root -> {Subject1 -> {tobii1, tobbi2, ..}, Subject2 -> {tobii1, tobii2, ..}, ..}
		File rootDir = new File(subjRootDir);
		if(!rootDir.isDirectory()) {
			Util.dPrintln("Need root directory for files; path not directory: " + subjRootDir);
			return;
		}


		PilotOutputRecord recorder = new PilotOutputRecord();
		
		for(File subjectDir : rootDir.listFiles()) {

			// Skip any non-directory files sitting around at the subject directory level
			if(!subjectDir.isDirectory()) {
				Util.dPrintln(subjectDir.getAbsolutePath() + " is not a directory; skipping.");
				continue;
			}

			String subjectID = subjectDir.getName();
			
			for(String subjEventPath : subjectDir.list()) {
				
				// Ignore non-Tobii files
				if(!subjEventPath.endsWith(".tsv")) {
					continue;
				}
				
				// Ignore non-Tobii Event-Map files
				int eventDataIndex = subjEventPath.lastIndexOf("-Event-Data");
				if(eventDataIndex < 0) {
					continue;
				}
				
				// If the file is a Tobii Event Map file, find its matching
				// All Data file and move on to loading both
				String allDataPrefix = subjEventPath.substring(0, eventDataIndex) + "-All-Data";
				String subjDataPath = null;
				for(String candSubjDataPath : subjectDir.list()) {
					if(candSubjDataPath.startsWith(allDataPrefix)) {
						subjDataPath = candSubjDataPath;
						break;
					}
				}
				
				// If we couldn't find a match to this Event Data, tell the user and skip
				if(subjDataPath == null) {
					Util.dPrintln("Couldn't find a match to expected Event Data file " + subjEventPath);
					return;
				}
		
				// Try to figure out what type of trial this is (ALL DIFF A, ALL SAME B, etc)
				PilotOutputRecord.TRIAL_TYPE trialType = PilotOutputRecord.TRIAL_TYPE.inferTrialType(subjEventPath);
				if(trialType.equals(PilotOutputRecord.TRIAL_TYPE.UNKNOWN)) {
					Util.dPrintln("Couldn't discern what type of trial (e.g., ALL DIFF A, ALL SAME B) this was from the Event Data file " + subjEventPath);
					return;
				} else {
					Util.dPrintln("I think I've found trial type " + trialType + " from filename " + subjEventPath);
				}
				
				
				File subjEventFile = new File(subjectDir, subjEventPath);
				File subjDataFile = new File(subjectDir, subjDataPath);
				
				
				
				//
				// Grab the overview event data for a single user
				TobiiEventMap eventMap = null;
				try {
					Util.dPrintln("Attempting to load trial data from subject path " + subjEventPath);
					eventMap = Loader.loadTobiiEvent2(subjEventFile);
				} catch(IOException e) {
					System.err.println("IO Error loading event data file " + subjEventPath);
					e.printStackTrace();
					return;
				}

				Util.dPrintln("Loaded event data from file " + subjEventPath);


				//
				// Grab the overview event data for a single user
				TobiiData subjectData = null;
				try {
					Util.dPrintln("Attempting to load Tobii data chunk from " + subjDataPath);
					subjectData = Loader.loadTobiiData2(subjDataFile, eventMap);
				} catch(IOException e) {
					System.err.println("Error loading Tobii data chunk from " + subjDataPath);
					e.printStackTrace();
					return;
				}

				Util.dPrintln("Loaded Tobii data chunk from " + subjDataPath);


				//
				// Score the subject's curves against the gold standard
				Util.dPrintln("Comparing subject's trajectory against gold standard.");

				long offset = 500;   // in ms
				PointComparator f = new DeltaOffsetPointComparator(offset);
				
				double exponent = 2.0;
				DistanceFunction dist = new EuclideanDistanceFunction();
				//DistanceFunction dist = new FalloffDistanceFunction(exponent);
				Scorer scorer = new PointByPointScorer(f, dist);

				// Record ALL score data in separate files, if desired
				boolean recordEverything = false;
				Map<Integer, List<Double>> recordMap = null;
				if(recordEverything) {
					recordMap = new HashMap<Integer, List<Double>>();
				}
				
				double lookThreshold = 0.0;
				Map<Integer, SingleRunScore> scores = scorer.scoreSubject(goldExperiment, subjectData, recordMap, lookThreshold);
				
				// Possibly record every single score in a separate file
				if(recordEverything) {
					try {
						for(Integer trialID : scores.keySet()) {
							CSVWriter writer = new CSVWriter(new FileWriter(new File("exact_" + trialType + "_" + subjectID + "_" + trialID + "_" + System.currentTimeMillis() + ".csv")));
							for(Double score : recordMap.get(trialID)) {
								if(score == null) {
									// For now, record invalid looks as negative score
									//score = -1.0;
									continue;
								}
								writer.writeNext(new String[] {String.valueOf(score)});
							}
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				
				
				// Record the subject's score in our map of scores, for charting later
				recorder.addHeader("offset", offset + " ms");
				recorder.addHeader("exponent", String.valueOf(exponent));
				recorder.addHeader("comparator function", f.getClass().getName());
				recorder.addHeader("distance function", dist.getClass().getName());
				recorder.addHeader("scorer", scorer.getClass().getName());
				recorder.addScore(trialType, subjectID, scores);
				
				
			} // end of individual subject's listing loop
		} // end of root directory listing loop
		
		
		// Write all our records to a .csv file
		try {
			recorder.writeToCSV("eyetrack_" + System.currentTimeMillis() + ".csv");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		
		return;
	}
}
