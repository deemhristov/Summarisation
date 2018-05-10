package pl.waw.ipipan.zil.summarizer.clauseannotator.basic;

import java.util.ArrayList;
import java.util.List;

public class Clause {

	private Sentence sentence;
	private final List<Word> words = new ArrayList<Word>();

	private Clause cont = null;

	public void add(Word currentWord) {
		this.words.add(currentWord);
		currentWord.setClause(this);
	}

	public void addFirst(Word currentWord) {
		this.words.add(0, currentWord);
		currentWord.setClause(this);
	}

	public List<Word> getWords() {
		return words;
	}

	public Sentence getSentence() {
		return this.sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public List<Clause> split(Word w) {
		Clause c1 = new Clause();
		Clause c2 = new Clause();

		boolean toC1 = true;
		for (Word word : words) {
			if (toC1) {
				if (word.equals(w)) {
					toC1 = false;
					c2.add(word);
				} else {
					c1.add(word);
				}
			} else {
				c2.add(word);
			}
		}

		ArrayList<Clause> l = new ArrayList<Clause>();
		l.add(c1);
		l.add(c2);
		return l;
	}

	public Clause getContinuedClause() {
		return cont;
	}

	public boolean isFirstInSentence() {
		return this.sentence.getClauses().get(0).equals(this);
	}

	public boolean isLastInSentence() {
		return this.sentence.getLastClause().equals(this);
	}

	public String toTrimmedString(int len) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Word w : words)
			if (first) {
				sb.append(w.toString());
				first = false;
			} else {
				sb.append(" " + w.toString());
			}

		String trimmed = sb.toString().substring(0, Math.min(sb.toString().length(), len));

		return sb.toString().length() < len ? trimmed : trimmed + "...";
	}

	@Override
	public String toString() {
		return toTrimmedString(80);
	}

	public void setContinuedClause(Clause clause) {
		this.cont = clause;
	}

	public List<Word> getWordsSequence(Word startWord, Word stopWord) {
		int start = -1;
		int stop = -1;
		for (int i = 0; i < words.size(); i++) {
			Word w = words.get(i);
			if (w.equals(startWord)) {
				start = i;
			}
			if (w.equals(stopWord)) {
				stop = i;
				break;
			}
		}

		return new ArrayList<Word>(this.words.subList(start, stop + 1));
	}

	public Word getFirstWord() {
		return this.words.get(0);
	}

}
