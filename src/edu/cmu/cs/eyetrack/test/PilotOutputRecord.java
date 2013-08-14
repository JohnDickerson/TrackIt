package edu.cmu.cs.eyetrack.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.cs.eyetrack.helper.Util;

import au.com.bytecode.opencsv.CSVWriter;

public class PilotOutputRecord {

	/**
	 * 
	 * @author Spook
	 * Hacky class made to parse data for the pilot runs in late 2011.
	 */

	public static enum TRIAL_TYPE {ALL_SAME_A, ALL_SAME_B, ALL_DIFF_A, ALL_DIFF_B, UNKNOWN;

	public static TRIAL_TYPE inferTrialType(String rawFileName) {
		String raw = rawFileName.toUpperCase();
		if(raw.contains("DIFF_B") || raw.contains("DIFF B") || raw.contains("ALLDIFF_02")) {
			return ALL_DIFF_B;
		} else if(raw.contains("DIFF_A") || raw.contains("DIFF A") || raw.contains("ALLDIFF_01")) {
			return ALL_DIFF_A;
		} else if(raw.contains("SAME_B") || raw.contains("SAME B") || raw.contains("ALLSAME_02")) {
			return ALL_SAME_B;
		} else if(raw.contains("SAME_A") || raw.contains("SAME A") || raw.contains("ALLSAME_01")) {
			return ALL_SAME_A;
		} else {
			return UNKNOWN;
		}
	}
	};

	// Random collection of strings->value pairs for header
	private SortedMap<String, String> headerMap;
	
	// Use SortedMaps all the way down so we can preserve order when printing out results
	private SortedMap<TRIAL_TYPE, SortedMap<String, SortedMap<Integer, SingleRunScore>>> dataMap;
	private SortedMap<TRIAL_TYPE, SortedSet<Integer>> seenTrialIDsMap;
	
	public PilotOutputRecord() {
	
		headerMap = new TreeMap<String, String>();
		
		dataMap = new TreeMap<TRIAL_TYPE, SortedMap<String, SortedMap<Integer, SingleRunScore>>>();
		seenTrialIDsMap = new TreeMap<TRIAL_TYPE, SortedSet<Integer>>();
		for(TRIAL_TYPE t : TRIAL_TYPE.values()) {
			dataMap.put(t, new TreeMap<String, SortedMap<Integer, SingleRunScore>>());
			seenTrialIDsMap.put(t, new TreeSet<Integer>());
		}
	
		
	}
	
	public void addHeader(String key, String value) {
		headerMap.put(key, value);
	}
	
	public void addScore(TRIAL_TYPE trialType, String subjectID, Integer trialID, SingleRunScore record) {
		
		SortedMap<String, SortedMap<Integer, SingleRunScore>> trialMap = dataMap.get(trialType);
		
		// Initialize if we've never seen this subject for this trial type
		if(!trialMap.containsKey(subjectID)) {
			trialMap.put(subjectID, new TreeMap<Integer, SingleRunScore>());
		}
		
		// Add data for this specific trial of the specified trial type
		trialMap.get(subjectID).put(trialID, record);
		
		// Say that we've seen at least one of this trial ID for this specific trial type
		seenTrialIDsMap.get(trialType).add(trialID);
		
	}
	
	public void addScore(TRIAL_TYPE trialType, String subjectID, Map<Integer, SingleRunScore> scoreMap) {
		if(scoreMap == null) { return; }
		for(Integer trialID : scoreMap.keySet()) {
			addScore(trialType, subjectID, trialID, scoreMap.get(trialID));
		}
	}
	
	public void fakeMissedTobiiTests(String subjectID) {
		// For all known Trial types, if this Subject doesn't have a score for that trial type, fill it in with a nil
		for( TRIAL_TYPE potential_miss : dataMap.keySet()) {
			
			// If we've never seen this Subject for this Trial Type ...
			if(null == dataMap.get(potential_miss).get(subjectID)) {
				
				// ... add in NaN-scores for each Trial ID we've ever seen.
				dataMap.get(potential_miss).put(subjectID, new TreeMap<Integer, SingleRunScore>());
				for( int trialID : seenTrialIDsMap.get(potential_miss)){ 
					dataMap.get(potential_miss).get(subjectID).put(trialID, new SingleRunScore().fakeTheScore());
				}
			}
		}
	}
	
	
	public void writeToCSV(String filename) throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(new File(filename)), ',');
		
		// Print overall header data at the top of the file
		for(String header : headerMap.keySet()) {
			writer.writeNext(new String[] {header, headerMap.get(header)});
		}
		writer.writeNext(new String[] {""});
		writer.flush();
		
		// Write the meaty data
		// For each overall trial type (e.g., ALL DIFF A, ALL SAME B, ...) ...
		for(TRIAL_TYPE trialType : dataMap.keySet()) {
			
			writer.writeNext(new String[] {trialType.toString()});
			
			// Add Header for this trial type
			SortedSet<Integer> trialIDSet = seenTrialIDsMap.get(trialType);
			int numCSVColumns = 1 + trialIDSet.size() + 1 + trialIDSet.size();
			String[] header = new String[numCSVColumns];
			int idx=0;
			header[idx++] = "Subject ID";
			// First columns correspond to the scores
			for(Integer trialID : trialIDSet) {
				header[idx++] = "Trial " + trialID;
			}
			header[idx++] = "";
			// Second columns correspond to time on task
			for(Integer trialID : trialIDSet) {
				header[idx++] = "Trial " + trialID;
			}
			writer.writeNext(header);
			
			// Write data for this trial type, one row per subject ID
			// TODO: map trial IDs to proper header's column; only an issue if we're missing data
			
			SortedMap<String, SortedMap<Integer, SingleRunScore>> trialMap = dataMap.get(trialType);
			for(String subjectID : trialMap.keySet()) {
				
				SortedMap<Integer, SingleRunScore> records = trialMap.get(subjectID);
				
				// If we don't have all data for this subject, skip (for now)
				if(records.size() != trialIDSet.size()) {
					Util.dPrintln("Skipping trial type " + trialType + ", subject " + subjectID + " because of incomplete data (" + records.size() + " / " + trialIDSet.size() + " possible trials found)");
					continue;
				}
				
				// Otherwise, write a row of data for this subject and all of his trials' scores
				String[] dataRow = new String[numCSVColumns];
				idx=0;
				dataRow[idx++] = subjectID;
				for(Integer trialID : records.keySet()) {
					dataRow[idx++] = String.valueOf( records.get(trialID).getScore() );
				}
				// And also the time-on-task for this subject across all trials
				dataRow[idx++] = "";
				for(Integer trialID : records.keySet()) {
					double totalTaskLen = records.get(trialID).getFrameCountOverall();
					double onTargetLen = records.get(trialID).getFrameCountOnTarget();
					double timeOnTask = totalTaskLen > 0 ? onTargetLen/totalTaskLen : -1;
					dataRow[idx++] = String.valueOf(timeOnTask);
				}
				writer.writeNext(dataRow);
				
			}
			
			writer.flush();
			writer.writeNext(new String[] {""});
			writer.writeNext(new String[] {""});
			
		}
	}
	
}
