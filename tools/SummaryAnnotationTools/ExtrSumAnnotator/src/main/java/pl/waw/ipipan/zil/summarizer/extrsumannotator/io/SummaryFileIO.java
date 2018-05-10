package pl.waw.ipipan.zil.summarizer.extrsumannotator.io;

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
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.extrsumannotator.basic.Text;
import pl.waw.ipipan.zil.summarizer.extrsumannotator.main.ExtrSumAnnotator;

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

			for (SortedSet<Integer> sum : text.getSummaries()) {
				bw.append(SUMMARY_START + "\n");
				for (Integer idx : sum)
					bw.append(idx + " ");
				bw.append("\n");
				bw.append(SUMMARY_END + "\n");
			}

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

	public static Text loadText(File input) {

		logger.info("Loading text with summaries from file: " + input);
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF-8"));
			String line = null;
			StringBuffer fullText = new StringBuffer();
			List<SortedSet<Integer>> summaries = new ArrayList<SortedSet<Integer>>();
			while ((line = br.readLine()) != null) {
				if (line.startsWith(SUMMARIES))
					loadSummaries(br, summaries);
				else
					fullText.append(line + "\n");
			}

			while (summaries.size() < ExtrSumAnnotator.SUMMARIES_SIZES.length) {
				summaries.add(new TreeSet<Integer>());
			}

			Text t = new Text(fullText.toString(), summaries);
			return t;

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
	}

	private static void loadSummaries(BufferedReader br, List<SortedSet<Integer>> summaries) throws IOException {
		SortedSet<Integer> summary = null;
		while ((summary = loadSummary(br)) != null)
			summaries.add(summary);
	}

	private static SortedSet<Integer> loadSummary(BufferedReader br) throws IOException {
		SortedSet<Integer> summary = new TreeSet<Integer>();

		String line = null;
		boolean inside = false;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(SUMMARY_END))
				break;

			if (inside) {
				Scanner sc = new Scanner(line);
				while (sc.hasNextInt())
					summary.add(sc.nextInt());
				sc.close();
			}

			if (line.startsWith(SUMMARY_START))
				inside = true;
		}

		if (summary.size() == 0 && !inside)
			return null;

		return summary;
	}

}
