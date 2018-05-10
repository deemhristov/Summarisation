package pl.waw.ipipan.zil.summarizer.clausesumannotator.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Word;

public class XMLReader {

	private static final Logger logger = Logger.getLogger(XMLReader.class);

	private static final XMLInputFactory xif = XMLInputFactory2.newInstance();

	public static String loadText(File input, List<Clause> allClauses) {
		logger.info("Loading text from file: " + input);

		String error = null;

		InputStream is = null;
		XMLStreamReader sr = null;
		try {
			is = new FileInputStream(input);
			sr = xif.createXMLStreamReader(is);

			Clause currentClause = null;
			Word currentWord = null;

			while (sr.hasNext()) {
				sr.next();

				if (sr.isStartElement()) {
					String name = sr.getName().getLocalPart();
					if (name.equals("CLAUSE")) {
						String id = sr.getAttributeValue(null, "ID");
						String cont = sr.getAttributeValue(null, "CONTINUE");

						if (cont == null)
							cont = "";

						if (id == null) {
							error = "Clause without id in line: " + sr.getLocation().getLineNumber();
							break;
						}

						if (currentClause != null) {
							error = "Clause starting before previous clause finished in line: "
									+ sr.getLocation().getLineNumber();
							break;
						}

						currentClause = new Clause(id, cont);
					} else if (name.equals("W")) {
						if (currentWord != null) {
							error = "Word starting before previous word finished in line: "
									+ sr.getLocation().getLineNumber();
							break;
						}
						currentWord = new Word();
					}
				} else if (sr.isEndElement()) {
					String name = sr.getName().getLocalPart();
					if (name.equals("CLAUSE")) {
						if (currentClause == null) {
							error = "Clause ending before starting in line: " + sr.getLocation().getLineNumber();
							break;
						}

						allClauses.add(currentClause);
						currentClause = null;
					} else if (name.equals("W")) {
						if (currentWord == null) {
							error = "Word ending before starting in line: " + sr.getLocation().getLineNumber();
							break;
						}
						if (currentClause == null) {
							error = "Word not inside a clause in line: " + sr.getLocation().getLineNumber();
							break;
						}
						currentClause.add(currentWord);
						currentWord = null;
					}
				} else if (sr.isWhiteSpace()) {

				} else if (sr.isCharacters()) {
					if (currentWord != null) {
						if (currentWord.getOrth() != null) {
							error = "Second word orth in line: " + sr.getLocation().getLineNumber();
							break;
						}
						currentWord.setOrth(sr.getText());
					}
				}
			}
		} catch (Exception e) {
			error = "Error reading file: " + e.getLocalizedMessage();
		} finally {
			try {
				if (is != null)
					is.close();
				if (sr != null)
					sr.close();
			} catch (Exception e) {
				error = "Error closing file: " + input + " Details: " + e.getLocalizedMessage();
			}
		}

		logger.info(allClauses.size() + " clauses found.");

		return error;
	}
}
