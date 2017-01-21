import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;

public class VSM_Search 
{
	public static void search(String IndexLocation, String queryTerm, String queryNum, String outputLocation, String queryType) throws IOException, ParseException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexLocation)));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryTerm);
		searcher.setSimilarity(new ClassicSimilarity());
		TopDocs results = searcher.search(query, 1000);
		ScoreDoc[] hits = results.scoreDocs;
		
		File output= new File(outputLocation);
		FileWriter outputWriter = new FileWriter(output, false);
		BufferedWriter outputWriterBuffer = new BufferedWriter(outputWriter);
		
		if (!output.exists()) 
		{
			output.createNewFile();
		}
		for(int i=0;i<hits.length;i++)
		{
			Document doc=searcher.doc(hits[i].doc);
			outputWriterBuffer.write("" + queryNum + " " + "0" + " " + doc.get("DOCNO")+ " " + (i+1) + " " + hits[i].score+ " "  + queryType+"\r\n");
		}	
		outputWriterBuffer.close();
		outputWriter.close();
	}
}
