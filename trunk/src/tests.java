import net.htmlparser.jericho.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Math;

public class tests {
	
	
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
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String arguments="('Uul1ItLo['plunger'][CRrtOhOH] = GjL08iWK.substr(0, ('+ Qn2_R5kv[x8n9EKml] + '-6)/2);'')";
		int counto = 0; // to count the opening (
		int countc = 0;  // to count the closing )
		int j= 0,i=0,count=0;
		int position=0;
		Vector<String> StringsList=new Vector<String>();
		Vector<String> VarList=new Vector<String>();
		Stack<String> VariablesStack=new Stack<String>();
		boolean Parsing=true;
		String ExtractedString=arguments;
		ExtractStrParBrack(arguments,StringsList,VarList,VariablesStack);
		
		for(i=0;i<StringsList.size();i++){
			System.out.println(StringsList.get(i));
		}
		for(i=0;i<VariablesStack.size();i++){
			System.out.println(VariablesStack.get(i));
		}
		
	/*	while(arguments.length()>1){ // process as long as there are arguments
			count++;
			while (Parsing){
				if(arguments.charAt(0)==' ' || arguments.charAt(0)=='=' || arguments.charAt(0)=='+' || arguments.charAt(0)=='[' || arguments.charAt(0)==','){
					arguments=arguments.substring(1);
				}else{
					Parsing=false; // no need to parse anymore
				}
			}
			// extract arguments inside parenthesis
			
			// extract arguments inside brackets
			
			
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
			}*/
		// TODO Auto-generated method stub
		  // Create a pattern to match breaks
    /*    Pattern p = Pattern.compile("[,=']+");
        // Split input with the pattern
        String[] result = 
                 p.split(arguments);
        for (i=0; i<result.length; i++)
            System.out.println(result[i]);*/
    }


private static void ExtractStrParBrack(String arguments,Vector<String> Strings,Vector<String> VarList,Stack<String> VariablesStack){
	
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
			
			ExtractStrParBrack(ExtractedString.substring(0,position),Strings,VarList,VariablesStack);
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
			ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VarList,VariablesStack);
			//System.out.println("first string "+ExtractedString.substring(0,posmin));
		}
		
		if(ExtractedString.substring(j,ExtractedString.length()).contains("(") || ExtractedString.substring(j,ExtractedString.length()).contains("[")){
		ExtractStrParBrack(ExtractedString.substring(j,ExtractedString.length()),Strings,VarList,VariablesStack);
		}
		if(ExtractedString.substring(0,j-1).contains("(") || ExtractedString.substring(0,j-1).contains("[")){
		ExtractStrParBrack(ExtractedString.substring(0,j-1),Strings,VarList,VariablesStack);
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
					ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VarList,VariablesStack);
					ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VarList,VariablesStack);
				}else{
					ExtractedString=ExtractedString.trim();
					ExtractedString=ExtractedString.substring(0,j-1);
					if (ExtractedString.charAt(0)=='"' && ExtractedString.charAt(ExtractedString.length()-1)=='"'){
						Strings.add(ExtractedString);
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
							Strings.add(ExtractedString);
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
				ExtractStrParBrack(ExtractedString.substring(0,posmin),Strings,VarList,VariablesStack);
				ExtractStrParBrack(ExtractedString.substring(posmin+1,ExtractedString.length()),Strings,VarList,VariablesStack);
			}else{
				ExtractedString=ExtractedString.trim();
				if (ExtractedString.charAt(0)=='"' && ExtractedString.charAt(ExtractedString.length()-1)=='"'){
					Strings.add(ExtractedString);
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
						Strings.add(ExtractedString);
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

}