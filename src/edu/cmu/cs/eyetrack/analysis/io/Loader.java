package edu.cmu.cs.eyetrack.analysis.io;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import edu.cmu.cs.eyetrack.analysis.struct.Trajectory;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiEventMap;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiFrame;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiData;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiHeader;
import edu.cmu.cs.eyetrack.analysis.struct.tobii.TobiiEventMap.TobiiEventType;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Experiment;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.TrackItFrame;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Trial;
import edu.cmu.cs.eyetrack.analysis.struct.trackit.Experiment.TrialType;
import edu.cmu.cs.eyetrack.helper.Coordinate;

public class Loader {

	/**
	 * Reads and stores header information for a Tobii file (either the Events
	 * or full Data file).  The CSVReader parameter will be advanced past the 
	 * the location of the header upon completion
	 * @param reader a CSVReader with an open connection to a Tobii file
	 * @return TobiiHeader containing header info for reader's Tobii file
	 * @throws IOException
	 */
	private static TobiiHeader readHeader(CSVReader reader) throws IOException {
		
		TobiiHeader header = new TobiiHeader();
		
		//
		// SYSTEM PROPERTIES section
		String line[] = reader.readNext();
		assert(line[0].equalsIgnoreCase("System Properties:"));
		
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Operating System:"));
		header.setSysOS(line[1]);
		
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("System User Name:"));
		header.setSysUser(line[1]);
		
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Machine Name:"));
		header.setSysMachine(line[1]);
							
		//
		// DATA PROPERTIES section
		reader.readNext();
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Data Properties:"));
		
		//
		// RECORDING section
		reader.readNext();
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Recording name:"));
		if(!line[1].isEmpty()) {
			header.setRecName(line[1]);
		} else if(!line[2].isEmpty()) {
			header.setRecName(line[2]);	
		}
		
		// Date and Time of the recording in Tobii.  We combine these into one Date
		// Formatted as, e.g., "6/24/2011" and "11:42:32 AM"
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Recording date:"));
		String recDateStr = line[1].trim();
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Recording time:"));
		String recTimeStr = line[1].trim();
		
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		try {
			Date recDateTime = dateFormat.parse(recDateStr + " " + recTimeStr);
			header.setRecDateTime(recDateTime);
			header.setRecDateTimestamp(recDateTime.getTime());
		} catch(ParseException e) {
			System.err.println("Had trouble parsing the date/times in a Tobii header file.  Non-fatal.");
			System.err.println("Date string: " + recDateStr + "\t\tTime string: " + recTimeStr);
		}
		
		
		// Resolution at which the Tobii experiment was recorded
		// Formatted as string, e.g., "1024 x 768"
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Recording resolution:"));
		String[] recResolutionStr = line[1].trim().split(" ");
		int width = Double.valueOf(recResolutionStr[0]).intValue();
		int height = Double.valueOf(recResolutionStr[2]).intValue();
		header.setRecResolution(new Dimension(width, height));
		
		// Newer version of Tobii also reports screen size in millimeters;
		// older versions skip straight to the export section (added Feb. 2012)
		line = reader.readNext();
		if(line.length > 1) {
			assert(line[0].equalsIgnoreCase("Recording screen size in millimeter:"));
			String[] screenSizeStr = line[1].trim().split(" ");
			double widthMM = Double.valueOf(screenSizeStr[0]).doubleValue();
			double heightMM = Double.valueOf(screenSizeStr[2]).doubleValue();
			header.setScreenSizeMM(new Rectangle2D.Double(0,0,widthMM, heightMM));
			reader.readNext();
		}
		
		//
		// EXPORT section
		
		// Date and Time of the recording in Tobii.  We combine these into one Date
		// Formatted as, e.g., "6/24/2011" and "11:42:32 AM"
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Export date:"));
		String exportDateStr = line[1].trim();
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Export time:"));
		String exportTimeStr = line[1].trim();
		
		try {
			Date exportDateTime = dateFormat.parse(exportDateStr + " " + exportTimeStr);
			header.setExportDateTime(exportDateTime);
		} catch(ParseException e) {
			System.err.println("Had trouble parsing the date/times in a Tobii header file.  Non-fatal.");
			System.err.println("Date string: " + exportDateStr + "\t\tTime string: " + exportTimeStr);
		}
		
		//
		// PARTICIPANT section
		reader.readNext();
		line = reader.readNext();
		assert(line[0].equalsIgnoreCase("Participant:"));
		header.setParticipant(line[1]);
		
		return header;
	}
	
	/**
	 * Loads Event Data map for a Tobii v2.x file (back when we did Track-It with videos, not real-time)
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static TobiiEventMap loadTobiiEvent2(File file) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(file), '\t');
		String[] line;
		
		// Get header data for this file
		TobiiHeader header = readHeader(reader);
		
		TobiiEventMap eventMap = new TobiiEventMap(header);
		
		// Work our way down to the info we care about---timestamped data
		while( (line = reader.readNext()) != null && !line[0].trim().equalsIgnoreCase("timestamp") );
		
		if(!line[0].trim().equalsIgnoreCase("timestamp")) {
			throw new IOException("Couldn't find the Timestamp column in Tobii file " + file.getAbsolutePath());
		}
		
		// Read data in about events and their timestamps, until the end of the file
		while( (line = reader.readNext()) != null ) {
			long timestamp = Double.valueOf(line[0]).longValue();
			String eventStr = line[1].trim();
			String descriptor = line[5].trim();
			
			// Transform Tobii event string into an enum
			TobiiEventType type = TobiiEventType.parseTobii(eventStr);
			if(type == null) {
				System.err.println("Could not understand event type " + eventStr + " in Tobii file " + file.getAbsolutePath());
				continue;
			}
			
			// Hacky way to grab the corresponding Trial ID # for Track-It from the filename
			// of the movie used in Tobii.  Need to get Psych people to formalize naming schema
			// Assume Trial ID is the first integer found in the filename
			int trialId = -1;
			if(type == TobiiEventType.MOVIE_START || type == TobiiEventType.MOVIE_END) {
				Pattern findInt = Pattern.compile("\\d+");
				Matcher matcher = findInt.matcher(descriptor);
				if(!matcher.find()) {
					throw new IOException("Couldn't identify the Trial ID in " + descriptor + " in file " + file.getAbsolutePath());
				}
				String firstInteger = matcher.group();
				
				trialId = Integer.parseInt(firstInteger);
				System.out.println(descriptor + " found Trial " + trialId);
			}
			
			eventMap.addEvent(type, timestamp, trialId);
		}
		
		
		
		reader.close();
		return eventMap;
	}
	
	
	/**
	 * Loads Event Data map for a Tobii v3.x file (real-time Track-It and Tobii)
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static TobiiEventMap loadTobiiEvent3(File file) throws IOException {

		CSVReader reader = new CSVReader(new FileReader(file), '\t');
		String[] line;
		
		// Get header data for this file
		TobiiHeader header = readHeader(reader);
		
		TobiiEventMap eventMap = new TobiiEventMap(header);
		
		// Work our way down to the info we care about---timestamped data
		while( (line = reader.readNext()) != null && !line[0].trim().equalsIgnoreCase("timestamp") );
		
		if(!line[0].trim().equalsIgnoreCase("timestamp")) {
			throw new IOException("Couldn't find the Timestamp column in Tobii file " + file.getAbsolutePath());
		}
		
		long initialTimestamp = header.getRecDateTimestamp();
		
		// Read data in about events and their timestamps, until the end of the file
		int trialId = 0;
		while( (line = reader.readNext()) != null ) {
			
			// Timestamp is reported relative to absolute wallclock timestamp; must add this on
			long timestamp = Double.valueOf(line[0]).longValue();
			timestamp += initialTimestamp;
			
			String eventStr = line[1].trim();
			String descriptor = line[5].trim();
			
			// Transform Tobii event string into an enum
			TobiiEventType type = TobiiEventType.parseTobii(eventStr);
			if(type == null) {
				System.err.println("Could not understand event type " + eventStr + " in Tobii file " + file.getAbsolutePath());
				continue;
			}
			
			// Hacky way to grab the corresponding Trial ID # for Track-It from the filename
			// of the movie used in Tobii.  Basically, every time the experimenter presses the Spacebar,
			// we assume that a new movie has started (corresponding to Frame 0)
			if(type == TobiiEventType.KEYPRESS && descriptor.equalsIgnoreCase("Space")) {
				trialId += 1;
				System.out.println(descriptor + " found Trial " + trialId);
			}
			
			eventMap.addEvent(type, timestamp, trialId, descriptor);
		}
		
		
		
		reader.close();
		return eventMap;
	}
	
	
	/**
	 * Loads data from the raw Tobii 2.x-generated experiment file (back when we did Track-It with videos, not real-time)
	 * @param path Relative or absolute path to the .csv/.tsv file exported from Tobii
	 * @param eventMap 
	 * @throws IOException If we have any sort of I/O issue (from not finding the file
	 * 		to an actual issue parsing the data), throw an IOException with error message.
	 */
	public static TobiiData loadTobiiData2(File file, TobiiEventMap eventMap) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(file), '\t');
	
		// Get header data for this file
		TobiiHeader header = readHeader(reader);
		
		// Make sure this header matches with the header of the EventMap
		if(!header.isCompatible(eventMap.getHeader())) {
			throw new IOException("Headers don't match in file " + file.getAbsolutePath());
		}
		
		TobiiData data = new TobiiData(eventMap);
		String line[];
		
		// Grab the rest of the header info specific to a Tobii data dump
		//line[] = reader.readNext();
		
		// Work our way down to the info we care about---timestamped data
		while( (line = reader.readNext()) != null && !line[0].trim().equalsIgnoreCase("timestamp") );
		
		if(!line[0].trim().equalsIgnoreCase("timestamp")) {
			throw new IOException("Couldn't find the Timestamp column in Tobii file " + file.getAbsolutePath());
		}
		
		
		
		// Only care about the eye tracking data for when we are watching a movie
		for(int eIdx=0; eIdx<eventMap.getEvents(TobiiEventType.MOVIE_START).size(); eIdx++) {
			
			long startTime = eventMap.getEvents(TobiiEventType.MOVIE_START).get(eIdx).getTimestamp();
			long stopTime = eventMap.getEvents(TobiiEventType.MOVIE_END).get(eIdx).getTimestamp();
			int trialId = eventMap.getEvents(TobiiEventType.MOVIE_START).get(eIdx).getTrialId();
			
			// Find the start of this event
			while( (line = reader.readNext()) != null && Long.valueOf(line[0]) != startTime );
			if(line == null) { break; }
			
			// Parse data in (start of event, end of event) non-inclusive
			Trajectory<TobiiFrame> trajectory = new Trajectory<TobiiFrame>();
			while( (line = reader.readNext()) != null && Long.valueOf(line[0]) != stopTime) {
				
				// Skip any events that weren't eye tracking
				if(line[1].isEmpty()) {
					continue;
				}
				
				// Adjust timestamp so that the first frame of each trial is timestamp = 0000
				long timestamp = Long.valueOf(line[0]) - startTime;
				
				TobiiFrame f = new TobiiFrame();
				f.setGazeLeftPt(new Coordinate<Double>( Double.valueOf(line[4]), Double.valueOf(line[5])));
				f.setValidityLeft(Integer.valueOf(line[10]));
				f.setGazeRightPt(new Coordinate<Double>( Double.valueOf(line[11]), Double.valueOf(line[12])));
				f.setValidityRight(Integer.valueOf(line[17]));
				f.setFixationPt(new Coordinate<Double>( Double.valueOf(line[19]), Double.valueOf(line[20])));
				f.setGazePt(new Coordinate<Double>( Double.valueOf(line[38]), Double.valueOf(line[39]) ));
				f.setTimestamp(timestamp);	
				
				trajectory.registerFrame(f);
			}
			
			data.registerTrajectory(trialId, trajectory);
		}
		
		
		reader.close();
		return data;
	}
	
	
	/**
	 * Loads data from the raw Tobii 3.x-generated experiment file (real-time Track-It and Tobii)
	 * @param path Relative or absolute path to the .csv/.tsv file exported from Tobii
	 * @param eventMap 
	 * @throws IOException If we have any sort of I/O issue (from not finding the file
	 * 		to an actual issue parsing the data), throw an IOException with error message.
	 */
	public static TobiiData loadTobiiData3(File file, TobiiEventMap eventMap) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(file), '\t');
	
		// Get header data for this file
		TobiiHeader header = readHeader(reader);
		
		// Make sure this header matches with the header of the EventMap
		if(!header.isCompatible(eventMap.getHeader())) {
			throw new IOException("Headers don't match in file " + file.getAbsolutePath());
		}
		
		TobiiData data = new TobiiData(eventMap);
		String line[];
		
		// Grab the rest of the header info specific to a Tobii data dump
		//line[] = reader.readNext();
		
		// Work our way down to the info we care about---timestamped data
		while( (line = reader.readNext()) != null && !line[0].trim().equalsIgnoreCase("timestamp") );
		
		if(!line[0].trim().equalsIgnoreCase("timestamp")) {
			throw new IOException("Couldn't find the Timestamp column in Tobii file " + file.getAbsolutePath());
		}
		
		long recStartTime = header.getRecDateTimestamp();
		
		// Only care about the eye tracking data for when Track-It is started.  This is always right after the Space bar is hit
		for(int eIdx=0; eIdx<eventMap.getEvents(TobiiEventType.KEYPRESS).size(); eIdx++) {
			
			// We only care about KeyPresses that are "Space", signalling the beginning of a Track-It run
			if( !eventMap.getEvents(TobiiEventType.KEYPRESS).get(eIdx).getDescriptor().equalsIgnoreCase("Space") ) {
				continue;
			}
			
			long startTime = eventMap.getEvents(TobiiEventType.KEYPRESS).get(eIdx).getTimestamp();
			int trialId = eventMap.getEvents(TobiiEventType.KEYPRESS).get(eIdx).getTrialId();
		
			// Find next spacebar press to figure out end of this trial
			long stopTime = Long.MAX_VALUE;
			for(int sbIdx=eIdx+1; sbIdx<eventMap.getEvents(TobiiEventType.KEYPRESS).size(); sbIdx++) {
				if(eventMap.getEvents(TobiiEventType.KEYPRESS).get(eIdx).getDescriptor().equalsIgnoreCase("Space")) {
					stopTime = eventMap.getEvents(TobiiEventType.KEYPRESS).get(sbIdx).getTimestamp();
					break;
				}
			}
			
			// In the event that we stop recording before pressing other keys, get a hard upper bound on stop time
			if(stopTime == Long.MAX_VALUE) {
				stopTime = eventMap.getEvents(TobiiEventType.SCREEN_REC_STOPPED).get(0).getTimestamp();
			}
			
			System.out.println("Start: " + (startTime-recStartTime) + "[" + startTime + "], Stop: " + (stopTime-recStartTime) + "[" + stopTime + "]");
			
			// Find the start of this event
			while( (line = reader.readNext()) != null && Long.valueOf(line[0]) <= (startTime-recStartTime) );
			if(line == null) { break; }
			
			// Parse data in (start of event, end of event) non-inclusive
			// SOME of this data (at the end) won't pertain to the actual tracking portion of the Track-It; rather,
			// it'll show the grid selection or memory check.  We handle this by comparing against the gold standard only
			Trajectory<TobiiFrame> trajectory = new Trajectory<TobiiFrame>(startTime);
			trajectory.setLengthMS(stopTime - startTime);
			while( (line = reader.readNext()) != null && ( Long.valueOf(line[0]) < stopTime-recStartTime) ) {
				
				// Skip any events that weren't eye tracking
				if(line[1].isEmpty() || line[3].isEmpty()) {
					continue;
				}
				
				// Translate relative timestamp into absolute wall-clock timestamp
				long timestamp = Long.valueOf(line[0]) + recStartTime;
				
				TobiiFrame f = new TobiiFrame();
				f.setGazeLeftPt(new Coordinate<Double>( Double.valueOf(line[4]), Double.valueOf(line[5])));
				f.setValidityLeft(Integer.valueOf(line[10]));
				f.setGazeRightPt(new Coordinate<Double>( Double.valueOf(line[11]), Double.valueOf(line[12])));
				f.setValidityRight(Integer.valueOf(line[17]));
				f.setFixationPt(new Coordinate<Double>( Double.valueOf(line[19]), Double.valueOf(line[20])));
				f.setGazePt(new Coordinate<Double>( Double.valueOf(line[38]), Double.valueOf(line[39]) ));
				f.setTimestamp(timestamp);	
				
				trajectory.registerFrame(f);
			}
			
			data.registerTrajectory(trialId, trajectory);
		}
		
		
		reader.close();
		return data;
	}
	
	
	
	
	/**
	 * Loads data from a Track-It-generated .csv file; returns an Experiment representing
	 * all the information stored in this file.
	 * @param path Relative or absolute path to the .csv file containing the data
	 * 		from a gold standard run of Track-It
	 * @return An Experiment representing all necessary data (headers, positions of
	 * 		stimuli, etc) to reconstruct the full Track-It experiment
	 * @throws IOException If we have any sort of I/O issue (from not finding the file
	 * 		to an actual issue parsing the data), throw an IOException with error message.
	 */
	public static Experiment loadGoldStandard(File file) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(file), ',');
		
		String[] headers;
		String[] data;
		
		Experiment exp = new Experiment();
		
		// Get full-experiment information (# distractors, lengths, etc)
		if((headers = reader.readNext()) != null && (data = reader.readNext()) != null) {
			for(int idx=0; idx<headers.length; idx++) {
				
				// Grab the header and its associated data (e.g., "Object Speed" and "500")
				String header = headers[idx].trim();
				String datum = data[idx].trim();
				
				// Store the information associated with headers we care about
				if(header.equalsIgnoreCase("number of distractors")) {
					exp.setNumDistractors( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("object speed")) {
					exp.setObjectSpeed( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("trial type")) {
					if( datum.equalsIgnoreCase("all same") ) {
						exp.setType(TrialType.ALL_SAME);
					} else if( datum.equalsIgnoreCase("all different") ) {
						exp.setType(TrialType.ALL_SAME);
					} else if( datum.equalsIgnoreCase("same as target") ) {
						exp.setType(TrialType.SAME_AS_TARGET);
					}
				} else if(header.equalsIgnoreCase("trial count")) {
					exp.setNumTrials( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("trial length")) {
					exp.setBaseTrialLength( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("uses random target")) {
					exp.setUsesRandomTarget(Boolean.valueOf(datum));
				} else if(header.equalsIgnoreCase("fps")) {
					exp.setFPS( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("seed")) {
					exp.setSeed( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("grid x size")) {
					exp.setGridX( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("grid y size")) {
					exp.setGridY( Double.valueOf(datum).intValue() );
				} else if(header.equalsIgnoreCase("uses background images")) {
					exp.setUsesBackgroundImages( Boolean.valueOf(datum) );
				} else if(header.equalsIgnoreCase("uses memory check")) {
					exp.setUsesMemCheck(Boolean.valueOf(datum) );
				}
			} 
			
			exp.setParsedCorrectly(true);
		} else {
			throw new IOException("Error while reading the header lines in file " + file.getAbsolutePath());
		}
		
		// One of the lines was unparseable or null; error out immediately
		if(!exp.isParsedCorrectly()) {
			throw new IOException("We had trouble parsing your input file: " + file.getAbsolutePath());
		}
		
		// When we infer timestamps, we add frame_increment on every tick
		long frame_increment = (long) (1000.0 / exp.getFPS());
		
		//
		// Parse each trial in the experiment
		while( (data=reader.readNext()) != null) {
			
			String datum=data[0].trim();
			
			// Start a new individual trial
			if(datum.equalsIgnoreCase("begin new trial")) {
				
				Trial trial = new Trial();
				
				// Grab the details for this trial (header+data line)
				if((headers = reader.readNext()) != null && (data = reader.readNext()) != null) {
					for(int idx=0; idx<headers.length; idx++) {
						
						// Grab the header and its associated data (e.g., "Object Speed" and "500")
						String header = headers[idx].trim();
						datum = data[idx].trim();
						
						if(header.equalsIgnoreCase("id")) {
							trial.setID( Double.valueOf(datum).intValue() );
						} else if(header.equalsIgnoreCase("length")) {
							trial.setLength( Double.valueOf(datum).intValue() );
						} else if(header.equalsIgnoreCase("target")) {
							trial.setTarget( datum );
						} else if(header.equalsIgnoreCase("startTime")) {
							trial.setStartTime( Long.valueOf(datum) );
							
						} else if(header.equalsIgnoreCase("targetX")) {
							double x = Double.valueOf(datum);
							datum = data[idx++].trim();
							double y = Double.valueOf(datum);
							trial.setTargetFinalPos(new Coordinate<Double>(x,y));
						} else if(header.equalsIgnoreCase("targetGridX")) {
							int x = Double.valueOf(datum).intValue();
							datum = data[idx++].trim();
							int y = Double.valueOf(datum).intValue();
							trial.setTargetFinalGridPos(new Coordinate<Integer>(x,y));
						} else if(header.equalsIgnoreCase("userX")) {
							double x = Double.valueOf(datum);
							datum = data[idx++].trim();
							double y = Double.valueOf(datum);
							trial.setUserClickPos(new Coordinate<Double>(x,y));
						} else if(header.equalsIgnoreCase("userGridX")) {
							int x = Double.valueOf(datum).intValue();
							datum = data[idx++].trim();
							int y = Double.valueOf(datum).intValue();
							trial.setUserClickGridPos(new Coordinate<Integer>(x,y));
						} else if(header.equalsIgnoreCase("gridClickCorrect")) {
							trial.setGridClickCorrect( Boolean.valueOf(datum) );
						} else if(header.equalsIgnoreCase("gridClickRT")) {
							trial.setGridClickRT( Long.valueOf(datum) );
						} else if(header.equalsIgnoreCase("lineupStimulus")) {
							trial.setLineupStimulus( datum );
						} else if(header.equalsIgnoreCase("lineupClickCorrect")) {
							trial.setLineupClickCorrect( Boolean.valueOf(datum) );
						} else if(header.equalsIgnoreCase("lineupClickRT")) {
							trial.setLineupClickRT( Long.valueOf(datum) );
						} else {
							throw new IOException("Errored while reading headers for trial " + trial.getID() + " in file " + file.getAbsolutePath());
						}
					}
				}
				
				// Grab the target's column number---since we have two columns per
				// distractor, divide by two
				int targetIdx = -1;
				if( (data = reader.readNext()) != null) {
					for(int idx=0; idx<data.length; idx++) {
						datum = data[idx].trim().toLowerCase();
						if(datum.equalsIgnoreCase("target")) {
							targetIdx = idx / 2;
							break;
						}
					}
				} else {
					throw new IOException("Errored while reading the target specifier line in trial " + trial.getID() + " in file " + file.getAbsolutePath());
				}
		
				// If we can't find any stimulus marked as the target, fail immediately
				if(targetIdx < 0) {
					throw new IOException("Could not find the target column for trial id " + trial.getID() + " in file " + file.getAbsolutePath());
				} else {
					trial.setTargetIdx(targetIdx);
				}
				
				// Grab column mappings for distractors' <x,y> coordinates
				// Newer versions have the first col set to "Frame Timestamp"---check for this
				boolean usesTimestamp = false;
				
				List<String> stimuliNames = new ArrayList<String>();
				if( (data = reader.readNext()) != null) {
					for(int idx=0; idx<data.length && !data[idx].isEmpty(); idx++) {
						
						// Grab the title of the stimulus, minus the _x or _y
						datum = data[idx].trim();
						
						// If this column pertains to the timestamp, note it and move on
						if(datum.equalsIgnoreCase("frame timestamp")) {
							usesTimestamp = true;
							continue;
						}		
						
						if(datum.endsWith("_x") || datum.endsWith("_y")) {
							datum = datum.substring(0,datum.length()-2);
						}

						stimuliNames.add(datum);
						idx++;
					}
					
					trial.setStimuliNames(stimuliNames);
				} else {
					throw new IOException("Couldn't find the stimulus names in trial " + trial.getID() + " in file " + file.getAbsolutePath());
				}
				
				
				
				// Tell the parser whether or not we need to read timestamps or infer them
				long timestamp = 0;
				long timestampStart = 0;
				int colOffset = 0;
				if(usesTimestamp) {
					colOffset = 1;
					timestampStart = trial.getStartTime();
				}
				
				
				// Maps Frame -> { <Stim.x,Stim.y> , <Stim.x, Stim.y> , ... }
				// Encode how long the trajectory is in milliseconds (regardless of refresh rate)
				List<Trajectory<TrackItFrame>> stimMoveList = new ArrayList<Trajectory<TrackItFrame>>();
				
				for(int idx=0; idx<stimuliNames.size(); idx++) {
					stimMoveList.add(new Trajectory<TrackItFrame>(timestampStart));
					stimMoveList.get(idx).setLengthMS(trial.getLength());
				}
				
				

				// Parse the movement data for each frame/object in the trial
				while( (data=reader.readNext()) != null) {
					
					datum = data[0].trim().toLowerCase();
					
					// Triggers the end of this trial
					if(datum.startsWith("end new trial")) {	
						trial.setTrajectories(stimMoveList);
						exp.addTrial(trial.getID(), trial);
						break;
					}

					if(usesTimestamp) {
						// If we're tracking timestamps, make sure to offset columns correctly
						timestamp = Long.valueOf( datum );

						// Add on the start-time to get the real timestamp (corresponding to wallclock)
						timestamp += timestampStart;
					}
					
					// A single row of <x,y>'s for each stimulus at a certain point in time
					for(int idx=0; idx<stimuliNames.size(); idx++) {
						
						// Grab the <x,y> coords at columns <idx, idx+1>
						// and store in the right stimulus' column
						datum = data[colOffset + (2*idx)].trim();
						double x = Double.valueOf(datum);
						datum = data[colOffset + (2*idx+1)].trim();
						double y = Double.valueOf(datum);
						stimMoveList.get(idx).registerFrame( new TrackItFrame(new Coordinate<Double>(x,y),timestamp) );
					}

					if(!usesTimestamp) {
						// For older versions of Track-It, we don't explicitly mark a timestamp;
						// we estimate our own.  This won't be used in Track-It v2.0.1+.
						timestamp += frame_increment;
					}

				}
			}
			
		}
		
		reader.close();
		return exp;
	}
}
