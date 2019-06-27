# SPLAT test case

## Description
GUI app (I use **JavaFX**) for searching text in log-files at certain directory, and open file for reading.

## Short review
For optimizing searching text in log files I use something like buffered reader.
The size of buffer was chosen after some small tests (4k,8k,16k,32k).

    private static final int BUFFER_SIZE = 8192 * 4;

As a element for reading text-file I have chosen 
ListView with vertical orientation 
(~~TextArea~~ was simple solution, but so bad for big files)

Application has 3 threads - main javafx-thread with UI, 
searchThread for searching text in log files and 
foundFilesThread for add found files to TreeView. When you click on "Search" button both threads start, but UI is still responsive.

Weaknesses of my solution:
* TreeView is something like List, not like FileTreeView for only found files.
* Not adaptive UI
* Pagination could be good idea for opening big files in new tab
* I don't use (because of poor experience) design patterns and structure of JavaFX projects, 
it can cause issues with further support.

Using JavaFX was interested for me (that was my 1st javafx-project), 
and it helps me improve my UI and multithreading skills. 
I'm glad to study something new. 

## Demonstration
Start window

![screenshot_start_window](https://github.com/ShittyWizard/splat-test-case/blob/master/src/demo_img/start_window.png "Screenshot of start window")

Searching...


![screenshot_start_window](https://github.com/ShittyWizard/splat-test-case/blob/master/src/demo_img/searching.png "Screenshot of searching")

Results


![screenshot_start_window](https://github.com/ShittyWizard/splat-test-case/blob/master/src/demo_img/results.png "Screenshot with result-window")





