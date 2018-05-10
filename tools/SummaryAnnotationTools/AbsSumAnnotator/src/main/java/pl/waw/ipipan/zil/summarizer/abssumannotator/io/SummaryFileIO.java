package pl.waw.ipipan.zil.summarizer.abssumannotator.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.abssumannotator.basic.Text;
import pl.waw.ipipan.zil.summarizer.abssumannotator.main.AbsSumAnnotator;

public class SummaryFileIO {

	private static final String SUMMARIES = "#### SUMMARIES ####";
	private static final String SUMMARY_START = "#### SUMMARY START ####";
	private static final String SUMMARY_END = "#### SUMMARY END ####";

	private static final Logger logger = Logger.getLogger(SummaryFileIO.class);

	public static boolean saveSummary(File summaryFile, Text text) {
		logger.info("Saving text with summaries in file: " + summaryFile);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(summaryFile), "UTF-8"));

			bw.append(text.getText() + "\n");
			bw.append(SUMMARIES + "\n");

			for (String summary : text.getSummaries())
				writeSummary(bw, summary);

		} catch (IOException e) {
			logger.error("Error writing summary: " + e.getLocalizedMessage());
			return false;
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException e) {
					logger.error("Error closing summary file: " + e.getLocalizedMessage());
					return false;
				}
		}
		return true;
	}

	private static void writeSummary(BufferedWriter bw, String summary) throws IOException {
		bw.append(SUMMARY_START + "\n");
		bw.append(summary + "\n");
		bw.append(SUMMARY_END + "\n");
	}

	public static Text loadText(File input) {

		logger.info("Loading text with summaries from file: " + input);
		BufferedReader br = null;

		Text t = new Text();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
			String line = null;
			StringBuffer fullText = new StringBuffer();
			List<String> summaries = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (line.startsWith(SUMMARIES))
					summaries = loadSummaries(br);
				else
					fullText.append(line + "\n");
			}

			while (summaries.size() < AbsSumAnnotator.SUMMARIES_COUNT) {
				summaries.add("");
			}

			t.setSummaries(summaries);
			t.setText(fullText.toString());

		} catch (IOException e) {
			logger.error("Error reading text: " + e.getLocalizedMessage());
			return null;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				logger.error("Error closing text file: " + e.getLocalizedMessage());
				return null;
			}
		}

		return t;
	}

	private static List<String> loadSummaries(BufferedReader br) throws IOException {

		List<String> summaries = new ArrayList<String>();

		String summary = null;
		while ((summary = loadSummary(br)) != null)
			summaries.add(summary);

		return summaries;
	}

	private static String loadSummary(BufferedReader br) throws IOException {
		StringBuffer summary = new StringBuffer();

		String line = null;
		boolean inside = false;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(SUMMARY_END))
				break;

			if (inside)
				summary.append(line + "\n");

			if (line.startsWith(SUMMARY_START))
				inside = true;
		}

		if (summary.length() == 0 && !inside)
			return null;

		return summary.toString();
	}

}
