package pl.waw.ipipan.zil.summarizer.extrsumannotator.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.extrsumannotator.basic.Text;

public class MyTab extends JSplitPane implements MyTabChangeListener {

	private static final Logger logger = Logger.getLogger(MyTab.class);

	private static final long serialVersionUID = 3656861958564714263L;

	private final JTextToSummarizeArea fullTextPane;
	private final JTextArea summaryPane;

	private final JProgressBar pb;

	private final int targetSegmentCount;
	private final int allSegmentCount;

	private final Set<MyTabChangeListener> listeners = new HashSet<MyTabChangeListener>();

	private final String originalText;

	public SortedSet<Integer> previousIndices = null;

	private final JButton undoButton;

	public MyTab(int targetCount, String originalText) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.originalText = originalText;

		this.targetSegmentCount = targetCount;
		this.allSegmentCount = Text.getStringWordCount(originalText);

		fullTextPane = new JTextToSummarizeArea();
		fullTextPane.setLineWrap(true);
		fullTextPane.setEditable(false);
		fullTextPane.setWrapStyleWord(true);
		fullTextPane.setMargin(new Insets(10, 10, 10, 10));

		summaryPane = new JTextArea();
		summaryPane.setLineWrap(true);
		summaryPane.setEditable(false);
		summaryPane.setWrapStyleWord(true);
		summaryPane.setMargin(new Insets(10, 10, 10, 10));

		pb = new JProgressBar(0, targetSegmentCount);
		JLabel all = new JLabel("Words in text: " + allSegmentCount);
		JLabel summ = new JLabel("Words in summary: " + targetSegmentCount);

		UndoAction undoAction = new UndoAction();
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK), "undo");
		this.getActionMap().put("undo", undoAction);
		undoButton = new JButton(undoAction);
		undoButton.setEnabled(false);
		undoButton.setText("Undo last action (CTRL+Z)");

		JPanel bottomLeftPanel = new JPanel();
		bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel, BoxLayout.Y_AXIS));
		bottomLeftPanel.add(pb);
		bottomLeftPanel.add(all);
		bottomLeftPanel.add(summ);
		bottomLeftPanel.add(Box.createVerticalStrut(8));
		bottomLeftPanel.add(undoButton);

		JPanel bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(new JScrollPane(summaryPane), BorderLayout.CENTER);
		bottomPane.add(bottomLeftPanel, BorderLayout.LINE_START);

		this.setTopComponent(new JScrollPane(fullTextPane));
		this.setBottomComponent(bottomPane);
		this.setOneTouchExpandable(true);
		this.setDividerLocation(350);
	}

	public void addChangeListener(MyTabChangeListener tab) {
		listeners.add(tab);
	}

	public void selectionChanged(SortedSet<Integer> selectedCharsIndices) {
		String summ = getTextByIndices(this.originalText, selectedCharsIndices);
		int summWC = Text.getStringWordCount(summ);

		pb.setValue(summWC);
		pb.setStringPainted(true);
		if (targetSegmentCount == 0)
			pb.setString(summWC + "/0 = -- %");
		else
			pb.setString(summWC + "/" + targetSegmentCount + " = " + (100 * summWC / targetSegmentCount) + "%");

		summaryPane.setText(summ);

		for (MyTabChangeListener l : listeners)
			l.fullTextChanged(selectedCharsIndices);
	}

	private String getTextByIndices(String text, SortedSet<Integer> selectedCharsIndices) {
		StringBuffer sb = new StringBuffer();
		for (Integer idx : selectedCharsIndices)
			sb.append(text.substring(idx, idx + 1));
		String summ = sb.toString();
		return summ;
	}

	public void fullTextChanged(SortedSet<Integer> selectedCharsIndices) {
		fullTextPane.setOriginalText(selectedCharsIndices);
	}

	public SortedSet<Integer> getSelectedIndices() {
		return fullTextPane.getSelectedIndices();
	}

	public void setSummary(SortedSet<Integer> sum) {
		fullTextPane.setSummary(sum);
	}

	public void saveStateForUndo(boolean thisTabChanged) {
		logger.debug("Saving state for undo... (tab: " + targetSegmentCount + ")");
		previousIndices = getSelectedIndices();
		logger.debug(previousIndices);

		if (thisTabChanged)
			undoButton.setEnabled(true);
		else
			undoButton.setEnabled(false);

		for (MyTabChangeListener l : listeners)
			l.saveStateForUndo(false);
	}

	public void undo() {
		if (previousIndices != null) {
			logger.debug("Undoing... (tab: " + targetSegmentCount + ")");
			logger.debug(previousIndices);
			this.undoButton.setEnabled(false);

			try {
				this.setSummary(previousIndices);
			} catch (Exception ex) {
				logger.error("Error undoing... (tab: " + targetSegmentCount + ")");
			}

			previousIndices = null;
			for (MyTabChangeListener l : listeners)
				l.undo();
		} else {
			logger.debug("Nothing to undo... (tab: " + targetSegmentCount + ")");
		}
	}

	private class JTextToSummarizeArea extends JTextArea implements CaretListener {

		private static final long serialVersionUID = -8674213151313542475L;

		private SortedSet<Integer> selectedCharsIndices = new TreeSet<Integer>();

		private final Map<Integer, Integer> mappingOrig2This = new HashMap<Integer, Integer>();
		private final Map<Integer, Integer> mappingThis2Orig = new HashMap<Integer, Integer>();

		private final Color hlColor = new Color(255, 0, 0, 100);
		private final HighlightPainter hlPainter = new DefaultHighlighter.DefaultHighlightPainter(hlColor);

		public JTextToSummarizeArea() {
			super();
			this.addCaretListener(this);
		}

		public void setSummary(SortedSet<Integer> sum) {
			selectedCharsIndices.clear();
			for (Integer idx : sum)
				selectedCharsIndices.add(mappingOrig2This.get(idx));
			refreshView();
		}

		public SortedSet<Integer> getSelectedIndices() {
			SortedSet<Integer> origSelectedCharsIndices = new TreeSet<Integer>();
			for (Integer i : this.selectedCharsIndices)
				origSelectedCharsIndices.add(mappingThis2Orig.get(i));

			return origSelectedCharsIndices;
		}

		public void setOriginalText(SortedSet<Integer> newOriginalTextIndices) {
			if (mappingOrig2This != null) {
				SortedSet<Integer> newSelectedCharsIndices = new TreeSet<Integer>();
				int i = 0;
				for (Integer idx : newOriginalTextIndices) {
					Integer oldIdx = mappingOrig2This.get(idx);
					if (oldIdx != null && selectedCharsIndices.contains(oldIdx))
						newSelectedCharsIndices.add(i);
					i++;
				}
				selectedCharsIndices = newSelectedCharsIndices;
			}

			mappingOrig2This.clear();
			int i = 0;
			for (Integer idx : newOriginalTextIndices)
				mappingOrig2This.put(idx, i++);

			mappingThis2Orig.clear();
			i = 0;
			for (Integer idx : newOriginalTextIndices)
				mappingThis2Orig.put(i++, idx);

			String summ = getTextByIndices(originalText, newOriginalTextIndices);

			this.setText(summ);
			this.setCaretPosition(0);
			refreshView();
		}

		public void caretUpdate(CaretEvent e) {
			int dot = e.getDot();
			int mark = e.getMark();

			if (dot == mark)
				return;

			if (dot < mark) {
				int tmp = mark;
				mark = dot;
				dot = tmp;
			}

			saveStateForUndo(true);

			boolean allSelected = true;
			for (int i = mark; i < dot; i++)
				if (!this.selectedCharsIndices.contains(i))
					allSelected = false;

			for (int i = mark; i < dot; i++)
				if (!allSelected)
					this.selectedCharsIndices.add(i);
				else
					this.selectedCharsIndices.remove(i);

			refreshView();
		}

		private void refreshView() {

			this.getHighlighter().removeAllHighlights();

			Integer start = null;
			Integer end = null;

			Iterator<Integer> it = selectedCharsIndices.iterator();
			while (it.hasNext()) {
				int idx = it.next();
				if (start == null) {
					start = idx;
					end = idx;
				} else {
					if (idx == end + 1)
						end = idx;
					else {
						addHighlight(start, end + 1);
						start = idx;
						end = idx;
					}
				}
			}
			if (start != null && end != null)
				addHighlight(start, end + 1);

			selectionChanged(getSelectedIndices());
		}

		private void addHighlight(Integer start, Integer end) {
			try {
				this.getHighlighter().addHighlight(start, end, this.hlPainter);
			} catch (BadLocationException e) {
				logger.error("Error highlighting... " + e.getLocalizedMessage());
			}
		}
	}

	private class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 640721327092571644L;

		public void actionPerformed(ActionEvent e) {
			if (undoButton.isEnabled()) {
				undo();
			}
		}
	}
}
