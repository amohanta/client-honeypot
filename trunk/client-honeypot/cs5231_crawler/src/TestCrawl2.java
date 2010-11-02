
import org.archive.crawler.Heritrix;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.crawler.event.CrawlStatusListener;
import org.archive.crawler.framework.CrawlController;

import org.archive.crawler.framework.exceptions.InitializationException;
import javax.management.InvalidAttributeValueException;

import java.io.File;
import java.io.IOException;

public class TestCrawl2  extends Heritrix implements CrawlStatusListener{
	
	private static final String seedfile = "seeds.txt";
	private static final String jobs_home = "jobs";
	private static final String order_home = "order.xml";
	private static boolean crawlRunning;
		
	private CrawlController controller;
	
	public TestCrawl2() throws IOException{
		
        try {
			XMLSettingsHandler handler = new XMLSettingsHandler(new File(order_home));
			handler.initialize();
			System.out.println("Load order");
			
			controller = new CrawlController();
            controller.initialize(handler);
		} catch (InitializationException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
			e.printStackTrace();
		}
        
		System.out.println("Start crawl");
		controller.addCrawlStatusListener(this);
        controller.requestCrawlStart();
		crawlStarted("Started crawling");
		
		while(crawlRunning){
		
		}// end while
	}
	
	public static void main(String[] args){
		try{	
			new TestCrawl2();
		
		} catch(IOException e) {
			e.printStackTrace();
		}
	}// end main

	public void crawlCheckpoint(File dir){}
          
	public void crawlEnded(String msg){
		this.crawlRunning = false;
	}
          
	public void crawlEnding(String msg){}
          
	public void	crawlPaused(String msg){}
          
	public void	crawlPausing(String msg){}

	public void	crawlResuming(String msg){}

	public void crawlStarted(String msg){
		this.crawlRunning = true;
	}
}// end TestCrawl2
