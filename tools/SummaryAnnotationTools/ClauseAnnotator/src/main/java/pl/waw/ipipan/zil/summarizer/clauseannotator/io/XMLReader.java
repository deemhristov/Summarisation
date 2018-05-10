package pl.waw.ipipan.zil.summarizer.clauseannotator.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Document;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class XMLReader {

	private static final Logger logger = Logger.getLogger(XMLReader.class);

	private static final XMLInputFactory xif = XMLInputFactory2.newInstance();

	public static String loadText(File input, Document document) {
		logger.info("Loading text from file: " + input);

		String error = null;

		InputStream is = null;
		XMLStreamReader sr = null;
		try {
			is = new FileInputStream(input);
			sr = xif.createXMLStreamReader(is);

			Sentence currentSentence = null;
			Clause currentClause = null;
			Word currentWord = null;
			Marker currentMarker = null;

			Map<String, Clause> id2Clause = new HashMap<String, Clause>();

			Map<Clause, String> clause2ContId = new HashMap<Clause, String>();

			while (sr.hasNext()) {
				sr.next();

				if (sr.isStartElement()) {
					String name = sr.getName().getLocalPart();

					if (name.equalsIgnoreCase("S")) {

						currentSentence = new Sentence();

						// artificial clause for first-time read
						currentClause = new Clause();

					} else if (name.equalsIgnoreCase("CLAUSE")) {

						String id = sr.getAttributeValue(null, "ID");
						String cont = sr.getAttributeValue(null, "CONTINUE");

						if (id == null)
							throw new Exception("Clause without id in line: " + sr.getLocation().getLineNumber());

						currentClause = new Clause();
						id2Clause.put(id, currentClause);

						if (cont != null && !cont.equals(""))
							clause2ContId.put(currentClause, cont);

					} else if (name.equalsIgnoreCase("W")) {

						String pos = sr.getAttributeValue(null, "POS");
						String lemma = sr.getAttributeValue(null, "LEMMA");
						if (pos == null)
							pos = sr.getAttributeValue(null, "pos");
						if (lemma == null)
							lemma = sr.getAttributeValue(null, "lemma");

						if (currentWord != null)
							throw new Exception("Word starting before previous word finished in line: "
									+ sr.getLocation().getLineNumber());

						currentWord = new Word(pos, lemma);

					} else if (name.equalsIgnoreCase("MARKER")) {

						String nuc = sr.getAttributeValue(null, "NUC");
						String connect = sr.getAttributeValue(null, "CONNECT");

						currentMarker = new Marker(nuc, connect);
					}

				} else if (sr.isEndElement()) {

					String name = sr.getName().getLocalPart();

					if (name.equalsIgnoreCase("S")) {

						if (currentSentence == null)
							throw new Exception("Sentence ending before starting in line: "
									+ sr.getLocation().getLineNumber());

						// for first-time read
						if (currentClause != null)
							currentSentence.add(currentClause);

						document.add(currentSentence);
						currentSentence = null;

					} else if (name.equalsIgnoreCase("CLAUSE")) {

						currentSentence.add(currentClause);
						currentClause = null;

					} else if (name.equalsIgnoreCase("W")) {

						if (currentWord == null)
							throw new Exception("Word ending before starting in line: "
									+ sr.getLocation().getLineNumber());

						if (currentClause == null)
							throw new Exception("Word not in a clause in line: " + sr.getLocation().getLineNumber());

						if (currentMarker != null) {
							currentMarker.add(currentWord);
						}

						if (currentWord.getPos() == null)
							throw new Exception("Word without pos attribute in line: "
									+ sr.getLocation().getLineNumber());

						currentClause.add(currentWord);
						currentWord = null;

					} else if (name.equalsIgnoreCase("MARKER")) {

						if (currentClause == null)
							throw new Exception("Marker not in a clause in line: " + sr.getLocation().getLineNumber());

						if (currentMarker == null)
							throw new Exception("Marker ended before started in line: "
									+ sr.getLocation().getLineNumber());

						currentMarker = null;
					}

				} else if (sr.isWhiteSpace()) {

				} else if (sr.isCharacters()) {
					if (currentWord != null) {
						if (currentWord.getOrth() != null)
							throw new Exception("Second word orth in line: " + sr.getLocation().getLineNumber());

						currentWord.setOrth(sr.getText());
					}
				}
			}

			for (Entry<Clause, String> e : clause2ContId.entrySet()) {
				Clause c = e.getKey();
				String contId = e.getValue();

				Clause cont = id2Clause.get(contId);
				if (cont == null)
					throw new Exception("Clause with id: " + contId + " not found in line: "
							+ sr.getLocation().getLineNumber());

				c.setContinuedClause(cont);
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

		logger.info(document.getSentences().size() + " sentences found.");

		return error;
	}
}
