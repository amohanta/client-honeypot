package Honeysift;
import net.htmlparser.jericho.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
import java.net.*;
import static java.io.File.*;

/**
 * Main Class for HoneySift
 * Parse the arguments and call the analysis, crawling, learning engines
 * @author Damien
 *
 */


public class AnalysisRunner {
	public static void main(String[] args) throws Exception {
		
		OptionParser parser = new OptionParser() {
            {
                accepts( "learning" ).withRequiredArg().ofType( String.class )
                    .describedAs( "learn : used when you want to add normal or obfuscated code to the learning database {Normal,Obfuscated}" ).defaultsTo( "Obfuscated" );
                accepts( "crawl" ).withRequiredArg().ofType( String.class )
                    .describedAs( "crawl : select this option to crawl the web" ).defaultsTo("http://forum.malekal.com");
                accepts( "filecrawl" ).withOptionalArg().ofType( String.class )
                .describedAs( "crawl : select this option to crawl the web based on the URLS that are the file that you provide" ).defaultsTo(new File("").getAbsolutePath()+"//seeds.txt");
              
                accepts( "verbose" ).withOptionalArg().describedAs("verbose")
                .describedAs( "be verbose: display the javascripts that are extracted and more information" );
                accepts( "help" ).withOptionalArg().describedAs( "help" );
                accepts( "analyze" ).withOptionalArg().describedAs( "Analyzing : select this option for the static analysis to run" );
                accepts( "?" ).withOptionalArg().describedAs( "help" );
                accepts( "input" ).withOptionalArg().ofType( String.class )
                     .describedAs( "Input folder for learning or analyzing web pages (no need to change if you use crawling). The folder must be a subfolder of the location where the program is and you should give only the subpath" ).defaultsTo("Captures");
                accepts( "depth" ).withRequiredArg().ofType( Integer.class )
                .describedAs( "depth of search for crawling, warning depth>2 can be very long" ).defaultsTo( 1 );
                accepts( "accuracy" ).withRequiredArg().ofType( Double.class )
                .describedAs( "accuracy 0<x<1 for the cosine similiraty to tag the code as malicious" ).defaultsTo( 0.96 );
            }
        };
        
        
        OptionSet options = parser.parse( args );
        if ( options.has( "?" ) || options.has( "h" ) || options.has( "help" ) ){
        	System.out.println("--------------------HoneySift Project for CS5231 Module NUS-----------");
        	parser.printHelpOn( System.out );
        	System.out.println(" ");
        	System.out.println("Examples of valid commands are:");
        	System.out.println("--crawl http://www.facebook.com --depth 2 --analyze");
        	System.out.println("--filecrawl list.txt --analyze --accuracy 0.98 --verbose");
        	System.out.println("--analyze --input test\\malwares");
        	System.out.println("--learn Normal --input test\\NormalPages");
        }
        //assertTrue( options.has( "help" ) );
        if(options.has("filecrawl")){
        	FastCrawler.FastCrawlerFile((String) options.valueOf("filecrawl"),(Integer) options.valueOf("depth"));
        }
        
        if(options.has("crawl")){
        	FastCrawler.FastCrawlerURL((Integer) options.valueOf("depth"),(String) options.valueOf("crawl"));
        }
        
        if(options.has("analyze")){
        	System.out.println("-----------------------Starting Analysis--------------------------");
        	System.out.println("............");
        	if(options.has("crawl")){
        	RunAnalysis((String)options.valueOf("input"),(String) options.valueOf("crawl"),(Double)options.valueOf("accuracy"),options.has("verbose"));
        	}else{
        	BufferedReader reader = new BufferedReader(new FileReader(new File((String) options.valueOf("filecrawl"))));	
        	String name="";
        	if(options.valueOf("input")=="Captures")name= reader.readLine();
        	RunAnalysis((String)options.valueOf("input"),name,(Double)options.valueOf("accuracy"),options.has("verbose"));
        	}
        	System.out.println("-------------------------Operations done------------------------- ");
            System.out.println("Results are available in report table with sqlite in "+new File(".").getAbsolutePath()+"MalDB");
        }
        
        if(options.has("learning")){
        	if(options.valueOf("learning")=="Obfuscated"){
        	RunLearning((String)options.valueOf("input"),"LearningOb",options.has("verbose"));	
        	}else{
        	RunLearning((String)options.valueOf("input"),"LearningN",options.has("verbose"));	
        	}
        	
        }
        	

		
  	}

	private static void RunAnalysis(String folder,String domainname,double accuracy,boolean verbose) throws MalformedURLException, IOException{
		 File dir = new File(folder);
		 boolean test=false;
		    String[] chld = dir.list();
		    if(chld == null){
		      System.out.println("Specified directory does not exist or is not a directory.");
		      System.exit(0);
		    }else{
		      for(int i = 0; i < chld.length; i++){
		    	String sourceUrlString= dir+"//"+chld[i];
		        if(verbose) System.out.println(sourceUrlString);		
				if (sourceUrlString.indexOf(':')==-1) sourceUrlString="file:"+sourceUrlString;
				try{
				Source source=new Source(new URL(sourceUrlString));	 
				//Get title		
				if(verbose)System.out.println("\n--Document title:");
				String title=getTitle(source);
				if(verbose)System.out.println(title==null ? "no title found" : title);

				//Get script		
				if (verbose)System.out.println("\n--Document scripts:");
				List<Element> scriptElements=source.getAllElements(HTMLElementName.SCRIPT);
				if (scriptElements.isEmpty()){
				if(verbose)System.out.println("No Script Detected");}
				else {		
					if(verbose){
					System.out.println(scriptElements.size()+" script detected");			
					System.out.println("\n================================================\n");
					System.out.println("----Detection Result----");		}
					int count=1;		
					for (Element scriptElement : scriptElements) {
						String script=scriptElement.getContent().toString();
						if(verbose)System.out.println("\nScript "+count+" : "+script);	// display code

						//---Go to analysis engine---
						test=AnalysisEngine.mainEngine(script,domainname+"/"+chld[i]+" (script "+count+")",accuracy,verbose);												
						count++;	
					}
					if(verbose)System.out.println("\n================================================\n");
				}
		      }catch(Exception e){
		    	  System.out.println(e);
		      }
		      if(!test && folder=="Captures"){ Delete.DelFile(new File("").getAbsolutePath()+"//"+folder+"//"+chld[i]);}
		      else{if(folder=="Captures"){
		    	  CreateDirectory CrD=new CreateDirectory();
		    	  CrD.CreateDir("MaliciousPages");
		    	  // File (or directory) to be moved
		    	    File file = new File(new File("").getAbsolutePath()+"//"+folder+"//"+chld[i]);
		    	    
		    	    // Destination directory
		    	    File dir2 = new File(new File("").getAbsolutePath()+"//MaliciousPages//");
		    	    // Move file to new directory
		    	    boolean success = file.renameTo(new File(dir2, file.getName()));
		    	    if (!success) {
		    	        // File was not successfully moved
		    	    }
		      }
		    	  	    	  
		      }
		      }
		      
		    }
	
		    }
	
	private static void RunLearning(String folder,String opt,boolean verb) throws MalformedURLException, IOException{
		
		 File dir = new File(folder);
	    String[] chld = dir.list();
	    if(chld == null){
	      System.out.println("Specified directory does not exist or is not a directory.");
	      System.exit(0);
	    }else{
	      for(int i = 0; i < chld.length; i++){
	    	String sourceUrlString= dir+"//"+chld[i];
	        System.out.println(sourceUrlString);
	        //String sourceUrlString="data/toshiba.html";
			//MachineLearning learningEngine = new MachineLearning();
			if (sourceUrlString.indexOf(':')==-1) sourceUrlString="file:"+sourceUrlString;

			Source source=new Source(new URL(sourceUrlString));
	 
			//Get title		
			if(verb)System.out.println("\n--Document title:");
			String title=getTitle(source);
			if (verb)System.out.println(title==null ? "no title found" : title);

			//Get script		
			if(verb)System.out.println("\n--Document scripts:");
			List<Element> scriptElements=source.getAllElements(HTMLElementName.SCRIPT);
			if (scriptElements.isEmpty()){
				System.out.println("No Script Detected");}	
			else {		
				if(verb){
				System.out.println(scriptElements.size()+" script detected");			
				System.out.println("\n================================================\n");
				System.out.println("----Detection Result----");		}
				int count=1;		
				for (Element scriptElement : scriptElements) {
					String script=scriptElement.getContent().toString();
					if(verb)System.out.println("\nScript "+count+" : "+script);	// display code

					//---Go to analysis engine---
					
					MachineLearning.mainEngine(script,opt,verb);

					count++;	
				}
				if(verb)System.out.println("\n================================================\n");
			}
			
			
	      }  
	    }		
	}
	
	static class Delete {
		  public static void DelFile(String fileName) {
		    //String fileName = "file.txt";
		    // A File object to represent the filename
		    File f = new File(fileName);

		    // Make sure the file or directory exists and isn't write protected
		    if (!f.exists())
		      throw new IllegalArgumentException(
		          "Delete: no such file or directory: " + fileName);

		    if (!f.canWrite())
		      throw new IllegalArgumentException("Delete: write protected: "
		          + fileName);

		    // If it is a directory, make sure it is empty
		    if (f.isDirectory()) {
		      String[] files = f.list();
		      if (files.length > 0)
		        throw new IllegalArgumentException(
		            "Delete: directory not empty: " + fileName);
		    }

		    // Attempt to delete it
		    boolean success = f.delete();

		    if (!success)
		      throw new IllegalArgumentException("Delete: deletion failed");
		  }

	}
	/**
	 * Class used to create directories
	 * @author Damien
	 *
	 */
	static class CreateDirectory {
		   
		 public static void CreateDir(String DirName){
		 String currentDir = new File(".").getAbsolutePath();
		// System.out.println(currentDir+"/"+DirName);
	       try{
	 		

	 		// Create one directory
	 		boolean success = (new File(DirName)).mkdir();
	 		if (success) {
	 			System.out.println("Directory: " + currentDir+"/"+DirName + " created");
	 		}		

	 		}catch (Exception e){//Catch exception if any
	 			System.err.println("Error: " + e.getMessage());
	 		}
	 	}
	 
	 }
	
	private static String getTitle(Source source) {
		Element titleElement=source.getFirstElement(HTMLElementName.TITLE);
		if (titleElement==null) return null;
		// TITLE element never contains other tags so just decode it collapsing whitespace:
		return CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
	}
	private static String getScript(Source source) {
		Element scriptElement=source.getFirstElement(HTMLElementName.SCRIPT);
		if (scriptElement==null) return null;
		return CharacterReference.decodeCollapseWhiteSpace(scriptElement.getContent());
	}
	
	

}
