package pl.waw.ipipan.zil.summarizer.clauseannotator.basic;

import java.util.ArrayList;
import java.util.List;

public class Document {

	private List<Sentence> sentences = new ArrayList<Sentence>();

	public void add(Sentence currentSentence) {
		this.sentences.add(currentSentence);
		currentSentence.setDocument(this);
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public List<Sentence> getSentenceNeighbourhood(Sentence s) {
		int pos = -1;
		for (int i = 0; i < sentences.size(); i++) {
			if (sentences.get(i).equals(s)) {
				pos = i;
				break;
			}
		}

		int first = Math.max(0, pos - 1);
		int last = Math.min(sentences.size() - 1, pos + 1);

		return new ArrayList<Sentence>(sentences.subList(first, last + 1));
	}

	public void splitSentence(Sentence s, Word selectedWord) {
		int pos = -1;
		for (int i = 0; i < sentences.size(); i++) {
			if (sentences.get(i).equals(s)) {
				pos = i;
				break;
			}
		}
		sentences.remove(pos);

		s.splitClause(selectedWord.getClause(), selectedWord);
		List<Clause> clauses1 = s.getClausesBefore(selectedWord.getClause());
		List<Clause> clauses2 = new ArrayList<Clause>(s.getClauses());
		clauses2.removeAll(clauses1);

		Sentence s1 = new Sentence();
		Sentence s2 = new Sentence();
		for (Clause c : clauses1)
			s1.add(c);
		for (Clause c : clauses2)
			s2.add(c);

		sentences.add(pos, s1);
		sentences.add(pos + 1, s2);
		s1.setDocument(this);
		s2.setDocument(this);
	}

}
