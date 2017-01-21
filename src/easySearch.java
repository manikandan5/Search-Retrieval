import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class easySearch 
{
	public static LinkedHashMap<String,String> term_docfreq= new LinkedHashMap<String,String>();
	public static LinkedHashMap<Integer,String> doc_doclength= new LinkedHashMap<Integer,String>(); 
	public static LinkedHashMap<Integer,String> doc_docnumber= new LinkedHashMap<Integer,String>(); 
	public static LinkedHashMap<Integer,String> doc_docscore=new LinkedHashMap<Integer,String>();
	
	public static void main(String[] args) throws IOException, ParseException
	{
		String indexLocation = System.getProperty("user.dir")+"/src/index";
		String outputLocation = System.getProperty("user.dir")+"/src/Task1Output.txt";
		System.out.println();
		System.out.print("Enter the query term : ");
		Scanner sc = new Scanner(System.in);
		String queryTerm = sc.nextLine();
		search(indexLocation, queryTerm, "1", outputLocation, false);
		sc.close();
		System.out.println("Output saved to the location :" + outputLocation);
	}
	
	public static void search(String indexLocation, String queryTerm, String queryNumber, String outputLocation, Boolean limit ) throws IOException, ParseException
	{
		//Output file which contains the rank document
		File rankDocument= new File(outputLocation);
		FileWriter outputWriter = new FileWriter(rankDocument, false);
		BufferedWriter outputWriterBuffer = new BufferedWriter(outputWriter);
		
		//Index Reader
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
		int totalDoc = reader.maxDoc();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(queryTerm);

		Set<Term> queryTerms=new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query,false).extractTerms(queryTerms);
				
		System.out.println("Terms in the query: ");
		
		for (Term t : queryTerms) 	
		{
			int docFreq=reader.docFreq(new Term("TEXT", t.text()));
			System.out.println("The term  \"" +t.text()+ "\" occurs in a total of : " +docFreq +" documents.");
			term_docfreq.put(t.text(),Integer.toString(docFreq));
		}
				
		ClassicSimilarity dSimi = new ClassicSimilarity();
		List<LeafReaderContext> leafContexts =reader.getContext().reader().leaves();
		
		for (int i = 0; i < leafContexts.size(); i++) 
		{
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			for (int docId = 0; docId < numberOfDoc; docId++)
			{
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
				float docLeng = 1 / (normDocLeng * normDocLeng);
				doc_doclength.put(docId+startDocNo,Float.toString(docLeng));
				doc_docnumber.put(docId +startDocNo, searcher.doc(docId +startDocNo).get("DOCNO"));
			}
		}
		
		DecimalFormat f = new DecimalFormat("##.000000000");
			
		for (Term t : queryTerms) 
		{
			int frequencycount = 0;
			float termfreq = 0;
			for (int i = 0; i < leafContexts.size(); i++) 
			{
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				
				@SuppressWarnings("unused")
				int doc=0;
				if (de != null) 
				{
					while ((doc = de.nextDoc()) !=PostingsEnum.NO_MORE_DOCS)
					{
						frequencycount=de.freq();
						
					    termfreq = frequencycount/(Float.parseFloat(doc_doclength.get(de.docID()+startDocNo)));	//Calculating TF
				    
						if(doc_docscore.containsKey((de.docID()+startDocNo))&& (doc_docscore.get(de.docID()) != null))	//Calculating Score for each DOC	
						{
							doc_docscore.put((de.docID()+startDocNo),String.valueOf(f.format(Float.parseFloat((doc_docscore.get(de.docID()+startDocNo))) + (termfreq * Math.log(1 + (totalDoc/Integer.parseInt(term_docfreq.get(t.text()))))))));
						}
						else
						{
							doc_docscore.put((de.docID()+startDocNo),String.valueOf(f.format(((termfreq * Math.log(1 + (totalDoc/Integer.parseInt(term_docfreq.get(t.text())))))))));
						}
					}		
				}
			}
		}
		
		@SuppressWarnings({ "unchecked" })
		Map<Integer, String> sortedmap = sortByValues_Dec(doc_docscore);
		int counter=0;		
		Iterator <Map.Entry<Integer,String>> iter = sortedmap.entrySet().iterator();
		
		while (iter.hasNext())
		{
			Entry<Integer,String> pair = iter.next();
			counter+=1;
			if(queryNumber == "" || doc_docnumber.get(pair.getKey()) == "" || pair.getValue()== "" )
			{
				break;
			}
			outputWriterBuffer.write("" + queryNumber + " " + "Q0" + " " + doc_docnumber.get(pair.getKey())+ " " + counter + " " + pair.getValue()+ " "  + "EasySearch\r\n");
			if(counter== 1000)
				break;
			
		}
		
		/*while (iter.hasNext())
		{
			Entry<Integer,String> pair = iter.next();
			counter+=1;
			if(limit)
			{
				outputWriterBuffer.write("" + queryNumber + " " + "Q0" + " " + doc_docnumber.get(pair.getKey())+ " " + counter + " " + pair.getValue()+ " "  + "run-1"+"\r\n");
				
				if(counter== 1000)
					break;
			}
			else
			{
				outputWriterBuffer.write("Relevance score for the Doc "+ doc_docnumber.get(pair.getKey())  + " : " + pair.getValue() + " Rank : " +(counter) + "\n");
			}
		}*/
		outputWriter.close();
	}
			
	//Function to sort MAP	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap sortByValues_Dec(HashMap<Integer,String> map) 
	{ 
       List list = new LinkedList(map.entrySet());
       Collections.sort(list, new Comparator() 
       {
            public int compare(Object o1, Object o2) 
            {
               return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
       });
       
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) 
       {
    	   Map.Entry entry = (Map.Entry) it.next();
    	   sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
	}	
}
