package pl.waw.ipipan.zil.summarizer.abssumannotator.main;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.abssumannotator.basic.Text;

public class MyTab extends JPanel {

	private static final long serialVersionUID = 3656861958564714263L;

	private static final Logger logger = Logger.getLogger(MyTab.class);

	private JTextArea textArea;
	private JProgressBar pb;

	private int targetSegmentCount;
	private int allSegmentCount;

	private AbsSumAnnotator sumanno;
	private int summNumber;

	private UndoManager undoManager;
	private JButton undoButton;
	private JButton redoButton;

	public MyTab(AbsSumAnnotator absSumAnnotator, String summary, int summNumber, int allSegmentCount,
			int targetSegmentCount) {

		this.sumanno = absSumAnnotator;
		this.summNumber = summNumber;

		this.targetSegmentCount = targetSegmentCount;
		this.allSegmentCount = allSegmentCount;

		textArea = new JTextArea(summary);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setMargin(new Insets(10, 10, 10, 10));

		undoManager = new UndoManager();
		UndoAction undoAction = new UndoAction();
		RedoAction redoAction = new RedoAction();
		undoButton = new JButton(undoAction);
		redoButton = new JButton(redoAction);

		textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
				summaryChanged(false);
				updateButtons();
			}
		});

		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK), "undo");
		this.getActionMap().put("undo", undoAction);
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK), "redo");
		this.getActionMap().put("redo", redoAction);

		JScrollPane textAreaScrollPane = new JScrollPane(textArea);

		pb = new JProgressBar(0, targetSegmentCount);
		JLabel all = new JLabel("Words in text: " + allSegmentCount);
		JLabel summ = new JLabel("Words in summary: " + targetSegmentCount);

		JPanel bottomLeftPanel = new JPanel();
		bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel, BoxLayout.Y_AXIS));
		bottomLeftPanel.add(pb);
		bottomLeftPanel.add(all);
		bottomLeftPanel.add(summ);
		bottomLeftPanel.add(Box.createVerticalStrut(8));
		bottomLeftPanel.add(undoButton);
		bottomLeftPanel.add(Box.createVerticalStrut(3));
		bottomLeftPanel.add(redoButton);

		this.setLayout(new BorderLayout());
		this.add(textAreaScrollPane, BorderLayout.CENTER);
		this.add(bottomLeftPanel, BorderLayout.LINE_START);

		summaryChanged(true);
		updateButtons();
	}

	protected void updateButtons() {
		undoButton.setText(undoManager.getUndoPresentationName() + " (CTRL+Z)");
		redoButton.setText(undoManager.getRedoPresentationName() + " (CTRL+Y)");
		undoButton.setEnabled(undoManager.canUndo());
		redoButton.setEnabled(undoManager.canRedo());
	}

	private void summaryChanged(boolean quiet) {
		int chosenWords = Text.getStringWordCount(textArea.getText());

		pb.setValue(chosenWords);
		pb.setStringPainted(true);
		if (targetSegmentCount == 0)
			pb.setString(chosenWords + "/0 = -- %");
		else
			pb.setString(chosenWords + "/" + targetSegmentCount + " = " + (100 * chosenWords / targetSegmentCount)
					+ "%");

		if (!quiet)
			sumanno.summaryChanged(summNumber, textArea.getText());
	}

	public String getName() {
		return (int) (targetSegmentCount * 100.0 / allSegmentCount) + "% summary";
	}

	private class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 640721327092571644L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (undoManager.canUndo()) {
				try {
					undoManager.undo();
					summaryChanged(false);
				} catch (CannotRedoException cre) {
					logger.error("Error when undoing:" + cre.getLocalizedMessage());
				}
				updateButtons();
			}
		}

	}

	private class RedoAction extends AbstractAction {

		private static final long serialVersionUID = 640721327092571644L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (undoManager.canRedo()) {
				try {
					undoManager.redo();
					summaryChanged(false);
				} catch (CannotRedoException cre) {
					logger.error("Error when redoing:" + cre.getLocalizedMessage());
				}
				updateButtons();
			}
		}

	}
}
