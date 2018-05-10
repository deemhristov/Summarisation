package pl.waw.ipipan.zil.summarizer.clauseannotator.io;

import java.util.HashMap;
import java.util.Map;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class IdGenerator {

	private int nextWordId = 0;
	private int nextClauseId = 0;
	private int nextSentenceId = 0;
	private int nextMarkerId = 0;

	private Map<Word, String> word2Id = new HashMap<Word, String>();
	private Map<Clause, String> clause2Id = new HashMap<Clause, String>();
	private Map<Sentence, String> sentence2Id = new HashMap<Sentence, String>();
	private Map<Marker, String> marker2Id = new HashMap<Marker, String>();

	public String getId(Word w) {
		if (word2Id.containsKey(w))
			return word2Id.get(w);

		String newId = "W" + nextWordId++;
		word2Id.put(w, newId);
		return newId;
	}

	public String getId(Marker m) {
		if (marker2Id.containsKey(m))
			return marker2Id.get(m);

		String newId = "MARKER" + nextMarkerId++;
		marker2Id.put(m, newId);
		return newId;
	}

	public String getId(Sentence s) {
		if (sentence2Id.containsKey(s))
			return sentence2Id.get(s);

		String newId = "S" + nextSentenceId++;
		sentence2Id.put(s, newId);
		return newId;
	}

	public String getId(Clause c) {
		if (clause2Id.containsKey(c))
			return clause2Id.get(c);

		String newId = "CLAUSE" + nextClauseId++;
		clause2Id.put(c, newId);
		return newId;
	}
}
