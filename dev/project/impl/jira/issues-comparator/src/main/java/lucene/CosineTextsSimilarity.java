package lucene;

import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.*;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.*;

import database.application.DatabaseApplication;
import similarity.TextsSimilarity;
import similarity.exceptions.SimilarityRangeException;

public class CosineTextsSimilarity implements TextsSimilarity
{

	public static final String CONTENT = "Content";

	private final Set<String> terms = new HashSet<>();
	private final RealVector v1;
	private final RealVector v2;
	private Logger logger;

	public CosineTextsSimilarity(String s1, String s2) throws IOException
	{
		Directory directory = createIndex(s1, s2);
		IndexReader reader = DirectoryReader.open(directory);
		Map<String, Integer> f1 = getTermFrequencies(reader, 0);
		Map<String, Integer> f2 = getTermFrequencies(reader, 1);
		reader.close();
		v1 = toRealVector(f1);
		v2 = toRealVector(f2);
	}

	public Directory createIndex(String text1, String text2) throws IOException {
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new SimpleAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		IndexWriter writer = new IndexWriter(directory, iwc);
		addDocument(writer, text1);
		addDocument(writer, text2);
		writer.close();
		return directory;
	}

	public void addDocument(IndexWriter writer, String content) throws IOException {
		Document doc = new Document();
		doc.add(new VecTextField(CONTENT, content, Store.YES));
		writer.addDocument(doc);
	}

	public double getSimilarity() {
		return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
	}

	@Override
	public double getSimilarity(String text1, String text2) {
		logger = Logger.getLogger(DatabaseApplication.class.getName());
		double similarity = 0.0;
		try {
			similarity = new CosineTextsSimilarity(text1, text2).getSimilarity();
			if(similarity < 0 || similarity > 1)
				throw new SimilarityRangeException();
		} catch (SimilarityRangeException e) {
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return similarity;
	}

	public Map<String, Integer> getTermFrequencies(IndexReader reader, int docId) throws IOException {
		Terms vector = reader.getTermVector(docId, CONTENT);
		TermsEnum termsEnum = null;
		termsEnum = vector.iterator();
		Map<String, Integer> frequencies = new HashMap<>();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null)
		{
			String term = text.utf8ToString();
			int freq = (int) termsEnum.totalTermFreq();
			frequencies.put(term, freq);
			terms.add(term);
		}
		return frequencies;
	}

	public RealVector toRealVector(Map<String, Integer> map) {
		RealVector vector = new ArrayRealVector(terms.size());
		int i = 0;
		for (String term : terms)
		{
			int value = map.containsKey(term) ? map.get(term) : 0;
			vector.setEntry(i++, value);
		}
		return (RealVector) vector.mapDivide(vector.getL1Norm());
	}
}