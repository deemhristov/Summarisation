package pl.waw.ipipan.zil.summarizer.extrsumannotator.basic;

import java.util.List;
import java.util.SortedSet;

public class Text {

	private String text = null;
	private List<SortedSet<Integer>> summaries;

	public Text(String string, List<SortedSet<Integer>> summaries) {
		this.text = string;
		this.summaries = summaries;
	}

	public String getText() {
		return text;
	}

	public int getTextWordsCount() {
		return getStringWordCount(text);
	}

	public static int getStringWordCount(String string) {
		return string.split("[ ]+").length;
	}

	public void setSummaries(List<SortedSet<Integer>> summaries) {
		this.summaries = summaries;
	}

	public SortedSet<Integer> getSummary(int number) {
		return summaries.get(number);
	}

	public List<SortedSet<Integer>> getSummaries() {
		return summaries;
	}

	public void setSummary(int summNumber, SortedSet<Integer> sum) {
		summaries.set(summNumber, sum);
	}
}
