import net.htmlparser.jericho.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Math;

public class AnalysisEngine {

	private static int counto = 0; // to count the opening (
	private static int countc = 0;  // to count the closing )
	private static int counto2 = 0; // to count the opening [
	private static int countc2 = 0;  // to count the closing ]	
	
	public static String[] Separator = {
		"+",
		"-",
		"*",
		"=",
		","
	};
		
	
	public static String mainEngine(String script) {
		String returnString=null;
		String shellString=null;
		String str;

		//-------------------------------------
		
		shellString=extractShellCode(script, 1);

		//-analysis-
		str = nopCheck(shellString);
		if (str != null)
			returnString = "\n" + str;
		
		Vector<String>  Strings= StringExtraction(script);
		
		
	/*	str = signatureCheck(shellString);
		if (str != null)
			returnString += "\n" + str;
		
		str = Check(shellString);
		if (str != null)
			returnString += "\n" + str;*/

		//-------------------------------------
		if (str == null)
			returnString = "- Benign ";
		
		//Display of Analysis Engine result:
		System.out.println("\n-----------Analysis for this script = " + returnString + "\n");
		return returnString;
	}

	private static String extractShellCode(String script, int displayOption) {
		String str = "";
		int count = 0;
		int j= 0;
		int len = script.length();
		
		for (int i = 0; i < len; i++){
			if (script.charAt(i) == '\\' && script.charAt(i + 1) == 'x')
				str += "\\x" + script.charAt(i + 2) + script.charAt(i + 3);
			if (script.charAt(i) == '%' && script.charAt(i + 1) == 'u') {
				j = 2;
				count = 0;		
				while(count != 4){		
					if (script.charAt(i+j+count) != '"' && script.charAt(i+j+count) != '+'){
				 		str += "\\x" + script.charAt(i+j+count) + script.charAt(i+j+count+1);
						count = count + 2;
					}
					else {
						j++;
					}
				}
			}
		}

		if(displayOption == 1)				
			System.out.println("Extracted ShellCode: "+str+"\n");
		return str;
	}

	private static String nopCheck(String script) {
		String str = null;
		String nopString = null;
		int nopIndex = 0;

		nopString = "\\x0a\\x0a\\x0a\\x0a";
		nopIndex = script.indexOf(nopString);
		if (nopIndex != -1)
			str = "Malicious NOPsled Found-";
		else
			str = "";
		return str;
	}

	private static String signatureCheck(String script) {		
		String str=null;
		String shellString = null;
		String knownCodeStr = null;
		int shellIndex = 0 ;
		
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/home/hoys/workspace/sqliteDB/MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM MalCode;");
			System.out.println("Connected to signature database...\n");			
			while (rs.next()) {
				//System.out.println("code= "+rs.getString("Code"));
				shellIndex = script.indexOf(rs.getString("Code"));
				if (shellIndex != -1){
					str = "Malicious Shell Code Detected by signature dictionary-";
					//System.out.println("Shell code signature Found!\n");			
				}
			}
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database\n");
		}

		return str;
	}

	private static String SimilarityCheck(String shellcode) {		
		String str=null;	
		double Sum_XY, Sum_DenomX, Sum_DenomY, Cosine_sim, Max_Cosine_sim, Cosine_sim_level;		
		int[] instrCount=new int[Global.N];
		int[] knownCodeInstrCount=new int[Global.N];	
		for(int i=0; i<Global.N; i++) {				
			instrCount[i] = 0;
			knownCodeInstrCount[i] = 0;
		}
		Max_Cosine_sim = 0;
		Cosine_sim_level = 0.50;
		
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/home/hoys/workspace/sqliteDB/MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM MalCode;");
			System.out.println("Connected to signature database for cosine similarity...\n");			
			instrCount = dataModeling(shellcode,1);			
			while (rs.next()) {
				knownCodeInstrCount = dataModeling(rs.getString("Code"),0);
				//instrCount = dataModeling(shellcode,1);
				//Computing cosine similarity
				Sum_XY = Sum_DenomX = Sum_DenomY = Cosine_sim = 0;
				for(int i=0; i< Global.N; i++) {				
					Sum_XY += knownCodeInstrCount[i] * instrCount[i];
					Sum_DenomX += knownCodeInstrCount[i] * knownCodeInstrCount[i];
					Sum_DenomY += instrCount[i] * instrCount[i];					
				}
				Cosine_sim = Sum_XY / Math.sqrt(Sum_DenomX * Sum_DenomY);
				if(Max_Cosine_sim < Cosine_sim)
					Max_Cosine_sim = Cosine_sim;
				System.out.println("Cosine_sim: "+Cosine_sim);
				
			}
			System.out.println("Max_Cosine_sim: "+Max_Cosine_sim);
			if(Max_Cosine_sim > Cosine_sim_level){
				str = "Malicious Shell Code Detected by cosine similarity-";
				//System.out.println("Malicious Shell Code Found");
			}
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database\n");
		}

		return str;
	}

	private static int[] dataModeling(String str, int displayOption) {		
		int[] instrCount=new int[Global.N];		
		for(int i=0; i< Global.N; i++) {				
			instrCount[i] = 0;
		}

		int moveCount=0;
		int pushCount=0;		
		String line;		
		String[] cmd = {
		"bash",
		"-c",
		"echo -e \""+str+"\" | x86dis -e 0 -s intel "
		};
		try {
			Process p = Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				for(int i=0; i< Global.N; i++) {				
					//count the instructions here
					if(line.indexOf(Global.instrString[i])!=-1)
						instrCount[i] +=1;
				}
				if(displayOption == 1)				
					System.out.println(line);
      			}
			input.close();		
		}
		catch (Exception err) {
		      err.printStackTrace();
    		}
		return instrCount;
	}





private static Vector<String>  StringExtraction(String script) {

	  int PositionStart=0,PositionEnd=0;
	  String func="";
	  String arg="";
	  String SubScript=script;
	  String BackScript=script;
	  String ExtractedArg="";
	  String ExtractedArgs="";
	  String ExtractedString="";
	  String arguments="";
		int counto = 0; // to count the opening (
		int countc = 0;  // to count the closing )
		int j= 0,i=0,count=0;
		int position=0;
		Vector<String> StringsList=new Vector<String>();
		Stack<String> VariablesStack=new Stack<String>();
	  for(i=0; i< Global.P; i++) {
		  SubScript=script;
		  PositionStart=0;
		  while(PositionStart!=-1){
			  PositionStart= SubScript.indexOf(Global.MaliciousFunctions[i]);
			  if(PositionStart!=-1){
				BackScript=script.substring(0,PositionStart);
				PositionStart+=Global.MaliciousFunctions[i].length();
				SubScript=SubScript.substring(PositionStart);
				arguments=AllArgumentsExtraction(SubScript);
				SubScript=SubScript.substring(arguments.length()-1);
				ExtractStrParBrack(arguments,StringsList,VariablesStack);
				UpSearch(BackScript,StringsList,VariablesStack);
			  }
		  }
	  }
	 
		
		for(i=0;i<StringsList.size();i++){
			System.out.println("extracted string "+StringsList.get(i));
		}
		for(i=0;i<VariablesStack.size();i++){
			System.out.println("extracted var stack "+VariablesStack.get(i));
		}  
	  
	  
	  return StringsList;
	  
	}
/*
 * 
 * Function that extracts all the arguments of a function
 */
private static String AllArgumentsExtraction (String script){
	
	String arguments="";
	int counto = 0; // to count the opening (
	int countc = 0;  // to count the closing )
	boolean start=true;
	int j= -1;
	
	
	while(counto!=countc || start){
		start=false;
		j++;
		if (script.charAt(j) == '('){
			counto++;
		}
		if (script.charAt(j) == ')'){
			countc++;
		}
	}
	arguments=script.substring(1,j); // return the arguments without the first '(' ')'
	
	return arguments;
}

private static void UpSearch(String BackScript,Vector<String> Strings,Stack<String> VariablesStack){
	String var="";
	String script="";
	int position=0,end=0;
	while(!VariablesStack.isEmpty()){
		var=VariablesStack.pop();
		if(BackScript.contains(var)){
			position=BackScript.indexOf(var)+var.length();
			script=BackScript.substring(position);
			end=script.indexOf(";");
			if(end!=-1)	script=script.substring(0,end);
			ExtractStrParBrack(script,Strings,new Stack<String>());	
		}
	}
	VariablesStack.removeAllElements();
	
	
}



/*private static void CheckArguments (String arguments, String function){
	
	int counto = 0; // to count the opening (
	int countc = 0;  // to count the closing )
	int j= 0,i=0,count=0;
	boolean Parsing=true;
	String ExtractedString="";

	//remove all white spaces and ',' and '='
	while(arguments.length()>1){ // process as long as there are arguments
		count++;
		while (Parsing){
			if(arguments.charAt(0)==' ' || arguments.charAt(0)=='=' || arguments.charAt(0)=='+' || arguments.charAt(0)=='[' || arguments.charAt(0)==','){
				arguments=arguments.substring(1);
			}else{
				Parsing=false; // no need to parse anymore
			}
		}
		if(arguments.charAt(0)=='"' ){
			j++;
			while(arguments.charAt(j)!='"' ){
				j++;
			}
			ExtractedString=arguments.substring(1,j-1);
			arguments=arguments.substring(j+1,arguments.length());
		}else{
		j=0;
		if (arguments.substring(0,0)=="'"){
			j++;
			while(arguments.substring(j,j)=="'"){
				j++;
			}
			ExtractedString=arguments.substring(1,j-1);
			arguments=arguments.substring(j+1,arguments.length());	
		}
		}
		if(!(ExtractedString.contains("'") || ExtractedString.indexOf('"')!=-1)){
			if(!ListStrings.containsKey(function+Integer.toString(count))){
			ListStrings.put(function+Integer.toString(count), ExtractedString); // function+count denotes the the count th argument of the function
			}
		}else{
			ExtraParsing(ExtractedString,function+count);
		}
		
	}
}*/
/*
 * Method used to isolate variables
 */
/*

private static void ExtraParsing (String arguments, String function){
	
	int counto = 0; // to count the opening (
	int countc = 0;  // to count the closing )
	int j= 0,i=0,count=0;
	boolean Parsing=true;
	String ExtractedString="";
	
	//remove all white spaces and ',' and '='
	while(arguments.length()>1){ // process as long as there are arguments
		count++;
		while (Parsing){
			if(arguments.charAt(0)==' ' || arguments.charAt(0)=='=' || arguments.charAt(0)=='+' || arguments.charAt(0)=='['){
				arguments=arguments.substring(1);
			}else{
				Parsing=false; // no need to parse anymore
			}
		}
		if(arguments.charAt(0)=='"' ){
			j++;
			while(arguments.charAt(j)!='"' ){
				j++;
			}
			ExtractedString=arguments.substring(1,j-1);
			arguments=arguments.substring(j+1,arguments.length());
		}else{
		j=0;
		if (arguments.substring(0,0)=="'"){
			j++;
			while(arguments.substring(j,j)=="'"){
				j++;
			}
			ExtractedString=arguments.substring(1,j-1);
			arguments=arguments.substring(j+1,arguments.length());	
		}
		}
		if(!(ExtractedString.contains("'") || ExtractedString.indexOf('"')!=-1)){
			if(!ListStrings.containsKey(function+count)){
			ListStrings.put(function+count, ExtractedString); // function+count denotes the the count th argument of the function
			}
		}
		
	}
}

/*
 * 
 * Method that extracts all the arguments in parentheesis and brackets
 * */

private static void ExtractStrParBrack(String arguments,Vector<String> Strings,Stack<String> VariablesStack){
	
	int j= 0,i=0,p=0;
	int position=0;
	String ExtractedString=arguments;
	if(ExtractedString.contains("(") && ExtractedString.contains("[")){
		position=Math.min(arguments.indexOf('('), arguments.indexOf('['));
		if(position==ExtractedString.indexOf("("))counto++;
		if(position==ExtractedString.indexOf("["))counto2++;
	}else{
		position=Math.max(arguments.indexOf('('), arguments.indexOf('['));
		if(ExtractedString.contains("(")) counto++;
		if(ExtractedString.contains("[")) counto2++;
	}
	

	if(ExtractedString.contains("(") || ExtractedString.contains("[")){
		if (position>=1){
			
			ExtractStrParBrack(ExtractedString.substring(0,position),Strings,VariablesStack);
		}
		if (position!=-1){
			ExtractedString=arguments.substring(position+1);
		}
		

		while(counto!=countc || counto2!=countc2){
			if (ExtractedString.charAt(j) == '('){
				counto++;
				
			}
			if (ExtractedString.charAt(j) == ')'){
				countc++;
				
			}
			if (ExtractedString.charAt(j) == '['){
				counto2++;
				
			}
			if (ExtractedString.charAt(j) == ']'){
				countc2++;
				
			}
			j++;
		}
		
		//System.out.println("string "+ExtractedString.substring(0,j-1));
		if(ExtractedString.contains("(") || ExtractedString.contains("[")){
		if(ExtractedString.contains("(") && ExtractedString.contains("[")){
			position=Math.min(arguments.indexOf('('), arguments.indexOf('['));
		
		}else{
			position=Math.max(arguments.indexOf('('), arguments.indexOf('['));
		}
		}
		String test;
		int posmin=10000;
		for (int k=0;k<Separator.length;k++){
			if(ExtractedString.indexOf(Separator[k])<posmin && ExtractedString.contains(Separator[k])){
				posmin=ExtractedString.indexOf(Separator[k]);
				test=Separator[k];
			}
		}
			
		if(posmin<position && !(ExtractedString.substring(0,posmin).contains("]") || ExtractedString.substring(0,posmin).contains(")"))){
			ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VariablesStack);
			//System.out.println("first string "+ExtractedString.substring(0,posmin));
		}
		
		if(ExtractedString.substring(j,ExtractedString.length()).contains("(") || ExtractedString.substring(j,ExtractedString.length()).contains("[")){
		ExtractStrParBrack(ExtractedString.substring(j,ExtractedString.length()),Strings,VariablesStack);
		}
		if(ExtractedString.substring(0,j-1).contains("(") || ExtractedString.substring(0,j-1).contains("[")){
		ExtractStrParBrack(ExtractedString.substring(0,j-1),Strings,VariablesStack);
		ExtractedString=ExtractedString.substring(0,j-1);
		
		}else{
			ExtractedString=ExtractedString.substring(0,j-1);
			if(ExtractedString.length()>1){
				if(ExtractedString.contains(".")){
					ExtractedString=ExtractedString.substring(0,ExtractedString.indexOf("."));
				}
				posmin=10000;
				for (int k=0;k<Separator.length;k++){
						if(ExtractedString.indexOf(Separator[k])<posmin && ExtractedString.contains(Separator[k])){
							posmin=ExtractedString.indexOf(Separator[k]);
							//System.out.println("posmin is "+posmin);
							test=Separator[k];
						}
					}
				if(posmin>0 && 10000>posmin){
					//System.out.println("extra call "+posmin);
					ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VariablesStack);
					ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VariablesStack);
				}else{
					ExtractedString=ExtractedString.trim();
					ExtractedString=ExtractedString.substring(0,j-1);
					if (ExtractedString.charAt(0)=='"' && ExtractedString.charAt(ExtractedString.length()-1)=='"'){
						if(!Strings.contains(ExtractedString.substring(1, ExtractedString.length()-2)))	Strings.add(ExtractedString.substring(1, ExtractedString.length()-2));
					}else{
						if (ExtractedString.charAt(0)=='"'){
							if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString.substring(1));	
						}
						if (ExtractedString.charAt(ExtractedString.length()-1)=='"'){
							if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString.substring(0,ExtractedString.length()-2));	
						}
						int charCount = ExtractedString.length() - ExtractedString.replaceAll("\\'", "").length();
						ExtractedString=ExtractedString.replaceAll("\\'", "");
						if(charCount==2){
							if(!Strings.contains(ExtractedString)) Strings.add(ExtractedString);
							//System.out.println("Strings.add(ExtractedString); "+ExtractedString.replaceAll("\\'", ""));
						}else{
							//System.out.println("VariablesStack.push1 "+ExtractedString);
							if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString);
						}
					}						
				}
			}
		}
		
	}else{
		if(ExtractedString.contains(".")){
			ExtractedString=ExtractedString.substring(0,ExtractedString.indexOf("."));
		}
		if(ExtractedString.length()>1){
			String test;
			int posmin=10000;
			for (int k=0;k<Separator.length;k++){
					if(ExtractedString.indexOf(Separator[k])<posmin && ExtractedString.contains(Separator[k])){
						posmin=ExtractedString.indexOf(Separator[k]);
						//System.out.println("posmin is "+posmin);
						test=Separator[k];
					}
				}
			if(posmin>0 && 10000>posmin){
				//System.out.println("extra call "+posmin);
				ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VariablesStack);
				ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VariablesStack);
			}else{
				ExtractedString=ExtractedString.trim();
				if (ExtractedString.charAt(0)=='"' && ExtractedString.charAt(ExtractedString.length()-1)=='"'){
					if(!Strings.contains(ExtractedString.substring(1, ExtractedString.length()-1))) Strings.add(ExtractedString.substring(1, ExtractedString.length()-1));
				}else{
					if (ExtractedString.charAt(0)=='"'){
						if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString.substring(1));	
					}
					if (ExtractedString.charAt(ExtractedString.length()-1)=='"'){
						if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString.substring(0,ExtractedString.length()-2));	
					}
					int charCount = ExtractedString.length() - ExtractedString.replaceAll("\\'", "").length();
					ExtractedString=ExtractedString.replaceAll("\\'", "");
					if(charCount==2){
						if(!Strings.contains(ExtractedString)) Strings.add(ExtractedString);
						//System.out.println("Strings.add(ExtractedString); "+ExtractedString.replaceAll("\\'", ""));
					}else{
						//System.out.println("VariablesStack.push1 "+ExtractedString);
						if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString);
					}
				}					
			}
		}
	}	
}



private static Double calculateEntropy(List<String> values) {
	  Map<String, Integer> map = new HashMap<String, Integer>();
	  // count the occurrences of each value
	  for (String sequence : values) {
	    if (!map.containsKey(sequence)) {
	      map.put(sequence, 0);
	    }
	    map.put(sequence, map.get(sequence) + 1);
	  }

	  // calculate the entropy
	  Double result = 0.0;
	  for (String sequence : map.keySet()) {
	    Double frequency = (double) map.get(sequence) / values.size();
	    result -= frequency * (Math.log(frequency) / Math.log(2));
	  }

	  return result;
	}

}