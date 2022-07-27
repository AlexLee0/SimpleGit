# Gitlet Design Document
author: Alex Lee

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures
### 
Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main.java
This class is the entry point of the program. It implements methods to set up persistance and support each command of the program.

### CommandClass.java
This class contains the commands that will be classed in Main.java.

#### Fields
1. private File CWD: A pointer to the current working directory of the program.
2. private String HEAD: Keeps track of name of branch that is HEAD
3. public File TO_HEAD: File to find HEAD branch in .gitlet/branches 

### StagingAdd.java
This class will organize the name of modified or new text files to the blob that they contain, to keep track of changes to the files that have not been committed yet.

#### Fields
1. public Hashmap stagingAddHash: Organizes file name to blob and will clear when committed

### StagingRemove.java
This class will organize the name of removed text files to the blob that they contain, to keep track of changes to the files that have not been fully removed from CWD yet.

#### Fields
1. public Hashmap stagingRemoveHash: Organizes file name to blob and will clear when committed

### Commit.java
This class represents an instance containing message, parent, and hashmap.

#### Fields
1. private String _message: Message of the Commit
2. private String _timestamp: Timestamp in which the Commit was committed
3. private Commit _parent: Commit that came before the current Commit Object
4. private Hashmap _hash: Hashmap that organizes file name to file content


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

### Main.java
1. main(String... args): Check if args are valid and will call on respective method in CommandClass.java

### CommandClass.java
1. init: Initialize gitlet folder, with branches, commits, stagingAdd, and stagingRemove directories as well as initial commit (if .gitlet does not already exist)
2. commit: Saves the files in stagingAdd and clear stagingAdd, removes the files in stagingRemove and clear stagingRemove, rewrite HEAD branch to new commit
3. add: Add file to the stagingAdd, remove from stagingRemove if in stagingRemove, do not add if same content as CWD
4. log: Report each commits with message and timestamp in reverse order
5. checkout: update file contents from commit, file, or branch depending on the arguments
6. merge: check if merge is necessary or if there are file conflicts, then merge files
7. rm: Unstage file if in Staging_Add. Check that file is in current commit, store in Staging_Remove, then remove file from directory
8. globalLog: Report all commits with message and timestamp in any order
9. find: Find the commit with the message
10. status: Check if Staging_Add and Staging_Remove are empty. If not print files in Staging_Add and Staging_Remove 

### StagingAdd.java
1. StagingAdd(): make new stagingAddHash
2. clearStage: reset Hashmap when commit is initiated
3. addToAddHash: add file to stagingAdd
4. removeFromAddHash: add file from stagingAdd

### StagingRemove.java
1. StagingRemove(): make new stagingRemoveHash
2. clearStage: reset Hashmap when commit is initiated
3. addToRemoveHash: add file to stagingRemove
4. removeFromRemoveHash: add file from stagingRemove


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

### Commit
- For each commit, we serialize the commit's message and create new path with the unique hashcode. This can allow us to access the Hashmap contents.

## Branch
- For each branch, we track the entire commit Object. This can allow us to access the Hashmap contents as well as message and timestamp when we need to report commits.

## HEAD
- For HEAD, we track the name of the branch that is considered the HEAD branch. This can allow us to access the contents of the HEAD branch by using HEAD file as storage for String name.

## stagingAdd
- For each change to stagingAddHash, we write the contents of the stagingAdd to the same file. This can allow us to access the contents of the stagingAddHash.

## stagingRemove
- For each change to stagingRemoveHash, we write the contents of the stagingRemove to the same file. This can allow us to access the contents of the stagingRemoveHash.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

