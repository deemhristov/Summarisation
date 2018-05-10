package pl.waw.ipipan.zil.summarizer.clauseannotator.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class XMLWriter {

	private static final Logger logger = Logger.getLogger(XMLWriter.class);
	private static final XMLOutputFactory xof = XMLOutputFactory2.newInstance();

	private static final String INDENT = "  ";

	public static boolean saveFile(File targetFile, List<Sentence> sentences) {
		return saveFile(targetFile, sentences, false, false);
	}

	public static boolean saveFile(File targetFile, List<Sentence> sentences, boolean omitClauses, boolean omitMarkers) {
		logger.info("Saving sentences in file: " + targetFile);
		BufferedWriter bw = null;
		XMLStreamWriter xsw = null;
		try {

			bw = new BufferedWriter(new FileWriter(targetFile));
			xsw = xof.createXMLStreamWriter(bw);

			IdGenerator gen = new IdGenerator();
			writeDocument(xsw, sentences, gen, omitClauses, omitMarkers);

		} catch (Exception e) {
			logger.error("Error saving file: " + e.getLocalizedMessage());
			return false;

		} finally {
			try {
				if (xsw != null)
					xsw.close();
				if (bw != null)
					bw.close();

			} catch (Exception e) {
				logger.error("Error closing file: " + e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}

	private static void writeSentence(XMLStreamWriter xsw, Sentence s, int i, IdGenerator gen, boolean omitClauses,
			boolean omitMarkers) throws XMLStreamException {

		indent(xsw, i);
		xsw.writeStartElement("S");
		xsw.writeAttribute("ID", gen.getId(s));
		xsw.writeCharacters("\n");

		for (Clause c : s.getClauses())
			writeClause(xsw, c, i + 1, gen, omitClauses, omitMarkers);

		indent(xsw, i);
		xsw.writeEndElement(); // S
		xsw.writeCharacters("\n");
	}

	private static void writeClause(XMLStreamWriter xsw, Clause c, int i, IdGenerator gen, boolean omitClauses,
			boolean omitMarkers) throws XMLStreamException {

		if (!omitClauses) {
			indent(xsw, i);
			xsw.writeStartElement("CLAUSE");

			xsw.writeAttribute("ID", gen.getId(c));

			Clause cont = c.getContinuedClause();
			if (cont != null)
				xsw.writeAttribute("CONTINUE", gen.getId(c.getContinuedClause()));

			xsw.writeCharacters("\n");
		} else {
			i--;
		}

		Marker prevMarker = null;
		for (Word w : c.getWords()) {
			Marker currentMarker = w.getMarker();

			if (omitMarkers || currentMarker == null) {

				if (!omitMarkers && prevMarker != null)
					endMarker(xsw, i + 1);

				writeWord(xsw, w, i + 1, gen);

			} else {

				if (prevMarker == null) {
					startMarker(xsw, currentMarker, i + 1, gen);

				} else if (!prevMarker.equals(currentMarker)) {
					endMarker(xsw, i + 1);
					startMarker(xsw, currentMarker, i + 1, gen);
				}

				writeWord(xsw, w, i + 2, gen);
			}

			prevMarker = currentMarker;
		}

		if (!omitMarkers && prevMarker != null)
			endMarker(xsw, i + 1);

		if (!omitClauses) {
			indent(xsw, i);
			xsw.writeEndElement(); // CLAUSE
			xsw.writeCharacters("\n");
		}
	}

	private static void endMarker(XMLStreamWriter xsw, int i) throws XMLStreamException {
		indent(xsw, i);
		xsw.writeEndElement(); // MARKER
		xsw.writeCharacters("\n");
	}

	private static void startMarker(XMLStreamWriter xsw, Marker currentMarker, int i, IdGenerator gen)
			throws XMLStreamException {
		indent(xsw, i);
		xsw.writeStartElement("MARKER");
		xsw.writeAttribute("ID", gen.getId(currentMarker));
		xsw.writeAttribute("NUC", currentMarker.getNUC());
		xsw.writeAttribute("TYPE", currentMarker.getType());
		xsw.writeAttribute("CONNECT", currentMarker.getConnect());

		xsw.writeCharacters("\n");
	}

	private static void writeWord(XMLStreamWriter xsw, Word w, int i, IdGenerator gen) throws XMLStreamException {
		indent(xsw, i);
		xsw.writeStartElement("W");
		xsw.writeAttribute("ID", gen.getId(w));
		xsw.writeAttribute("LEMMA", w.getLemma());
		xsw.writeAttribute("POS", w.getPos());

		xsw.writeCharacters(w.getOrth());

		xsw.writeEndElement(); // W
		xsw.writeCharacters("\n");
	}

	private static void indent(XMLStreamWriter xsw, int i) throws XMLStreamException {
		for (int j = 0; j < i; j++)
			xsw.writeCharacters(INDENT);
	}

	private static void writeDocument(XMLStreamWriter xsw, List<Sentence> sentences, IdGenerator gen,
			boolean omitClauses, boolean omitMarkers) throws XMLStreamException {
		xsw.writeStartDocument("utf-8", "1.0");
		xsw.writeCharacters("\n");
		xsw.writeStartElement("DOCUMENT");
		xsw.writeCharacters("\n");

		for (Sentence s : sentences)
			writeSentence(xsw, s, 1, gen, omitClauses, omitMarkers);

		xsw.writeEndElement(); // DOCUMENT
		xsw.writeEndDocument();
	}

}
