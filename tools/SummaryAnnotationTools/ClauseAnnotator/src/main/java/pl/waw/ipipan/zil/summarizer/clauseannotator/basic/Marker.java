package pl.waw.ipipan.zil.summarizer.clauseannotator.basic;

import java.util.ArrayList;
import java.util.List;

public class Marker {

	private final List<Word> words = new ArrayList<Word>();

	private String nuc = "";
	private String connect = "";
	private String type = "";

	public Marker(List<Word> selectedWords) {
		for (Word w : selectedWords)
			add(w);
	}

	public void add(Word w) {
		this.words.add(w);
		w.setMarker(this);
	}

	public Marker(String nuc, String connect) {
		if (nuc != null)
			this.nuc = nuc;
		if (connect != null)
			setConnect(connect);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Word w : words)
			sb.append(" " + w.getOrth());

		return sb.substring(1);
	}

	public Clause getClause() {
		return this.words.get(0).getClause();
	}

	public void setNUC(String nuc) {
		this.nuc = nuc;
	}

	public String getNUC() {
		return nuc;
	}

	public Word getFirstWord() {
		return words.get(0);
	}

	public void deleteMe() {
		for (Word w : words)
			w.setMarker(null);
	}

	public List<Word> getWords() {
		return words;
	}

	public String getConnect() {
		return connect;
	}

	public String getType() {
		return type;
	}

	public void setConnect(String connect) {
		this.connect = connect;
		if (!connect.equals(""))
			this.type = "ext";
		else
			this.type = "int";
	}

}
