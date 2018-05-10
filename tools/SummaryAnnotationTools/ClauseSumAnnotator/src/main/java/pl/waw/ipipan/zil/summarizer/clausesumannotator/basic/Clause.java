package pl.waw.ipipan.zil.summarizer.clausesumannotator.basic;

import java.util.ArrayList;
import java.util.List;

public class Clause {

	private List<Word> words = new ArrayList<Word>();
	private String id;
	private String cont;

	public Clause(String id, String cont) {
		this.id = id;
		this.cont = cont;
	}

	public void add(Word currentWord) {
		this.words.add(currentWord);
	}

	public String getId() {
		return this.id;
	}

	public List<Word> getWords() {
		return words;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Word w : words)
			sb.append(" " + w.getOrth());
		return sb.toString();
	}

	public String getContinue() {
		return this.cont;
	}

}
