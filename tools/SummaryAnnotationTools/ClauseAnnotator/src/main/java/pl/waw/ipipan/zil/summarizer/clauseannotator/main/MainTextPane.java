package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.awt.Insets;
import java.util.List;

import javax.swing.JTextPane;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class MainTextPane extends JTextPane {

	private static final long serialVersionUID = 3709902726162446568L;
	private static final Logger logger = Logger.getLogger(MainTextPane.class);

	private MainSplitPanel mainSplitPanel;

	private MainDocument document;

	public MainTextPane(MainSplitPanel mainSplitPanel) {
		super();

		this.mainSplitPanel = mainSplitPanel;
		this.document = new MainDocument(this);

		this.setStyledDocument(document);
		this.setMargin(new Insets(5, 5, 5, 5));
		this.setEditable(false);

		this.addCaretListener(document);
		this.addKeyListener(document);
	}

	public void setSentences(List<Sentence> sentences) {
		selectionChanged(null, null, 0);
		document.setSentences(sentences);
		this.setCaretPosition(0);
		this.getCaret().setVisible(true);
	}

	public List<Sentence> getSentences() {
		return document.getSentences();
	}

	public void selectionChanged(List<Word> selectedSequence, Marker m, int dot) {
		this.select(dot, dot);
		this.getCaret().setVisible(true);
		mainSplitPanel.selectionChanged(selectedSequence, m);
	}

	private void reloadSentences() {
		document.setSentences(document.getSentences());
		final int caretPos = document.getFirstSelectedWordStartPos();
		this.select(caretPos, caretPos);
		this.getCaret().setVisible(true);
		this.mainSplitPanel.annotationChanged();
	}

	private void reloadCurrentClause() {
		document.refreshCurrentClauseStyle();
		this.mainSplitPanel.annotationChanged();
	}

	private void reloadCurrentMarker() {
		document.refreshCurrentMarkerStyle();
		this.mainSplitPanel.annotationChanged();
	}

	/* ACTIONS */

	public synchronized void splitClause() {
		Word selectedWord = document.getFirstSelectedWord();
		if (selectedWord == null)
			return;

		if (selectedWord.isFirstInClause())
			return;

		if (selectedWord.isInMiddleOfMarker())
			return;

		Clause c = selectedWord.getClause();
		logger.info("Splitting clause: " + c + " starting from word: " + selectedWord);
		Sentence s = c.getSentence();
		s.splitClause(c, selectedWord);

		reloadSentences();
	}

	public synchronized void mergeClauseWithPrevious() {
		Word selectedWord = document.getFirstSelectedWord();
		if (selectedWord == null)
			return;
		Clause c = selectedWord.getClause();

		if (c.isFirstInSentence())
			return;

		logger.info("Merging clause: " + c + " into previous one");
		Sentence s = c.getSentence();
		s.mergeClauseWithPrevious(c);

		reloadSentences();
	}

	public synchronized void mergeClauseWithNext() {
		Word selectedWord = document.getFirstSelectedWord();
		if (selectedWord == null)
			return;
		Clause c = selectedWord.getClause();

		if (c.isLastInSentence())
			return;

		logger.info("Merging clause: " + c + " into next one");
		Sentence s = c.getSentence();
		s.mergeClauseWithNext(c);

		reloadSentences();
	}

	public synchronized void setContinuingClause(Clause cont) {
		Word selectedWord = document.getFirstSelectedWord();
		if (selectedWord == null) {
			logger.error("Trying to continiuing clause when no clause chosen!");
			return;
		}

		Clause c = selectedWord.getClause();
		logger.info("Setting continiuing clause: " + cont + " for clause: " + c);
		c.setContinuedClause(cont);

		reloadCurrentClause();
	}

	public synchronized void createMarker() {
		List<Word> selectedWords = document.getSelectedWords();
		if (selectedWords == null)
			return;

		for (Word w : selectedWords)
			if (w.getMarker() != null)
				return;

		logger.info("Creating new marker from words: " + selectedWords);
		new Marker(selectedWords);

		reloadSentences();
	}

	public synchronized void deleteMarker() {
		Marker selectedMarker = document.getSelectedMarker();
		if (selectedMarker == null)
			return;

		logger.info("Deleting marker: " + selectedMarker);
		selectedMarker.deleteMe();

		reloadSentences();
	}

	public synchronized void setMarkerNUC(String nuc) {
		Marker m = document.getSelectedMarker();
		if (m == null) {
			logger.error("Trying to set marker nuc when no marker chosen!");
			return;
		}

		logger.info("Setting nuc " + nuc + " for marker: " + m);
		m.setNUC(nuc);

		reloadCurrentMarker();
	}

	public void splitSentence() {
		Word selectedWord = document.getFirstSelectedWord();
		if (selectedWord == null)
			return;

		if (selectedWord.isFirstInClause())
			return;

		if (selectedWord.isInMiddleOfMarker())
			return;

		Clause c = selectedWord.getClause();
		Sentence s = c.getSentence();
		logger.info("Splitting sentence: " + s + " starting from word: " + selectedWord);

		s.getDocument().splitSentence(s, selectedWord);

		reloadSentences();

	}

	public void setMarkerConnect(String connect) {
		Marker m = document.getSelectedMarker();
		if (m == null) {
			logger.error("Trying to set marker connect when no marker chosen!");
			return;
		}

		logger.info("Setting connect " + connect + " for marker: " + m);
		m.setConnect(connect);

		reloadCurrentMarker();
	}

}
