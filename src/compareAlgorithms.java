
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

public class compareAlgorithms 
{
	public static String IndexLocation = System.getProperty("user.dir")+"/src/index";
	public static String queryFileLoaction = System.getProperty("user.dir")+ "/src/topics.51-100";
	
	private static List<String> shortQuery = new ArrayList<> ();
	private static List<String> longQuery = new ArrayList<> ();
	private static List<String> queryNum = new ArrayList<>();
	
	public static void addTags(String tagIndexTag, String startIndexTag, String endIndexTag, String query, String contentSplit, String replaceStr)
	{
		int startIndex, endIndex, TagIndex = 0;
		String tags = "";
		
		TagIndex = contentSplit.indexOf(tagIndexTag,TagIndex);
		startIndex = TagIndex+startIndexTag.length();
		endIndex = contentSplit.indexOf(endIndexTag, startIndex);
		tags = contentSplit.substring(startIndex,endIndex);		
		tags = tags.replaceAll(replaceStr,"");
		
		if (query == "shortQuery")
		{
			shortQuery.add(tags.trim());
		}
		else if(query == "longQuery")
		{
			longQuery.add(tags.trim());
		}
		else if(query == "queryNum")
		{
			if (tags.startsWith("0"))
    		{
    			tags=tags.substring(1);
    		}
    		queryNum.add(tags.trim());
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException
	{
		String content = new String(Files.readAllBytes(Paths.get(queryFileLoaction)));
		
		
		String[] contentSplit= content.split("</top>"); 
		int totalDoc = contentSplit.length-1;
	    
	    String shortQueryVSM = System.getProperty("user.dir")+"/src/shortQueryVSM.txt";
	    String longQueryVSM = System.getProperty("user.dir")+"/src/longQueryVSM.txt";

	    String shortQueryBM25 = System.getProperty("user.dir")+"/src/shortQueryBM25.txt";
	    String longQueryBM25 = System.getProperty("user.dir")+"/src/longQueryBM25.txt";

	    String shortQueryLMDS = System.getProperty("user.dir")+"/src/shortQueryLMDS.txt";
	    String longQueryLMDS = System.getProperty("user.dir")+"/src/longQueryLMDS.txt";

	    String shortQueryLMJMS = System.getProperty("user.dir")+"/src/shortQueryLMJMS.txt";
	    String longQueryLMJMS = System.getProperty("user.dir")+"/src/longQueryLMJMS.txt";

		for (int i=0; i<totalDoc; i++)
		{
			addTags("<title>", "<title> Topic:", "<desc>", "shortQuery", contentSplit[i], "[\\/:&]");
			addTags("<desc> ", "<desc> Description:", "<smry>", "longQuery", contentSplit[i], "[\\/:&]");
			addTags("<num> ", "<num> Number:", "<dom> ", "queryNum", contentSplit[i], "[:]");
		}
	
		if(queryNum.size()==shortQuery.size())
		{
			System.out.println("Processing Vector Space Models for Short Queries :");
			for(int i=0; i<shortQuery.size(); i++)
			{
				VSM_Search.search( IndexLocation, shortQuery.get(i), queryNum.get(i), shortQueryVSM, "VSM_short");
			}
			System.out.println("Results saved to :" + shortQueryVSM);
			
			System.out.println("Processing BM25 for Short Queries :");
			for(int i=0;i<shortQuery.size();i++)
			{
				BM25_Search.search( IndexLocation, shortQuery.get(i), queryNum.get(i), shortQueryBM25, "BM25_short");
			}
			System.out.println("Results saved to :" + shortQueryBM25);
		
			System.out.println("Processing Language Model with Dirichlet Smoothing for Short Queries :");
			for(int i=0;i<shortQuery.size();i++)
			{	
				LMDS_Search.search( IndexLocation, shortQuery.get(i), queryNum.get(i), shortQueryLMDS, "LMDS_short");
			}
			System.out.println("Results saved to :" + shortQueryLMDS);
			
			System.out.println("Processing Language Model with Jelinek Mercer Smoothing for Short Queries :");
			for(int i=0;i<shortQuery.size();i++)
			{
				LMJMS_Search.search( IndexLocation, shortQuery.get(i), queryNum.get(i), shortQueryLMJMS, "LMJSM_short");
			}
			System.out.println("Results saved to :" + shortQueryLMJMS);
		}
		
		if(queryNum.size() == longQuery.size())
		{
			System.out.println("Processing Vector Space Models for Long Queries :");
			for(int i=0; i<longQuery.size(); i++)
			{
				VSM_Search.search( IndexLocation, longQuery.get(i), queryNum.get(i), longQueryVSM, "VSM_long");
			}
			System.out.println("Results saved to :" + longQueryVSM);
			
			System.out.println("Processing BM25 for Long Queries :");
			for(int i=0;i<longQuery.size();i++)
			{
				BM25_Search.search( IndexLocation, longQuery.get(i), queryNum.get(i), longQueryBM25, "BM25_long");
			}
			System.out.println("Results saved to :" + longQueryBM25);
			
			System.out.println("Processing Language Model with Dirichlet Smoothing for Long Queries :");
			for(int i=0;i<longQuery.size();i++)
			{
				LMDS_Search.search( IndexLocation, longQuery.get(i), queryNum.get(i), longQueryLMDS, "LMDS_long");
			}
			System.out.println("Results saved to :" + longQueryLMDS);
			
			System.out.println("Processing Language Model with Jelinek Mercer Smoothing for Long Queries :");
			for(int i=0;i<longQuery.size();i++)
			{
				LMJMS_Search.search( IndexLocation, longQuery.get(i), queryNum.get(i), longQueryLMJMS, "LMJMS_long");
			}
			System.out.println("Results saved to :" + longQueryLMJMS);
		}
	}
}