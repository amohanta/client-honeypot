
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

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

public class TestCrawl4{
	
	private static final String seedfile = "seeds.txt";
	private static final String crawlfile = "crawl.txt";
	private static final String keywords= "(honeypots)";
	
	private MyCrawler crawler;
	private EventLog logger;
	private DownloadParameters dp;
	private String links;
	
	public TestCrawl4(){
		links = loadLinks(seedfile);
		//searchLinks(-1, keywords, 10);
		run(links);
		print(true);
	}
	
	private String loadLinks(String fname){
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

	private String searchLinks(int engine, String keywords, int maxResults){
		Search search = null;
		/*
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
			case 6: search = AltaVista.search(keywords, maxResults);
					break;
			
			default: search = Google.search(keywords, maxResults);
					break;
		}// end switch
		*/
		new Search (new Google(), keywords, maxResults);
		
		return "";
	}// end getLinksFromEngine

	private void run(String links){
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
	
	private void print(boolean write){
		Vector<Page> pages = crawler.getPages();
		System.out.println("Crawl Links: " + pages.size());
		
		for(int a=0; a<pages.size(); a++){
			System.out.println(pages.get(a).getURL().toString());
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
	
	public static void main(String[] args){
		new TestCrawl4();
	}// end main
}// end TestCrawl4

class MyCrawler extends Crawler {
	
	private Vector<Page> pages;
	
    MyCrawler() {
        super();
		pages = new Vector<Page> ();
    }
   
	// Override method
    public void visit(Page page) {
        //System.out.println("Visiting: " + page.getTitle());
        pages.add(page);
    }// end visit
	
	public Vector<Page> getPages(){
		return pages;
	}// end getPages
}// end MyCrawler
