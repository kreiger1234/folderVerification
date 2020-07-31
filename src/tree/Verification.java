package tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;


public class Verification {
	enum Mode{
		FIND_DIRECTORIES,
		FIND_FILES
	}
	
	/* 
	*Helper functions below this. All these functions are used in makeTree method.
	*
	*/
	
	List<String> subDirectoriesPath(String rootDirectory, Mode mode) {
		File f = new File(rootDirectory);
		String[] fileList = f.list();
		List<String> directoryPaths = new ArrayList<>();
		if (mode == Mode.FIND_DIRECTORIES) {
			for (String path : fileList) {
				File file = new File(rootDirectory + "\\" + path);
				if (file.isDirectory()) {
					directoryPaths.add(file.getPath());
				}
			}
		}
		else if(mode == Mode.FIND_FILES) {
			System.out.println("DEBUG: " + rootDirectory);
			for (String path : fileList) {
				File file = new File(rootDirectory + "\\" + path);
				if (!file.isDirectory()) {
					directoryPaths.add(file.getPath());
				}
			}
		}
		
		return directoryPaths;
	}
	
	/*
	 * No helper functions beyond this.
	 * No only write class related functions, i.e. the functions need to be called from another class making use of this class.
	 */
	
	StringBuilder processLocation(String rootPath) {
		Queue<String> queue = new LinkedList<String>();
		StringBuilder allFiles = new StringBuilder();
		queue.add(rootPath); //The queue is for folders, anything outside folder needs to be verified.
		while (!queue.isEmpty()) {
			String path = queue.remove(); // this path represents current path we are at. String path is the path where we need to check all *files* inside that path directory. Note path will always be directory.
			//System.out.println(debug);
			List<String> subFiles = subDirectoriesPath(path,Mode.FIND_FILES);
			subFiles.forEach((filePath) -> {
				File f = new File(filePath);
				allFiles.append(f.getName() + " - " +  f.length() + "\n");
			});
			subFiles = subDirectoriesPath(path, Mode.FIND_DIRECTORIES); //TODO - Refactor to prevent repeated looping on same directory, once for files and once for directories.
			subFiles.forEach((filepath)->{
				queue.add(filepath);
			});
		}
		return allFiles;
	}
	
	void writeFile(String filename, StringBuilder allFiles) {
		try {
			FileWriter fw = new FileWriter(new File(filename));
			fw.write(allFiles.toString());
			fw.close();
		}
		catch(IOException ioe) {
			ioe.getMessage();
			ioe.printStackTrace();
		}
	}
	
	void verify(String file1, String file2) {
		File f1 = new File(file1);
		File f2 = new File(file2);
		String line1="",line2="N/A";
		if(f1.exists() && f2.exists()) {
			if(f1.length() == f2.length()) { // While not 100% correct, it still does the job of verification when file names are expected to be the same. Works for my use case, might not fit all.
				System.out.println("Files Verified successfully");
				if(f1.delete() && f2.delete()) {
					System.out.println("Temporary files deleted successfully");
				}
			}
			else {
				try {
					Scanner reader1 = new Scanner(f1);
					Scanner reader2 = new Scanner(f2);
					while(reader1.hasNextLine()) {
						line1=reader1.nextLine();
						line2=reader2.nextLine();
						if(!line1.equals(line2)) {
							System.out.println("Failed to verify: " + line1 + " With " + line2);
						}
						break;
					}
					reader1.close();
					reader2.close();
				}
				catch(FileNotFoundException fnfe) {
					fnfe.getMessage();
					fnfe.printStackTrace();
				}
				catch(NoSuchElementException nsee) {
						System.out.println("Failed to verify: " + line1 + " With " + line2);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Verification t = new Verification();
		File f = new File("D:\\Anime"); //Set first folder here
		File f2 = new File("D:\\Anime"); // Set second folder here
		Thread t1 = new Thread(()->{
			if( f2.isDirectory() ) {
				t.writeFile("data2.txt",t.processLocation(f2.getPath()));
			}
		});
		t1.start();
		if( f.isDirectory() ) {
			t.writeFile("data1.txt",t.processLocation(f.getPath()));
		}
		try {
			System.out.println("Waiting for threads to finish...");
			t1.join();
			System.out.println("Threads finished, starting verification");
			t.verify("data1.txt","data2.txt");
		}
		catch(InterruptedException ie) {
			ie.getMessage();
			ie.printStackTrace();
		}
	}
}
