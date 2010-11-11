package Honeysift;

import websphinx.Crawler;
import websphinx.CrawlEvent;
import websphinx.DownloadParameters;
import websphinx.EventLog;
import websphinx.Link;
import websphinx.Page;
import websphinx.searchengine.AltaVista;
import websphinx.searchengine.Excite;
import websphinx.searchengine.Google;
import websphinx.searchengine.HotBot;
import websphinx.searchengine.MetaCrawler;
import websphinx.searchengine.NewsBot;
import websphinx.searchengine.NewsIndex;
import websphinx.searchengine.Search;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;
/**
 * Web crawler based on websphynx
 * @author Damien
 *
 */
public class FastCrawler{
	
	private static  String seedfile = "seeds.txt";
	private static  int depth = 1;
	private static final String crawlfile = "crawl.txt";
	//private static final String keywords= "(honeypots)";
	
	private static MyCrawler crawler;
	private static EventLog logger;
	private static DownloadParameters dp;
	private static String links;
	
	public static void FastCrawlerFile(String filename,int dep) throws IOException {
		if(dep>0) depth=dep;
		if(filename.length()>1) seedfile=filename;
		links = loadLinks(seedfile);
		//searchLinks(-1, keywords, 10);
		run(links);
		print(true);
	}
	
	public static void FastCrawlerURL(int dep,String URL) throws IOException{
		if(dep>0) depth=dep;
	
		//searchLinks(-1, keywords, 10);
		run(URL);
		print(true);
	}
	
	private static String loadLinks(String fname){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(fname)));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			
			System.out.println("Reading from: " + fname);
			
			while (( line = reader.readLine()) != null) {
				buffer.append(line + "\n");
			}// end while

			reader.close();
			System.out.println(buffer.toString());
			return buffer.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}// end loadLinks
/*
	private static Search searchLinks(int engine, String keywords, int maxResults){
		Search search = null;
		
		switch(engine){
			// altavista
			case 0: search = AltaVista.search(keywords, maxResults);
					break;
			// excite
			case 1: search = Excite.search(keywords, maxResults);
					break;
			// Google
			case 2: search = Google.search(keywords, maxResults);
					break;
			// HotBot
			case 3: search = HotBot.search(keywords, maxResults);
					break;
			// MetaCrawler
			case 4: search = MetaCrawler.search(keywords, maxResults);
					break;
			// NewBot
			case 5: search = NewsBot.search(keywords, maxResults);
					break;
			// NewsIndex
			case 6: search = NewsIndex.search(keywords, maxResults);
					break;
			
			default: search = Google.search(keywords, maxResults);
					break;
		}// end switch
		
		//new Search (new Google(), keywords, maxResults);
		
		return search;
	}// end getLinksFromEngine
*/
	private static void run(String links){
		crawler = new MyCrawler ();
        logger = new EventLog ();
		dp = DownloadParameters.DEFAULT;	
		
		//dp = dp.changeAcceptedMIMETypes("");	// http header
        //dp = dp.changeCrawlTimeout();			// -1
        //dp = dp.changeDownloadTimeout();		// 60
        dp = dp.changeInteractive(false);		// true
        //dp = dp.changeMaxPageSize();			// 100kb
        //dp = dp.changeMaxThreads()			// 4
        //dp.changeObeyRobotExclusion();		// false
        //dp = dp.changeUseCaches();			// true
		//dp = dp.changeUserAgent("");
		
		crawler.addCrawlListener (logger);
		crawler.setDomain (Crawler.SUBTREE);		 
        crawler.setDownloadParameters (dp);
		crawler.setMaxDepth(depth);
		//crawler.setLinkPredicate ();
        //crawler.setPagePredicate ();
        //crawler.setAction ());

		try {
			crawler.setRootHrefs(links);
		} catch (java.net.MalformedURLException e) {
			e.printStackTrace();
		}

		crawler.run();
	}// end run
	
	private static void print(boolean write) throws IOException{
		Vector<Page> pages = crawler.getPages();
		System.out.println("Crawl Links: " + pages.size());
		
		for(int a=0; a<pages.size(); a++){
			System.out.println(pages.get(a).getURL().toString());
			CreateDirectory CrD=new CreateDirectory();
			CrD.CreateDir("Captures");		
			String currentDir = new File("").getAbsolutePath();
			//UrlUtils UU=new UrlUtils(pages.get(a).getURL().toString(),currentDir+"\\Captures");
			//UrlDl UD=new UrlDl();
			//UD.fileDownload(pages.get(a).getURL().toString(), currentDir+"\\Captures");
			Download UD=new Download();
			UD.DL(pages.get(a).getURL().toString(), currentDir+"//Captures");
		
		}// end for

		if(!write) 
			return;

		try{
			PrintWriter writer = new PrintWriter(new FileWriter(new File(crawlfile)));
			
			for(int a=0; a<pages.size(); a++){
				writer.println(pages.get(a).getURL().toString());
			}// end for
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}// end print
	
	/*public static void main(String[] args) throws IOException{
		new FastCrawler();
		
	}// end main*/
}// end TestCrawl4

class MyCrawler extends Crawler {
	
	private Vector<Page> pages;
	private Vector<String> types;
    MyCrawler() {
        super();
		pages = new Vector<Page> ();
		types = new Vector<String>();
		types.add("htm");
		types.add("html");
		types.add("php");
		types.add("exe");
    }
   
	// Override method
    public void visit(Page page) {
        //System.out.println("Visiting: " + page.getTitle());
		boolean accept = false;
		
		for(int a=0; a<types.size(); a++){
			if(page.getContentType().contains(types.get(a))){
				accept = true;
				break;
			}// end if
		}// end for
		
		if(accept)
			pages.add(page);
    }// end visit
	
	public Vector<Page> getPages(){
		return pages;
	}// end getPages
}// end MyCrawler




 /*class UrlUtils {

	public UrlUtils(String HOST,String destinationDir) {
		 int slashIndex =HOST.lastIndexOf('/');
		 int periodIndex =HOST.lastIndexOf('.');

		 String fileName=HOST.substring(slashIndex + 1);
		try {
			URL racine = new URL(HOST);
			getFile(racine,fileName,destinationDir);
		} catch (MalformedURLException e) {
			System.err.println(HOST + " : URL non comprise.");
		} catch (IOException e) {
			System.err.println(e);
		}

	}

	public void getFile(URL u,String localFileName, String destinationDir) throws IOException {
		URLConnection uc = u.openConnection();
		String FileType = uc.getContentType();
		int FileLenght = uc.getContentLength();
		if (FileLenght == -1) {
			throw new IOException("Fichier non valide.");
		}
		InputStream in = uc.getInputStream();
		//String FileName = u.getFile();
		//FileName = FileName.substring(FileName.lastIndexOf('/') + 1);
		FileOutputStream WritenFile = new FileOutputStream(destinationDir+"\\"+localFileName);
		byte[]buff = new byte[1024];
		int l = in.read(buff);
		while(l>0)
		{
		WritenFile.write(buff, 0, l);
		l = in.read(buff);
		}
		WritenFile.flush();
		WritenFile.close();

	}

}*/

 /*class UrlDl {
	 final static int size=38000;
	 public static void  fileUrl(String fAddress, String localFileName, String destinationDir) throws IOException{
	 
		 
	 OutputStream outStream = null;
	 URLConnection  uCon = null;
	 InputStream is = null;
	 try {
		 URL Url;
		 byte[] buf;
		 int ByteRead,ByteWritten=0;
		 Url= new URL(fAddress);
		 outStream = new BufferedOutputStream(new
		 FileOutputStream(destinationDir+"\\"+localFileName));
		 uCon = Url.openConnection();
		 String FileType = uCon.getContentType();
		 int FileLenght = uCon.getContentLength();
		 if (FileLenght == -1) {
				throw new IOException("Fichier non valide.");
		 }
		 is = uCon.getInputStream();
		 buf = new byte[size];
		 while ((ByteRead = is.read(buf)) != -1) {
		 outStream.write(buf, 0, ByteRead);
		 ByteWritten += ByteRead;
		 }
		 outStream.flush();
		 outStream.close();
		 System.out.println("Downloaded Successfully.");
		 System.out.println("File name:\""+localFileName+ "\"\nNo ofbytes :" + ByteWritten);
	 }
	 catch (Exception e) {
		 System.out.println(e);
	// e.printStackTrace();
	 }
	 }
	 public static void fileDownload(String fAddress, String destinationDir) throws IOException
	 {
	  
	   int slashIndex =fAddress.lastIndexOf('/');
	 int periodIndex =fAddress.lastIndexOf('.');

	 String fileName=fAddress.substring(slashIndex + 1);

	 if (periodIndex >=1 &&  slashIndex >= 0 && slashIndex < fAddress.length()-1)
	 {
	 fileUrl(fAddress,fileName,destinationDir);
	 }
	 else
	 {
	// System.err.println("path or file name.");
	 }}
 }*/
 

class Download {

 	/**
 	 * @param args
 	 */
 	public static void DL(String fAddress, String destinationDir) {		
		 int slashIndex =fAddress.lastIndexOf('/');
		 int periodIndex =fAddress.lastIndexOf('.');
		 String fileName=fAddress.substring(slashIndex + 1);
 		try { 
 			URL url = new URL(fAddress);
 			 URLConnection conn = url.openConnection();
 			 conn.setDoOutput(true);
 			 InputStream in = null;
 			 in = url.openStream();
 			 String content = pipe(in,"utf-8", fileName, destinationDir);
 			 //System.out.println(content);
 		} catch (Exception e) {
 			//new Delete().DelFile(destinationDir+"\\"+fileName);
 			System.out.println(" Page could not be downloaded, file was deleted go to next URL");
 		}
 	}

 	static String pipe(InputStream in,String charset,String localFileName, String destinationDir) throws IOException {
         StringBuffer s = new StringBuffer();
         if(charset==null||"".equals(charset)){
         	charset="utf-8";
         }
         String rLine = null;
         BufferedReader bReader = new BufferedReader(new InputStreamReader(in,charset));
         PrintWriter pw = null;
         
 		FileOutputStream fo = new FileOutputStream(destinationDir+"//"+localFileName);
 		OutputStreamWriter writer = new OutputStreamWriter(fo, "utf-8");
 		pw = new PrintWriter(writer);
         while ( (rLine = bReader.readLine()) != null) {
             String tmp_rLine = rLine;
             int str_len = tmp_rLine.length();
             if (str_len > 0) {
               s.append(tmp_rLine);
               pw.println(tmp_rLine);
               pw.flush();
             }
             tmp_rLine = null;
        }
         in.close();
         pw.close();
         return s.toString();
 	}
 }
 
 
 
class CreateDirectory {
	   
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

class Delete {
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
 

