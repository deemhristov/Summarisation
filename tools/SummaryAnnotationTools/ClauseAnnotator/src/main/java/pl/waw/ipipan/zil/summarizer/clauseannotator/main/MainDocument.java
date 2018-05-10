package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class MainDocument extends DefaultStyledDocument implements CaretListener, KeyListener {

	private static final long serialVersionUID = 4276485351817531583L;
	private static final Logger logger = Logger.getLogger(MainDocument.class);

	private final MainTextPane textPane;

	private List<Word> selectedWords = null;
	private Marker selectedMarker = null;

	private List<Word> posToWord = null;
	private Map<Word, Integer> wordToStartPos = null;
	private List<Sentence> sentences = null;

	private boolean ignoreCaretUpdate = false;

	public MainDocument(MainTextPane mainTextPane) {
		this.textPane = mainTextPane;
	}

	public synchronized void setSentences(final List<Sentence> sents) {

		ignoreCaretUpdate = true;

		try {
			remove(0, getLength());
		} catch (BadLocationException e) {
			logger.error("Error clearing document: " + e.getLocalizedMessage());
		}

		sentences = sents;

		posToWord = new ArrayList<Word>();
		wordToStartPos = new HashMap<Word, Integer>();

		int pos = 0;
		try {
			for (Sentence s : sentences) {

				for (Clause c : s.getClauses()) {

					insertString(pos, "[ ", Styles.getHandleStyle());
					pos += 2;
					posToWord.add(null);
					posToWord.add(null);

					Word prev = null;
					for (Word w : c.getWords()) {
						if (prev != null) {
							insertString(pos, " ", Styles.getWhitespaceStyle());
							pos += 1;
							posToWord.add(prev);
						}

						String orth = w.getOrth();
						boolean selected = selectedWords != null && selectedWords.contains(w);
						insertString(pos, orth, Styles.getWordStyle(w, selected));
						int length = orth.length();
						wordToStartPos.put(w, pos);
						for (int i = 0; i < length; i++)
							posToWord.add(w);
						pos += length;

						prev = w;
					}

					insertString(pos, " ]", Styles.getHandleStyle());
					pos += 2;
					posToWord.add(null);
					posToWord.add(null);
				}
				insertString(pos, "\n\n", Styles.getWhitespaceStyle());
				pos += 2;
				posToWord.add(null);
				posToWord.add(null);
			}
			// for last dot
			posToWord.add(null);

		} catch (BadLocationException e) {
			logger.error("Error setting document: " + e.getLocalizedMessage());
		}

		ignoreCaretUpdate = false;
	}

	public synchronized List<Sentence> getSentences() {
		return sentences;
	}

	public void caretUpdate(final CaretEvent e) {
		if (ignoreCaretUpdate)
			return;

		int dot = e.getDot();
		int mark = e.getMark();
		selectionOcurred(mark, dot);
	}

	private synchronized void selectionOcurred(int mark, int dot) {

		if (posToWord == null)
			return;

		Word startWord;
		Word stopWord;
		if (mark > dot) {
			startWord = posToWord.get(dot);
			stopWord = posToWord.get(mark);
		} else {
			startWord = posToWord.get(mark);
			stopWord = posToWord.get(dot);
		}

		if (startWord == null || stopWord == null) {
			this.setSelectedWords(null, dot);
			return;
		}

		Clause clause1 = startWord.getClause();
		Clause clause2 = stopWord.getClause();

		if (!clause1.equals(clause2)) {
			this.setSelectedWords(null, dot);
			return;
		}

		List<Word> selectedSequence = clause1.getWordsSequence(startWord, stopWord);

		this.setSelectedWords(selectedSequence, dot);
	}

	public synchronized void setSelectedWords(List<Word> selectedSequence, int mark) {

		if (wordToStartPos == null)
			return;

		logger.debug("Setting selected words: " + selectedSequence);

		// remove previous selection
		if (this.selectedWords != null) {
			for (Word w : selectedWords) {
				SimpleAttributeSet att = Styles.getWordStyle(w, false);
				int start = wordToStartPos.get(w);
				int len = w.toString().length();
				this.setCharacterAttributes(start, len, att, true);
			}
		}

		// mark new selection
		if (selectedSequence != null) {
			for (Word w : selectedSequence) {
				SimpleAttributeSet att = Styles.getWordStyle(w, true);
				int start = wordToStartPos.get(w);
				int len = w.toString().length();
				this.setCharacterAttributes(start, len, att, true);
			}
		}

		this.selectedWords = selectedSequence;
		this.selectedMarker = getMarkerFromSelection(selectedSequence);

		if (this.ignoreCaretUpdate) {
			this.textPane.selectionChanged(selectedSequence, selectedMarker, mark);
		} else {
			this.ignoreCaretUpdate = true;
			this.textPane.selectionChanged(selectedSequence, selectedMarker, mark);
			this.ignoreCaretUpdate = false;
		}

	}

	private Marker getMarkerFromSelection(List<Word> selectedSequence) {
		if (selectedSequence == null)
			return null;

		Marker m = null;
		for (Word w : selectedSequence) {
			if (w.getMarker() != null) {
				if (m == null) {
					m = w.getMarker();
				} else if (!m.equals(w.getMarker())) {
					m = null;
					break;
				}
			}
		}
		return m;
	}

	public synchronized Word getFirstSelectedWord() {
		if (selectedWords == null)
			return null;
		return this.selectedWords.get(0);
	}

	public synchronized int getFirstSelectedWordStartPos() {
		Word firstSelectedWord = getFirstSelectedWord();
		if (firstSelectedWord == null || wordToStartPos == null)
			return 0;
		return wordToStartPos.get(firstSelectedWord);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			ignoreCaretUpdate = true;
		}
	}

	public void keyReleased(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			textPane.splitClause();
		} else if (e.getKeyCode() == KeyEvent.VK_P) {
			textPane.mergeClauseWithPrevious();
		} else if (e.getKeyCode() == KeyEvent.VK_N) {
			textPane.mergeClauseWithNext();
		} else if (e.getKeyCode() == KeyEvent.VK_M) {
			textPane.createMarker();
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			textPane.deleteMarker();
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			ignoreCaretUpdate = false;
			Caret c = textPane.getCaret();
			selectionOcurred(c.getDot(), c.getMark());
		}
		// } else if (e.getKeyCode() == KeyEvent.VK_S) {
		// textPane.splitSentence();
		// }

	}

	public synchronized List<Word> getSelectedWords() {
		return this.selectedWords;
	}

	public synchronized Marker getSelectedMarker() {
		return this.selectedMarker;
	}

	public synchronized void refreshCurrentMarkerStyle() {
		if (selectedMarker == null || wordToStartPos == null)
			return;

		for (Word w : selectedMarker.getWords()) {
			boolean selected = false;
			if (selectedWords.contains(w))
				selected = true;

			SimpleAttributeSet att = Styles.getWordStyle(w, selected);
			int start = wordToStartPos.get(w);
			int len = w.toString().length();
			this.setCharacterAttributes(start, len, att, true);
		}
	}

	public synchronized void refreshCurrentClauseStyle() {
		if (selectedWords == null || wordToStartPos == null)
			return;

		Clause c = selectedWords.get(0).getClause();

		for (Word w : c.getWords()) {
			boolean selected = false;
			if (selectedWords.contains(w))
				selected = true;

			SimpleAttributeSet att = Styles.getWordStyle(w, selected);
			int start = wordToStartPos.get(w);
			int len = w.toString().length();
			this.setCharacterAttributes(start, len, att, true);
		}
	}

}
