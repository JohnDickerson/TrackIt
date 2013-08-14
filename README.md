Track-It
=======

Track-It was created specifically to investigate mechanisms of sustained selective attention with young children.

Track-It presents the participant with a grid, with each of the grid locations optionally identified by a background image, and a target object moving on the grid along a random trajectory. Participants are asked to visually track the target and identify the grid location last visited by the target before it disappears. The moving target in this task can be accompanied by distractors, also moving along a random trajectory. Target and distractor objects are optionally randomly selected on each trial from a pool of 72 unique objects. After each trial, all possible distractor and target objects are (optionally) displayed, and the participant is asked to identify which one he or she tracked.

Track-It is an interdisciplinary research project linking the Computer Science and Psychology Departments of Carnegie Mellon University.  See the [website](http://www.psy.cmu.edu/~trackit/) for more details!
  

External Dependencies
=====================
If you'd like to use the automated analytics and comparison functionality coded into Track-It, you'll need to use the [Tobii](<http://www.tobii.com/>) eye tracker.  Included code currently loads Tobii gaze trajectory data in their 2.x- and 3.x- formats (although this could change with a pull request, *hint hint*).

**You can definitely use this software without Tobii**, but the gaze trajectory comparison code will not be applicable without eye-tracking data.


Internal Dependencies
=====================
We depend on the following Java libraries to compile this code: [JCalendar](http://www.toedter.com/en/jcalendar/), [Joda-Time](http://joda-time.sourceforge.net/), [JGoodies Looks](http://www.jgoodies.com/freeware/libraries/looks/), [OpenCSV](<http://opencsv.sourceforge.net/>), and the [Swing Timing Framework](https://java.net/projects/timingframework).  For reproducibility, the actual .jars used in our experiments are available via this repository.  

Note that you can download and run the Track-It .jar file available on the project [website](http://www.psy.cmu.edu/~trackit/) without doing any of this, if you're just interested in running experiments!


Related Research
================

*   *Assessing selective sustained attention in 3- to 5-year-old children: Evidence from a new paradigm*.  2012.  Anna Fisher, Erik Thiessen, Karrie Godwin, Heidi Kloos, John P. Dickerson.  *Journal of Experimental Child Psychology* **113**.  [PubMed link.](http://www.ncbi.nlm.nih.gov/pubmed/23022318)

*   *Eyes as the windows of cognition: The Track-It paradigm and selective attention*.  2012.  Erik Thiessen, John P. Dickerson, Lucy Erickson, Anna Fisher.  *SRCD Themed Meeting on Developmental Methodology*.