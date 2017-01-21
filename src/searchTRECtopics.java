import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

public class searchTRECtopics 
{
	private static List<String> shortQuery = new ArrayList<> ();
	private static List<String> longQuery = new ArrayList<> ();
	private static List<String> queryNum = new ArrayList<>();
	private static String IndexLocation = System.getProperty("user.dir")+"/src/index";
	private static String queryFileLoaction = System.getProperty("user.dir")+ "/src/topics.51-100";
	private static String shortFileLocation = System.getProperty("user.dir")+"/src/shortQueriesES.txt";
	private static String longFileLocation = System.getProperty("user.dir")+"/src/longQueriesES.txt";
	
	public static void addTags(String tagIndexTag, String startIndexTag, String endIndexTag, String query, String contentSplit, String replaceStr)
	{
		int startIndex, endIndex, TagIndex = 0;
		String tags = "";
		
		TagIndex = contentSplit.indexOf(tagIndexTag,TagIndex);
		startIndex = TagIndex+startIndexTag.length();
		endIndex = contentSplit.indexOf(endIndexTag, startIndex);
		tags = contentSplit.substring(startIndex,endIndex);		
		tags=tags.replaceAll(replaceStr,"");
		
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
		
		String[] contentSplit = content.split("</top>"); 
		
		int totalDoc = contentSplit.length-1;
		
		for (int i=0; i < totalDoc; i++)
		{
			addTags("<title>", "<title> Topic:", "<desc>", "shortQuery", contentSplit[i], "[\\/:&]");
			
			addTags("<desc> ", "<desc> Description:", "<smry>", "longQuery", contentSplit[i], "[\\/:&]");
			
			addTags("<num> ", "<num> Number:", "<dom> ", "queryNum", contentSplit[i], "[:]");
		}
		
		System.out.println("Processing Short Queries");
		if(queryNum.size()==shortQuery.size())
		{
			for(int queryNo=0; queryNo<shortQuery.size(); queryNo++)
			{
				easySearch.search(IndexLocation, shortQuery.get(queryNo), queryNum.get(queryNo), shortFileLocation, true);
			}
		}
		System.out.println("Short queries output is saved at :" + shortFileLocation);
		
		System.out.println("Processing Long Queries");
		if(queryNum.size()==longQuery.size())
		{
			for(int queryNo=0;queryNo<longQuery.size();queryNo++)
			{
				easySearch.search(IndexLocation, longQuery.get(queryNo), queryNum.get(queryNo), longFileLocation, true);
			}
		}
		System.out.println("Long queries output is saved at :" + longFileLocation);
	}
}