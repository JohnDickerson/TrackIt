package edu.cmu.cs.eyetrack.io;

import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;
import edu.cmu.cs.eyetrack.state.GameState;

public class TestRecord {

	private static TestRecord instance = null;
	
	private TestRecord() {
		
	}
	
	public static synchronized TestRecord getInstance() {
		if(instance == null) {
			instance = new TestRecord();
		}
		return instance;
	}
	
	public void updateLog(GameState state, boolean shortCircuited) throws IOException {
		
		// If we call this before setting up the state, return gracefully
		if(state.getIO() == null) {
			return;
		}
		
		@SuppressWarnings("resource")
		CSVWriter writer = new CSVWriter(state.getIO(), ',');
		
		while(state.hasMoreToWrite()) {
			writer.writeNext(state.nextDataLine());			
		}
		
		// If we short circuited (e.g., user exited abruptly, program failed), 
		// make a mark in the .csv file
		if(shortCircuited) {
			writer.writeNext(new String[] { "End of entire file." } );
		}

		writer.flush();

		//writer.close();
	}
	
	public void updateLog(GameState state) throws IOException {
		// Assume we are updating normally
		updateLog(state, false);
	}
	
}
