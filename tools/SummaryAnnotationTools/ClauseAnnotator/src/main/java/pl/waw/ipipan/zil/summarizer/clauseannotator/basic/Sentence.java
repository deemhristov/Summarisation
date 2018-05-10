package pl.waw.ipipan.zil.summarizer.clauseannotator.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sentence {

	private List<Clause> clauses = new ArrayList<Clause>();
	private Document document;

	public void add(Clause clause) {
		this.clauses.add(clause);
		clause.setSentence(this);
	}

	public List<Clause> getClauses() {
		return clauses;
	}

	public void splitClause(Clause c, Word w) {
		int pos = -1;
		for (int i = 0; i < clauses.size(); i++) {
			if (clauses.get(i).equals(c)) {
				pos = i;
				break;
			}
		}
		List<Clause> split = c.split(w);
		deleteClause(c);

		clauses.addAll(pos, split);
		for (Clause cl : split) {
			cl.setSentence(this);
		}
	}

	public void mergeClauseWithPrevious(Clause c) {
		int pos = -1;
		for (int i = 0; i < clauses.size(); i++) {
			if (clauses.get(i).equals(c)) {
				pos = i;
				break;
			}
		}
		Clause prevClause = clauses.get(pos - 1);
		for (Word w : c.getWords())
			prevClause.add(w);

		deleteClause(c);
	}

	public void mergeClauseWithNext(Clause c) {
		int pos = -1;
		for (int i = 0; i < clauses.size(); i++) {
			if (clauses.get(i).equals(c)) {
				pos = i;
				break;
			}
		}
		Clause nextClause = clauses.get(pos + 1);
		List<Word> words = new ArrayList<Word>(c.getWords());
		Collections.reverse(words);
		for (Word w : words)
			nextClause.addFirst(w);

		deleteClause(c);
	}

	private void deleteClause(Clause c) {
		for (Clause clause : clauses)
			if (c.equals(clause.getContinuedClause()))
				clause.setContinuedClause(null);

		clauses.remove(c);
	}

	public List<Clause> getClausesBefore(Clause c) {
		int pos = 0;
		for (int i = 0; i < clauses.size(); i++)
			if (clauses.get(i).equals(c)) {
				pos = i;
				break;
			}

		return new ArrayList<Clause>(clauses.subList(0, pos));
	}

	public Clause getLastClause() {
		return clauses.get(clauses.size() - 1);
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return this.document;
	}
}
