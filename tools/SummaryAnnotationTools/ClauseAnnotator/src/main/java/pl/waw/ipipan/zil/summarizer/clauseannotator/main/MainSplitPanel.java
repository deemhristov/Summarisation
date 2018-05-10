package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Sentence;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class MainSplitPanel extends JSplitPane {

	private static final long serialVersionUID = 3656861958564714263L;
	private static final Logger logger = Logger.getLogger(MainSplitPanel.class);

	private ClauseAnnotator clauseannotator;

	private MainTextPane mainTextPane;
	private BottomPanel bottomPanel;

	public MainSplitPanel(ClauseAnnotator clauseannotator) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.clauseannotator = clauseannotator;

		logger.info("Creating main panel...");

		mainTextPane = new MainTextPane(this);
		bottomPanel = new BottomPanel(mainTextPane);

		this.setTopComponent(new JScrollPane(mainTextPane));
		this.setBottomComponent(bottomPanel);
		this.setOneTouchExpandable(true);
		this.setDividerLocation(350);
	}

	public void setSentences(final List<Sentence> sentences) {
		mainTextPane.setSentences(sentences);
	}

	public List<Sentence> getSentences() {
		return mainTextPane.getSentences();
	}

	public void selectionChanged(List<Word> selectedSequence, Marker m) {
		bottomPanel.selectionChanged(selectedSequence, m);
	}

	public void annotationChanged() {
		this.clauseannotator.annotationChanged();
	}

}
