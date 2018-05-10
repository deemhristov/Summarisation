package pl.waw.ipipan.zil.summarizer.clausesumannotator.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clausesumannotator.main.MyTab;

public class SummaryFileIO {

	private static final Logger logger = Logger.getLogger(SummaryFileIO.class);

	public static boolean loadSummary(File summaryFile, JTabbedPane tabbedPane) {
		logger.info("Loading summary from file: " + summaryFile);
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(summaryFile));
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				MyTab tab = (MyTab) tabbedPane.getComponent(i);
				readTab(br, tab);
			}
		} catch (IOException e) {
			logger.error("Error reading summary: " + e.getLocalizedMessage());
			return false;
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				logger.error("Error closing summary file: " + e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	public static boolean saveSummary(File summaryFile, JTabbedPane tabbedPane) {
		logger.info("Saving summary in file: " + summaryFile);
		BufferedWriter bw = null;

		try {
			bw = new BufferedWriter(new FileWriter(summaryFile));

			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				MyTab tab = (MyTab) tabbedPane.getComponent(i);
				writeTab(bw, tab);
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

	private static void readTab(BufferedReader br, MyTab tab) throws IOException {
		br.readLine();

		Set<String> clauseIds = new HashSet<String>();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			if (line.equals(""))
				break;
			clauseIds.add(line);
		}

		tab.setChosenClauses(clauseIds);

		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			if (line.equals(""))
				break;
		}
	}

	private static void writeTab(Writer writer, MyTab tab) throws IOException {
		writer.append("A " + tab.getName() + " should contain:\n");

		int maxContLen = 0;
		int maxIdLen = 0;
		for (Clause c : tab.getChosenClauses()) {
			int contLen = getCont(c).length();
			int idLen = getId(c).length();
			if (contLen > maxContLen)
				maxContLen = contLen;
			if (idLen > maxIdLen)
				maxIdLen = idLen;

			writer.append(c.getId() + "\n");
		}

		writer.append("\n");

		for (Clause c : tab.getChosenClauses()) {
			String id = fill(getId(c), maxIdLen + 1);
			String cont = fill(getCont(c), maxContLen + 1);
			String full = id + " " + cont + " " + c.toString();
			writer.append(full + "\n");
		}

		writer.append("\n");
	}

	public static File getTargetFile(File currentFile) {
		File parent = currentFile.getParentFile();
		String targetName = currentFile.getName().substring(0, currentFile.getName().lastIndexOf(".")) + ".summ";
		return new File(parent + File.separator + targetName);
	}

	private static String getId(Clause c) {
		return "ID=\"" + c.getId() + "\"";
	}

	private static String getCont(Clause c) {
		return "CONTINUE=\"" + c.getContinue() + "\"";
	}

	private static String fill(String string, int spaceCount) {
		StringBuffer sb = new StringBuffer(string);
		int toFill = Math.max(0, spaceCount - string.length());
		for (int i = 0; i < toFill; i++)
			sb.append(" ");
		return sb.toString();
	}

}
