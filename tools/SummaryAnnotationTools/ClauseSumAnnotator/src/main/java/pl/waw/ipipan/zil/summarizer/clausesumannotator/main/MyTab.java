package pl.waw.ipipan.zil.summarizer.clausesumannotator.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;

public class MyTab extends JSplitPane implements MyTabChangeListener {

	private static final long serialVersionUID = 3656861958564714263L;

	private final ClauseTable table;
	private final JTextArea textArea;
	private final JProgressBar pb;

	private final int targetSegmentCount;

	private final Set<MyTabChangeListener> listeners = new HashSet<MyTabChangeListener>();

	private final double desired;

	public MyTab(int allSegments, double desired, float displayFontSize) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.targetSegmentCount = (int) (desired * allSegments);
		this.desired = desired;

		textArea = new JTextArea("empty");
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(textArea.getFont().deriveFont(displayFontSize));
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);

		pb = new JProgressBar(0, targetSegmentCount);
		JLabel all = new JLabel("Words in text: " + allSegments);
		JLabel prop = new JLabel("Summary proportion: " + (int) (desired * 100) + "%");
		JLabel summ = new JLabel("Words in summary: " + targetSegmentCount);

		// pb.setFont(pb.getFont().deriveFont(displayFontSize));
		// all.setFont(all.getFont().deriveFont(displayFontSize));
		// prop.setFont(prop.getFont().deriveFont(displayFontSize));
		// summ.setFont(summ.getFont().deriveFont(displayFontSize));

		JPanel bottomLeftPanel = new JPanel();
		bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel, BoxLayout.Y_AXIS));
		bottomLeftPanel.add(pb);
		bottomLeftPanel.add(all);
		bottomLeftPanel.add(prop);
		bottomLeftPanel.add(summ);
		bottomLeftPanel.setPreferredSize(new Dimension(prop.getPreferredSize().width + 10, 100));

		JPanel bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(textAreaScrollPane, BorderLayout.CENTER);
		bottomPane.add(bottomLeftPanel, BorderLayout.LINE_START);

		table = new ClauseTable(this, displayFontSize);

		JScrollPane topPane = new JScrollPane(table);

		this.setTopComponent(topPane);
		this.setBottomComponent(bottomPane);
		this.setOneTouchExpandable(true);
		this.setDividerLocation(350);
	}

	private void notifyAllListeners() {
		for (MyTabChangeListener tab : listeners)
			tab.clausesChanged(table.getChosenClauses());
	}

	public void addChangeListener(MyTabChangeListener tab) {
		listeners.add(tab);
	}

	@Override
	public void clausesChanged(List<Clause> clauses) {
		table.setClauseList(clauses);
		chosenClausesChanged();
	}

	public void chosenClausesChanged() {
		StringBuffer sb = new StringBuffer();
		int chosenWords = 0;
		for (Clause c : table.getChosenClauses()) {
			sb.append(c);
			chosenWords += c.getWords().size();
		}

		pb.setValue(chosenWords);
		pb.setStringPainted(true);
		if (targetSegmentCount == 0)
			pb.setString(chosenWords + "/0 = -- %");
		else
			pb.setString(chosenWords + "/" + targetSegmentCount + " = " + (100 * chosenWords / targetSegmentCount)
					+ "%");
		textArea.setText(sb.toString());

		notifyAllListeners();
	}

	@Override
	public String getName() {
		return (int) (desired * 100) + "% summary";
	}

	public List<Clause> getChosenClauses() {
		return table.getChosenClauses();
	}

	public void setChosenClauses(Set<String> clauseIds) {
		table.setChosenClauses(clauseIds);
	}

}
