package pl.waw.ipipan.zil.summarizer.clauseannotator.basic;

import pl.waw.ipipan.zil.summarizer.clauseannotator.main.ClauseAnnotator;

public class Word {

	private Clause clause = null;

	private String orth = null;
	private String pos;
	private String lemma;

	private Marker marker = null;

	public Word(String pos, String lemma) {
		this.pos = pos;
		this.lemma = lemma;
	}

	public void setOrth(String orth) {
		this.orth = orth;
	}

	public String toString() {
		return orth;
	}

	public String getOrth() {
		return this.orth;
	}

	public Clause getClause() {
		return this.clause;
	}

	public void setClause(Clause clause) {
		this.clause = clause;
	}

	public String getPos() {
		return this.pos;
	}

	public String getLemma() {
		return this.lemma;
	}

	public boolean isFirstInClause() {
		return this.clause.getFirstWord().equals(this);
	}

	public boolean isVerb() {
		if (ClauseAnnotator.VERB_POS_REGEXP == null)
			return false;
		return pos.matches(ClauseAnnotator.VERB_POS_REGEXP);
	}

	public Marker getMarker() {
		return this.marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public boolean isInMiddleOfMarker() {
		if (marker == null)
			return false;

		if (marker.getFirstWord().equals(this))
			return false;

		return true;
	}

}
