import net.htmlparser.jericho.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class ExtractScript {
	public static void main(String[] args) throws Exception {
		String sourceUrlString="data/IMWebControl.htm";
		AnalysisEngine analysisEngine = new AnalysisEngine();
	
		if (args.length==0)
		  	System.err.println("Using default argument of \""+sourceUrlString+'"');
		else
			sourceUrlString=args[0];
		if (sourceUrlString.indexOf(':')==-1) sourceUrlString="file:"+sourceUrlString;

		Source source=new Source(new URL(sourceUrlString));
 
		//Get title		
		System.out.println("\n--Document title:");
		String title=getTitle(source);
		System.out.println(title==null ? "no title found" : title);

		//Get script		
		System.out.println("\n--Document scripts:");
		List<Element> scriptElements=source.getAllElements(HTMLElementName.SCRIPT);
		if (scriptElements.isEmpty())
			System.out.println("No Script Detected");	
		else {		
			System.out.println(scriptElements.size()+" script detected");			
			System.out.println("\n================================================\n");
			System.out.println("----Detection Result----");		
			int count=1;		
			for (Element scriptElement : scriptElements) {
				String script=scriptElement.getContent().toString();
				System.out.println("\nScript "+count+" : "+script);	// display code

				//---Go to analysis engine---
				String result=analysisEngine.mainEngine(script);
				System.out.println("\n--Analysis for this script = " + result + "\n");			
				
				count++;	
			}
			System.out.println("\n================================================\n");
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
