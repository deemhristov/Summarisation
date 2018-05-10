package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.io.File;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Document;
import pl.waw.ipipan.zil.summarizer.clauseannotator.io.XMLReader;
import pl.waw.ipipan.zil.summarizer.clauseannotator.io.XMLWriter;

public class FixIds {

	public static void main(String[] args) {

		File dir = new File(args[0]);
		for (File f : dir.listFiles()) {
			Document doc = new Document();
			XMLReader.loadText(f, doc);
			XMLWriter.saveFile(f, doc.getSentences(), false, true);
		}
	}
}
