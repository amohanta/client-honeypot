
import org.archive.crawler.Heritrix;
import org.archive.crawler.framework.exceptions.InitializationException;
import javax.management.InvalidAttributeValueException;
import java.io.IOException;

public class TestCrawl3 extends Heritrix{
	
	private static final String order_home = "order.xml";
	
	public TestCrawl3() throws IOException{
	
		try{
			System.out.println("do one crawl");
			String str = doOneCrawl(order_home);
			System.out.println(str);
		} catch (InitializationException e) {
            e.printStackTrace();
        } catch (InvalidAttributeValueException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		try{
			new TestCrawl3();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}// end main
	
}// end TestCrawl3