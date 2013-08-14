package edu.cmu.cs.eyetrack.io;

import java.util.List;

public interface CSVWritable {
	public String[] getCSVHeader();
	public List<String[]> getCSVData();
}
