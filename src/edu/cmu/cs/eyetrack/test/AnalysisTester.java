package edu.cmu.cs.eyetrack.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVWriter;
import edu.cmu.cs.eyetrack.analysis.io.Loader;
import edu.cmu.cs.eyetrack.analysis.score.FixationPointScorer;
import edu.cmu.cs.eyetrack.analysis.score.Scorer;
import edu.cmu.cs.eyetrack.analysis.score.compare.DeltaOffsetPointComparator;
import edu.cmu.cs.eyetrack.analysis.score.compare.PointComparator;
import edu.cmu.cs.eyetrack.analysis.score.distance.DistanceFunction;
import edu.cmu.cs.eyetrack.analysis.score.distance.EuclideanDistanceFunction;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiData;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiEventMap;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Experiment;
import edu.cmu.cs.eyetrack.helper.Util;

public class AnalysisTester {


	public static void main(String args[]) {

		//
		// Sets paths for our dev systems based on Windows/Linux
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
		boolean isMacOsX = System.getProperty("os.name").toLowerCase().contains("mac os x");
		String subjRootDir;
		if(isWindows) {
			subjRootDir = "C:\\sideprojects\\TrackIt\\experiments\\feb2012\\";
			Util.dPrintln("Detected Windows-based operating system");
		} else if(isMacOsX) {
			subjRootDir = "/Users/spook/code/TrackIt/sample/sample_data_August_2013";
			Util.dPrintln("Detected Mac OS X as operating system");
		} else {
			//subjRootDir = "/home/spook/sideprojects/TrackIt/experiments/feb2012/";
			subjRootDir = "/usr0/home/jpdicker/Dropbox/organized_DirectTrackIt Files";
			//subjRootDir = "/usr0/home/jpdicker/code/TrackIt/sample/sample_data_August_2013";
			Util.dPrintln("Detected *nix-based operating system");
		}



		// Start traversal at root.  This root is organized as follows:
		// root -> {Subject1 -> {tobii1, tobbi2, ..}, Subject2 -> {tobii1, tobii2, ..}, ..}
		File rootDir = new File(subjRootDir);
		if(!rootDir.isDirectory()) {
			Util.dPrintln("Need root directory for files; path not directory: " + subjRootDir);
			return;
		}


		// Lets the user loop through different time offsets
		Vector<Long> timeOffsets = new Vector<Long>();
		for(Long offset=0L; offset<=0L; offset+=100L) {
			timeOffsets.add(offset);
		}


		for(Long offset : timeOffsets) {
			
			PilotOutputRecord recorder = new PilotOutputRecord();

			for(File subjectDir : rootDir.listFiles()) {

				// Skip any non-directory files sitting around at the subject directory level
				if(!subjectDir.isDirectory()) {
					Util.dPrintln(subjectDir.getAbsolutePath() + " is not a directory; skipping.");
					continue;
				}

				String subjectID = subjectDir.getName();

				// First, load the "gold standards" (e.g., the .csv files) representing the actual trajectories of the
				// distractors and targets from Track-It
				//
				// Load the actual trajectories for each object in this task, taken straight from Track-It
				Map<String, Experiment> goldStandards = new HashMap<String, Experiment>();

				for(String goldPath : subjectDir.list()) {

					// Ignore non-Track-It files
					if(!goldPath.endsWith(".csv")) {
						continue;
					}

					Experiment goldExperiment = null;
					try {
						Util.dPrintln("Attempting to load gold standard data from " + goldPath);
						goldExperiment = Loader.loadGoldStandard(new File(subjectDir, goldPath));
					} catch(IOException e) {
						System.err.println("IO Error loading gold standard file " + goldPath);
						e.printStackTrace();
						return;
					}

					Util.dPrintln("Loaded " + goldPath);
					Util.dPrintln("Found " + goldExperiment.getNumTrials() + " trials total.");

					// Map the identifier (filename except .csv) to the Experiment
					String goldID = goldPath.substring(0, goldPath.lastIndexOf('.')).toUpperCase();
					goldStandards.put(goldID, goldExperiment);
				}



				// Second, go through each Track-It file in the directory and compare it against the proper gold
				// standard that has (hopefully) been loaded and mapped in the goldStandards HashMap.  Unlike with the old
				// version, we'll need to compare timestamps in Track-It to timestamps in Tobii to synchronize the data
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

					// Now try to match the Tobii data file to the pre-loaded gold standard file from Track-It\
					// We assume that a file named   02_MackJ_AllSame_01_02-All-Data.tsv  matches with a file named
					//                               02_MackJ_AllSame_01_02.csv           from Track-It.
					String goldID = subjEventPath.substring(0, subjEventPath.indexOf('-')).toUpperCase();
					if(!goldStandards.containsKey(goldID)) {
						Util.dPrintln("Couldn't find a matching Track-It file for Tobii file " + subjEventPath.toUpperCase() + "; we expected " + goldID + ".csv");
						return;
					}
					Experiment goldExperiment = goldStandards.get(goldID);


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
						eventMap = Loader.loadTobiiEvent3(subjEventFile);
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
						subjectData = Loader.loadTobiiData3(subjDataFile, eventMap);
					} catch(IOException e) {
						System.err.println("Error loading Tobii data chunk from " + subjDataPath);
						e.printStackTrace();
						return;
					}

					Util.dPrintln("Loaded Tobii data chunk from " + subjDataPath);


					//
					// Score the subject's curves against the gold standard
					Util.dPrintln("Comparing subject's trajectory against gold standard.");

					//long offset = 000;   // in ms
					PointComparator f = new DeltaOffsetPointComparator(offset);

					double exponent = 2.0;
					DistanceFunction dist = new EuclideanDistanceFunction();
					//DistanceFunction dist = new FalloffDistanceFunction(exponent);
					Scorer scorer = new FixationPointScorer(f, dist);

					// Record ALL score data in separate files, if desired
					boolean recordEverything = false;
					Map<Integer, List<Double>> recordMap = null;
					if(recordEverything) {
						recordMap = new HashMap<Integer, List<Double>>();
					}

					// Requires that a subject's eyes be tracked by Tobii for at least X% of the time; else, score = NaN
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
										Util.dPrintln("Null score for subject " + subjectID + " on trial " + trialID);
										continue;
									}
									writer.writeNext(new String[] {String.valueOf(score)});
								}
								writer.close();
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
					}


					// Record the subject's score in our map of scores, for charting later
					recorder.addHeader("scorer", scorer.getClass().getName());
					recorder.addHeader("offset", offset + " ms");
					recorder.addHeader("exponent", String.valueOf(exponent));
					recorder.addHeader("comparator function", f.getClass().getName());
					recorder.addHeader("distance function", dist.getClass().getName());
					recorder.addHeader("scorer", scorer.getClass().getName());
					recorder.addHeader("lookThreshold", String.valueOf(lookThreshold));
					
					recorder.addScore(trialType, subjectID, scores);


				} // end of individual subject's listing loop

				// For printing purposes, make sure we fill in any Tobii files that were missing with NaN scores
				recorder.fakeMissedTobiiTests(subjectID);

			} // end of root directory listing loop


			// Write all our records to a .csv file
			try {
				recorder.writeToCSV("eyetrack_offset_" + offset.toString() + "_" + System.currentTimeMillis() + ".csv");
			} catch(IOException e) {
				e.printStackTrace();
			}

		}
		return;
	}
}
