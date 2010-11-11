package Honeysift;
import net.htmlparser.jericho.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Math;
import weka.estimators.KernelEstimator;

/**
 * Analysis Engine for HoneySift. 
 * @author Damien
 *
 */

public class AnalysisEngine {

	private static int counto = 0; // to count the opening (
	private static int countc = 0;  // to count the closing )
	private static int counto2 = 0; // to count the opening [
	private static int countc2 = 0;  // to count the closing ]	
	private static boolean verb=false;
	public static String[] Separator = {
		"+",
		"-",
		"*",
		"=",
		",",
		"<",
		">"
	};
		
	
	public static boolean mainEngine(String script,String URL,double accuracy,boolean verbose) {
		String returnString=null;
		String shellString=null;
		boolean mal=false;
		boolean obfusc=false;
		boolean shell=false;

		//-------------------------------------
		verb=verbose;// for the display options
		//Analysis
		shellString=extractShellCode(script);
		if(shellString.length()>1) shell=true;	
		Vector<String>  Strings= StringExtraction(script);
		if(Strings.size()>0){
		obfusc=BayesClassiciation(Strings);}
		
		double sim=SimilarityCheck(shellString);
		if(sim>accuracy) mal=true;

		WriteReport(URL,signatureCheck(shellString),mal,sim,obfusc,nopCheck(shellString),shell);
	
		//Display of Analysis Engine result:
		//System.out.println("\n-----------Analysis for this script = " + returnString + "\n");
		return mal;
	}
	
	/**
	 * Write the results of the analysis in a sqlite DB
	 * @param URL : URL being tested
	 * @param SignDetect : result of the signature detection test
	 * @param Malicious :  result of the signature or similiraty detection test
	 * @param Similarity :  value fo the similairty
	 * @param Obfuscated :  result of the obfuscation detection test
	 * @param NOPSled :  result of the NOPsled detection test
	 * @param Shellcode :  result of the shellcode detection test
	 */
	private static void WriteReport(String URL,boolean SignDetect,boolean Malicious,double Similarity,boolean Obfuscated,boolean NOPSled,boolean Shellcode){
		if(SignDetect) Malicious=SignDetect;
		try {			
			Class.forName("org.sqlite.JDBC");
			
			Connection conn = DriverManager.getConnection("jdbc:sqlite:MalDB");	
			Statement stat = conn.createStatement();
			
			//System.out.println("INSERT INTO Report (URL,SignDetect,Malicious,Similarity,Obfuscated,NOPSled,Shellcode) " +
					//"VALUES (" + URL+","+SignDetect+","+Malicious+","+Similarity+","+Obfuscated+","+NOPSled+","+Shellcode+");");
			stat.executeQuery("INSERT INTO Report (URL,SignDetect,Malicious,Similarity,Obfuscated,NOPSled,Shellcode) " +
					"VALUES ('" + URL+"','"+SignDetect+"','"+Malicious+"',"+Similarity+",'"+Obfuscated+"','"+NOPSled+"','"+Shellcode+"');" );
			stat.close();
			conn.close();
			if(verb) System.out.println("Value added to Report database...\n");			
			
		}
		catch (Exception e) {
			//System.out.println(e+" Error---Unable to connect to database. Please copy MalDB in the folder where the program is\n");
		}
		}
		
	/**
	 * Function that estimates whether the script is obfuscated
	 * @param StringsList
	 * @return test result
	 */
	
	
	private static boolean BayesClassiciation(Vector<String> StringsList){
		Vector<double[]> Ngram=Ngrams(StringsList);
		Vector<Double> Entropies=calculateEntropy(StringsList);
		double leng=AverageLength(StringsList);
		double [] CurNgram;
		Double Entropy;
		double Pobfusc=0;
		double Pnormal=0;
		Vector<KernelEstimator> PDFObfusc=PDFEestimation("LearningOb");
		Vector<KernelEstimator> PDFNorm=PDFEestimation("LearningN");
		
		for (int i=0;i<Entropies.size();i++){
		
		Pobfusc=Pobfusc+Math.log(PDFObfusc.get(0).getProbability(Ngram.get(i)[0])*PDFObfusc.get(1).getProbability(Ngram.get(i)[1])*PDFObfusc.get(2).getProbability(Ngram.get(i)[3])*PDFObfusc.get(3).getProbability(Entropies.get(i)));
		Pnormal=Pnormal+Math.log(PDFNorm.get(0).getProbability(Ngram.get(i)[0])*PDFNorm.get(1).getProbability(Ngram.get(i)[1])*PDFNorm.get(2).getProbability(Ngram.get(i)[3])*PDFNorm.get(3).getProbability(Entropies.get(i)));
		
		}
		
		Pobfusc=Pobfusc+Math.log(PDFObfusc.get(4).getProbability(leng));
		Pnormal=Pnormal+Math.log(PDFNorm.get(4).getProbability(leng));
		
		
		if(Pobfusc>Pnormal){
			if(verb){ System.out.println("Code is obfuscated");
			System.out.println("Log Probability of being normal code: "+Pnormal);
			System.out.println("Log Probability of being obfuscated code: "+Pobfusc);}
			return true;
		}else{
			if(verb) System.out.println("Code is normal");
			return false;
		}
		
		
	}
	
	/**
	 * Estimation of the pdf
	 * @param Table
	 * @return
	 */
	
	private static Vector<KernelEstimator> PDFEestimation(String Table){
		Vector<KernelEstimator> PDFs=new Vector<KernelEstimator>();
		KernelEstimator NG1=new KernelEstimator(0.01);
		KernelEstimator NG2=new KernelEstimator(0.1);
		KernelEstimator NG3=new KernelEstimator(0.01);
		KernelEstimator Entropy=new KernelEstimator(0.02);
		KernelEstimator Length=new KernelEstimator(1);
		
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM "+Table+";");
			if(verb) System.out.println("Connected to Learning database to estimate probabilities...\n");			
			while (rs.next()) {
				
				
				//System.out.println("reading NG1 "+rs.getDouble("NG1"));
				NG1.addValue(rs.getDouble("NG1"),1);
				NG2.addValue(rs.getDouble("NG2"),1);
				NG3.addValue(rs.getDouble("NG3"),1);
				Entropy.addValue(rs.getDouble("Entropy"),1);
				Length.addValue(rs.getDouble("Length"),1);				
			}
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database\n");
		}
		PDFs.add(NG1);
		PDFs.add(NG2);
		PDFs.add(NG3);
		PDFs.add(Entropy);
		PDFs.add(Length);
		return PDFs;
	}
	
	
	/**
	 * test if the string is a decimal number
	 * @param str
	 * @return
	 */
	
	private static boolean IsNumber(String str) {
			boolean test=true;
	        char[] all = str.toCharArray();
	        String numbers = "";
	        for(int i = 0; i < all.length;i++) {
	            if(!Character.isDigit(all[i])) {
	                test=false;
	                break;
	            }
	        }
	       // System.out.println(str+" is number "+test);
	 
	        return test;
	}
	
	/**
	 * 
	 * Calculate the average length of extracted strings
	 * @param StringList
	 * @return
	 */
	
	private static double AverageLength(Vector<String> StringList) {
		// Calculate the entropy of readable characters divied by length of the string
		int k=0;
		double leng=0; 
		for(int p=0;p<StringList.size();p++){
			if(!IsNumber(StringList.get(p))){
				leng+=StringList.get(p).length();
				k++;
			}
			}
		return leng/k;
	}
	/**
	 * Calculate the entropy of each string
	 * @param StringList
	 * @return
	 */

	private static Vector<Double> calculateEntropy(Vector<String> StringList) {
		// Calculate the entropy of readable characters divied by length of the string
		
		Vector<Double> EntropyList=new  Vector<Double>(); 
		for(int p=0;p<StringList.size();p++){
			List<Character> values=new Vector<Character>();
			if(!IsNumber(StringList.get(p))){
				for (int z=0;z<StringList.get(p).length();z++){
					values.add(StringList.get(p).charAt(z));
				}
				Map<Character, Integer> map = new HashMap<Character, Integer>();
				  // count the occurrences of each value
				  for (Character sequence : values) {
					  if(((int)sequence<58 && (int)sequence>47) || ((int)sequence<91 && (int)sequence>64) || ((int)sequence<123 && (int)sequence>96)){
				    if (!map.containsKey(sequence)) {
				      map.put(sequence, 0);
				    }
				    map.put(sequence, map.get(sequence) + 1);
				  }
				  }
				  // calculate the entropy
				  Double result = 0.0;
				  for (Character sequence : map.keySet()) {
				    Double frequency = (double) map.get(sequence) / values.size();
				    result -= frequency * (Math.log(frequency) / Math.log(2));
				  }
				  result=result/StringList.get(p).length();
				  EntropyList.add(result);
				  //System.out.println("Density of Entropy is ,"+result);
				}
		}
		  return EntropyList;
		}
	
	/**
	 * Calculate the Ngrams frequencies
	 * @param StringList
	 * @return
	 */
	private static Vector<double[]> Ngrams(Vector<String> StringList){
		 Vector<double[]> NgramsList=new  Vector<double[]>();
		 /** Get the frequency of the characters */
		//System.out.println("case1, case2, case3, case4");
		for(int p=0;p<StringList.size();p++){
		String text=StringList.get(p);
		if(!IsNumber(text)){
			//System.out.println("reading "+text);
			int max=0;
			int k=0;
			int[] counts = new int[4]; // 4 classes of char
			    for (int i = 0; i < text.length(); i++){
			    if((int)text.charAt(i)==117 || (int)text.charAt(i)==120){
			    	 k=0;
			    	 counts[0]++; // Count the u and x
			    }else{
			    	if((int)text.charAt(i)<58 && (int)text.charAt(i)>47){
				    	 k=1;
				    	 counts[1]++; // Count the numbers
				    }else{
				    	if(((int)text.charAt(i)<91 && (int)text.charAt(i)>64) || ((int)text.charAt(i)<123 && (int)text.charAt(i)>96)){
					    	 k=2;
					    	 counts[2]++; // Count the alphabet characters
					    }else{
					    	k=3;
					    	counts[3]++; // Count the special characters
					    }		    	
				    }
			    }
			    
			   
			    if (counts[k]>max)max=counts[k]; 
			    }
			    double[] counts2 = new double[4]; // stores the frequencies
			   // System.out.println(" ");
				for (int i = 0; i < 4; i++){
					counts2[i]=(double)counts[i]/max; 
					//System.out.print(counts2[i]+", ");
				}   
				//System.out.println(" ");
				NgramsList.add(counts2);
				}
		}
		
		return NgramsList;
	
	}
	/**
	 * Extracts any shellcode present in the javascript
	 * @param script
	 * @return
	 */
	
	private static String extractShellCode(String script) {
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

		if(verb && str.length()>1)System.out.println("Extracted ShellCode: "+str+"\n");
		return str;
	}
/**
 * Checks if there is a NOPsled
 * @param script
 * @return
 */
	private static boolean nopCheck(String script) {
		String str = null;
		String nopString = null;
		int nopIndex = 0;

		nopString = "\\x0a\\x0a\\x0a\\x0a";
		nopIndex = script.indexOf(nopString);
		if (nopIndex != -1)
			str = "Malicious NOPsled Found-";
		else
			str = "";
		if (str.length()>1){
		return true;
		}else{
			return false;
		}
	}
	/**
	 * Signature detection based on the signatures in MalDB
	 * @param script
	 * @return
	 */

	private static boolean signatureCheck(String script) {		
		boolean test=false;
		String str=null;
		String shellString = null;
		String knownCodeStr = null;
		int shellIndex = 0 ;
		
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM MalCode;");
			if(verb) System.out.println("Connected to signature database...\n");			
			while (rs.next()) {
				//System.out.println("code= "+rs.getString("Code"));
				shellIndex = script.indexOf(rs.getString("Code"));
				if (shellIndex != -1){
					test=true;
					str = "Malicious Shell Code Detected by signature dictionary-";
					if(verb) System.out.println(str);			
				}
			}
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database. Please copy MalDB in the folder where the program is\n");
		}

		return test;
	}
/**
 * Function that performs the cosine similarity test
 * @param shellcode
 * @return
 */
	private static double SimilarityCheck(String shellcode) {		
		
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
			Connection conn = DriverManager.getConnection("jdbc:sqlite:MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM MalCode;");
			if(verb) System.out.println("Connected to signature database for cosine similarity...\n");						
			instrCount = dataModeling(shellcode,true);			
			
			int z=0;
			while (rs.next()) {
				knownCodeInstrCount = dataModeling(rs.getString("Code"),false);
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
				//System.out.println("Cosine_sim: "+Cosine_sim);
				
			}
			
			if(Max_Cosine_sim > Cosine_sim_level){
				str = "Malicious Shell Code Detected by cosine similarity-";
				//System.out.println("Malicious Shell Code Found");
			}
			if(verb) System.out.println("Max_Cosine_sim: "+Max_Cosine_sim);
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database. Please copy MalDB in the folder where the program is\n");
		}

		return Max_Cosine_sim;
	}
/**
 * Runs the dissassembler to calculate the frequencies of each opcode
 * @param str
 * @param test
 * @return
 */
	private static int[] dataModeling(String str,boolean test) {		
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
				if(verb && test)				
					System.out.println(line);
      			}
			input.close();		
		}
		catch (Exception err) {
		      err.printStackTrace();
    		}
		return instrCount;
	}

/**
 * Extracts all the strings related to dangerous functions
 * @param script
 * @return Vector or extracted strings
 */



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
				if(!arguments.isEmpty()){
					SubScript=SubScript.substring(arguments.length()-1);
					ExtractStrParBrack(arguments,StringsList,VariablesStack);			
					UpSearch(BackScript,StringsList,VariablesStack);
				}
			  }
		  }
	  }
	 
		/*
		for(i=0;i<StringsList.size();i++){
			System.out.println("extracted string "+StringsList.get(i));
		}
		for(i=0;i<VariablesStack.size();i++){
			System.out.println("extracted var stack "+VariablesStack.get(i));
		}  */
	  
	
	  return StringsList;
	  
	}
/**
 * 
 * Function that extracts all the arguments of a function
 */
private static String AllArgumentsExtraction (String script){
	if (script.length()>1){
	String arguments="";
	int counto = 0; // to count the opening (
	int countc = 0;  // to count the closing )
	boolean start=true;
	int j= -1;
	
	
	while(counto!=countc || start){
		start=false;
		if(j==script.length()-1) return "";
		j++;
		if (script.charAt(j) == '('){
			counto++;
		}
		if (script.charAt(j) == ')'){
			countc++;
		}
	}
	
	int k=1;
	if(j==0)k=0;
	arguments=script.substring(k,j); // return the arguments without the first '(' ')'
	//System.out.println("arguments "+arguments);
	return arguments;
	}else return "";
}
/**
 * Search for strings realted to a variable
 * @param BackScript space where to perform the search of strings
 * @param Strings vector where the strings are stored
 * @param VariablesStack variables to search
 */

private static void UpSearch(String BackScript,Vector<String> Strings,Stack<String> VariablesStack){
	//System.out.println("bacscript is "+BackScript);
	String var="";
	String script="";
	int position=0,end=0;
	while(!VariablesStack.isEmpty()){
		var=VariablesStack.pop();	
		//System.out.println("var is "+var);
		if(BackScript.contains(var) && BackScript.trim().length()>8 && var.length()>2){
			position=BackScript.indexOf(var)+var.length();
			script=BackScript.substring(position);
			end=script.indexOf(";");
			if(end!=-1)	script=script.substring(0,end);
			Stack<String> VarStack=new Stack<String>();
			ExtractStrParBrack(script,Strings,VarStack);
			UpSearch(script,Strings,VarStack);
		}
	}
	VariablesStack.removeAllElements();
	
	
}
/**
 * 
 * Method that extracts all the arguments in parenthesis and brackets
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
		

		while((counto!=countc || counto2!=countc2) && j<ExtractedString.length()){
			
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
			//System.out.println("length required "+j+ " length"+ExtractedString.length());
			ExtractStrParBrack(ExtractedString.substring(j,ExtractedString.length()),Strings,VariablesStack);
		}
		if(j==0) j++;
		
		if(ExtractedString.substring(0,j-1).contains("(") || ExtractedString.substring(0,j-1).contains("[")){
		ExtractStrParBrack(ExtractedString.substring(0,j-1),Strings,VariablesStack);
		ExtractedString=ExtractedString.substring(0,j-1);
		
		}else{
			ExtractedString=ExtractedString.substring(0,j-1);
			if(ExtractedString.length()>1){
			/*	if(ExtractedString.contains(".")){
					ExtractedString=ExtractedString.substring(0,ExtractedString.indexOf("."));
				}*/
				posmin=10000;
				for (int k=0;k<Separator.length;k++){
						if(ExtractedString.indexOf(Separator[k])<posmin && ExtractedString.contains(Separator[k])){
							posmin=ExtractedString.indexOf(Separator[k]);
							//System.out.println("posmin is "+posmin);
							test=Separator[k];
						}
					}
				if(posmin>=0 && 10000>posmin){
					//System.out.println("extra call "+posmin);
					ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VariablesStack);
					ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VariablesStack);
				}else{
					ExtractedString=ExtractedString.trim();
					//System.out.println("length required "+(j-1)+ " length"+ExtractedString.length());
					//ExtractedString=ExtractedString.substring(0,j-1);
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
							if(!Strings.contains(ExtractedString) && ExtractedString.length()>1) Strings.add(ExtractedString);
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
		/*if(ExtractedString.contains(".")){
			ExtractedString=ExtractedString.substring(0,ExtractedString.indexOf("."));
		}*/
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
			if(posmin>=0 && 10000>posmin){
				
				ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VariablesStack);
				if(posmin<ExtractedString.length()-2) ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VariablesStack);
			}else{
				ExtractedString=ExtractedString.trim();
				if(ExtractedString.length()>1){
				if (ExtractedString.charAt(0)=='"' && ExtractedString.charAt(ExtractedString.length()-1)=='"'){
					//System.out.println("extra call "+posmin+" length is "+ExtractedString.length());
					if(!Strings.contains(ExtractedString.substring(1, ExtractedString.length()-1)) && (ExtractedString.length()-2)>1 ) Strings.add(ExtractedString.substring(1, ExtractedString.length()-1));
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
						if(!Strings.contains(ExtractedString) && ExtractedString.length()>1) Strings.add(ExtractedString);
						//System.out.println("Strings.add(ExtractedString); "+ExtractedString.replaceAll("\\'", ""));
					}else{
						//System.out.println("VariablesStack.push2 "+ExtractedString);
						if(!VariablesStack.contains(ExtractedString)) VariablesStack.push(ExtractedString);
					}
				}					
			}
			}
		}
	}	
}





}