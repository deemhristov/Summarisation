package pl.waw.ipipan.zil.summarizer.abssumannotator.basic;

import java.util.List;

public class Text {

	private String text;

	private List<String> summaries;

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public int getTextWordsCount() {
		return getStringWordCount(text);
	}

	public int getSummaryWordsCount(int number) {
		return getStringWordCount(getSummary(number));
	}

	public static int getStringWordCount(String string) {
		return string.split("[ ]+").length;
	}

	public void setSummaries(List<String> summaries) {
		this.summaries = summaries;
	}

	public String getSummary(int number) {
		return summaries.get(number);
	}

	public List<String> getSummaries() {
		return summaries;
	}

	public void setSummary(int summNumber, String sum) {
		summaries.set(summNumber, sum);
	}
}
