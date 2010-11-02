
import org.archive.crawler.Heritrix;
import org.archive.crawler.admin.CrawlJobHandler;
import org.archive.crawler.admin.CrawlJob;
import org.archive.crawler.admin.CrawlJobErrorHandler;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.crawler.settings.CrawlerSettings;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.crawler.event.CrawlStatusListener;

import org.archive.crawler.framework.CrawlController;
import org.archive.crawler.framework.exceptions.InitializationException;
import javax.management.InvalidAttributeValueException;

import java.io.File;
import java.io.IOException;
import javax.management.Attribute;


public class TestCrawl{
	
	private static final String seedfile = "seeds.txt";
	private static final String jobs_home = "jobs";
	private static final String order_home = "order.xml";
	private static boolean crawlRunning;
		
	public static void main(String[] args){
		
		try{
			// Configure storage
			File jobDir = new File(jobs_home);
			File orderfile = new File(order_home);
			System.out.println(orderfile.getAbsolutePath());
			
			// Configure crawl settings
			XMLSettingsHandler settingsHandler = new XMLSettingsHandler(orderfile);
			settingsHandler.initialize();
			CrawlOrder order = settingsHandler.getOrder();
			System.out.println(order.getFrom(null));
			System.out.println(order.getUserAgent(null)+"\n");

			// Configure crawl job
			CrawlJobErrorHandler errorHandler = new CrawlJobErrorHandler();
			CrawlJob job = new CrawlJob("ID01", "My Crawl", settingsHandler, 
							errorHandler, CrawlJob.PRIORITY_HIGH, jobDir);
			
			job.setupForCrawlStart();
			System.out.println(job.importUris(seedfile, "seeds", false, false));	
			job.crawlStarted("Crawl job started: " + job.getJobName());
			job.crawlEnded("Crawl job ended: " + job.getJobName());

			// Configure crawl job handler
			CrawlJobHandler jobHandler = new CrawlJobHandler(jobDir);
			jobHandler.addJob(job);	

			// Configure Heritrix
			Heritrix heritrix = new Heritrix("heritrix", false, jobHandler);
			//heritrix.start();
			System.out.println("Start crawling");
			heritrix.startCrawling();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}// end main
}// end TestCrawl
