import net.htmlparser.jericho.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.lang.Math;

public class AnalysisEngine {
	public static String mainEngine(String script) {
		String returnString=null;
		String shellString=null;
		String str;

		//-------------------------------------
		
		shellString=extractShellCode(script);

		//-analysis-
		str = nopCheck(shellString);
		if (str != null)
			returnString = "\n" + str;
		
		str = signatureCheck(shellString);
		if (str != null)
			returnString += "\n" + str;
		
		str = obfuscationCheck(shellString);
		if (str != null)
			returnString += "\n" + str;

		//-------------------------------------
		if (str == null)
			returnString = "- Benign ";

		return returnString;
	}

	private static String extractShellCode(String script)
	{
		String str = "";

		int len = script.length();
		for (int i = 0; i < len; i++)
		{
			if (script.charAt(i) == '\\' && script.charAt(i + 1) == 'x')
				str += "\\x" + script.charAt(i + 2) + script.charAt(i + 3);
			if (script.charAt(i) == '%' && script.charAt(i + 1) == 'u') {
				str += "\\x" + script.charAt(i + 2) + script.charAt(i + 3); 
				str += "\\x" + script.charAt(i + 4) + script.charAt(i + 5);
			}
		}
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
		//else
		//	System.out.println("NOP not found :");
		
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

		//shellString = "%u9fc0%u7807%uefef%u66ef%uf3aa%u2a64%u2f6c%u66bf%ucfaa%u1087%uefef%ubfef%uaa64%u85fb";
		//shellIndex = script.indexOf(shellString);
		//if (shellIndex != -1)
		//	str = "Malicious Shell Code Detected-";
		//else
		//	System.out.println("Signature not detected :");
		return str;
	}

	private static String obfuscationCheck(String shellcode) {		
		String str=null;		
		double Sum_XY, Sum_DenomX, Sum_DenomY, Cosine_sim, Max_Cosine_sim, Cosine_sim_level;		
		int[] instrCount=new int[Global.N];
		int[] knownCodeInstrCount=new int[Global.N];	
		for(int i=0; i<Global.N; i++) {				
			instrCount[i] = 0;
			knownCodeInstrCount[i] = 0;
		}
		Max_Cosine_sim = 0;
		Cosine_sim_level = 0.6;
		
		try {			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:/home/hoys/workspace/sqliteDB/MalDB");	
			Statement stat = conn.createStatement();
			ResultSet rs=stat.executeQuery("SELECT * FROM MalCode;");
			System.out.println("Connected to signature database for cosine similarity...\n");			
			while (rs.next()) {
				knownCodeInstrCount = dataModeling(rs.getString("Code"));
				instrCount = dataModeling(shellcode);
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
			if(Max_Cosine_sim > Cosine_sim_level)
				str = "Malicious Shell Code Detected by cosine similarity-";
			rs.close();
			conn.close();
		}
		catch (Exception e) {
			System.out.println("Error---Unable to connect to database\n");
		}

		/*
		String knownCodeStr="\\x31\\xc0\\xb0\\x25\\x6a\\xff\\x5b\\xb1\\x09\\xcd\\x80";

		knownCodeInstrCount = dataModeling(knownCodeStr);
		instrCount = dataModeling(shellcode);

		//Computing cosine similarity
		double Sum_XY, Sum_DenomX, Sum_DenomY, Cosine_sim;
		Sum_XY = Sum_DenomX = Sum_DenomY = Cosine_sim = 0;
		for(int i=0; i< Global.N; i++) {				
			Sum_XY += knownCodeInstrCount[i] * instrCount[i];
			Sum_DenomX += knownCodeInstrCount[i] * knownCodeInstrCount[i];
			Sum_DenomY += instrCount[i] * instrCount[i];					
		}
		Cosine_sim = Sum_XY / Math.sqrt(Sum_DenomX * Sum_DenomY);

		System.out.println("Cosine_sin: "+Cosine_sim);
		//for(int i=0; i<N; i++) {				
		//	System.out.print(instrString[i]+" : "+instrCount[i]+" ");
		//}*/
		return str;
	}

	private static int[] dataModeling(String str) {		
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
				//System.out.println(line);
      			}
			input.close();		
		}
		catch (Exception err) {
		      err.printStackTrace();
    		}
		return instrCount;
	}

}
