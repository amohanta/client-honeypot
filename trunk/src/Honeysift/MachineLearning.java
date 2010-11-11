package Honeysift;
import net.htmlparser.jericho.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Math;
/**
 * 
 * Class that Implements the machine learning methods for code obfuscation detection
 * @author Damien
 *
 */

public class MachineLearning {

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
		
	
	public static String mainEngine(String script,String table,boolean verbose) {
		String returnString=null;
		String shellString=null;
		String str;
		verb=verbose;
		//-------------------------------------
		
		
		
		Vector<String>  Strings= StringExtraction(script);
		Learning(Strings,table);
		
	
		
		//Display of Analysis Engine result:
		if(verb)System.out.println("\n-----------Machine Learning  for this script done-------------" + "\n");
		return returnString;
	}
	/**
	 * Function that test whether a string a is a decimal number
	 * @param str string taht yoy want to test
	 * @return a boolean to know whether the string is a number
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
	 * Stores the probabilities in the learning database
	 * @param StringsList Results to store
	 * @param table : where to store the probabilities
	 */
	
	
	private static void Learning(Vector<String> StringsList,String table){
		Vector<double[]> Ngram=Ngrams(StringsList);
		Vector<Double> Entropies=calculateEntropy(StringsList);
		double leng=AverageLength(StringsList);
		double [] CurNgram;
		Double Entropy;
		//System.out.println("size is "+Entropies.size());
		for (int i=0;i<Entropies.size();i++){
		
		Entropy=Entropies.get(i);
		CurNgram=Ngram.get(i);
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:MalDB");	
			Statement stat = conn.createStatement();
			//System.out.println("connection made");
			//System.out.println("(Entropy,Length,NG1,NG2,NG3) "+Entropy+","+leng+","+CurNgram[0]+","+CurNgram[1]+","+CurNgram[3]);
			stat.executeQuery("INSERT INTO "+table+" (Entropy,Length,NG1,NG2,NG3) " +
					"VALUES (" + Entropy+","+leng+","+CurNgram[0]+","+CurNgram[1]+","+CurNgram[3]+");" );
			stat.close();
			conn.close();
			if(verb)System.out.println("Value added to learning database...\n");			
			
		}
		catch (Exception e) {
			//System.out.println("Error---Unable to connect to database\n");
		}
		}
		
	}
	/**
	 * Caclulate the average length of strings in the script
	 * @param StringList list of strings
	 * @return double
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
	 * Caciulate the entropy of each string in the list
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
				  if(verb) System.out.println("Density of Entropy is ,"+result);
				}
		}
		  return EntropyList;
		}
	
	/**
	 * calculate ngrams probabilities
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
	 * Extract all strings in the script
	 * @param script
	 * @return
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