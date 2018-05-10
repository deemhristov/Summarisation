package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Marker;
import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class BottomPanel extends JPanel implements ItemListener {

	private static final long serialVersionUID = 285048609353220598L;
	private static final Logger logger = Logger.getLogger(BottomPanel.class);

	private final MainTextPane textPane;

	// clause stuff
	private final JComboBox<Object> cont = new JComboBox<Object>();
	private final JLabel currentClause = new JLabel("");

	// marker stuff
	private final JComboBox<String> markerNUC = new JComboBox<String>();
	private final JComboBox<String> markerConnect = new JComboBox<String>();
	private final JLabel currentMarker = new JLabel("");

	{
		markerNUC.addItem("");

		markerNUC.addItem("_NN");
		markerNUC.addItem("_NS");
		markerNUC.addItem("_SN");

		markerNUC.addItem("N_N");
		markerNUC.addItem("N_S");
		markerNUC.addItem("S_N");

		markerNUC.addItem("NN_");
		markerNUC.addItem("NS_");
		markerNUC.addItem("SN_");

		markerConnect.addItem("");

		markerConnect.addItem("relate");
		markerConnect.addItem("fulfil");
		markerConnect.addItem("expect");
	}

	public BottomPanel(MainTextPane mainTextPane) {
		super();
		this.textPane = mainTextPane;

		clearClauseSelection();
		clearMarkerSelection();

		cont.addItemListener(this);
		markerConnect.addItemListener(this);
		markerNUC.addItemListener(this);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);

		// labels
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;

		this.add(new JLabel("Current clause:"), c);
		this.add(new JLabel("Continues:"), c);
		this.add(new JLabel("Current marker:"), c);
		this.add(new JLabel("Current marker nuc:"), c);
		this.add(new JLabel("Current marker connect:"), c);

		// values:
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;

		this.add(currentClause, c);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		this.add(cont, c);
		this.add(currentMarker, c);
		this.add(markerNUC, c);
		this.add(markerConnect, c);
	}

	private synchronized void clearMarkerSelection() {
		markerNUC.setEnabled(false);
		markerConnect.setEnabled(false);

		currentMarker.setText("No marker selected");
		markerNUC.setSelectedIndex(0);
		markerConnect.setSelectedIndex(0);
	}

	private synchronized void clearClauseSelection() {
		cont.setEnabled(false);
		cont.removeAllItems();
		currentClause.setText("No clause selected");
	}

	public void selectionChanged(final List<Word> selectedSequence, final Marker m) {
		if (selectedSequence == null) {
			selectMarker(null);
			selectClause(null);
		} else {
			Clause c = selectedSequence.get(0).getClause();

			selectClause(c);
			selectMarker(m);
		}
	}

	private synchronized void selectClause(Clause clause) {

		if (clause == null) {
			clearClauseSelection();

		} else {
			currentClause.setText(clause.toString());

			cont.setEnabled(false);
			cont.removeAllItems();
			cont.addItem("");
			int sel = 0;
			int i = 1;

			for (Clause c : clause.getSentence().getClausesBefore(clause)) {
				if (c.equals(clause.getContinuedClause()))
					sel = i;

				cont.addItem(c);
				i++;
			}

			cont.setSelectedIndex(sel);
			cont.setEnabled(true);
		}
	}

	private synchronized void selectMarker(Marker m) {

		if (m == null) {
			clearMarkerSelection();

		} else {
			currentMarker.setText(m.toString());

			markerNUC.setEnabled(false);
			markerNUC.setSelectedItem(m.getNUC());
			markerNUC.setEnabled(true);

			markerConnect.setEnabled(false);
			markerConnect.setSelectedItem(m.getConnect());
			markerConnect.setEnabled(true);
		}
	}

	public synchronized void itemStateChanged(ItemEvent e) {

		// we don't care about deselection events
		if (e.getStateChange() == ItemEvent.DESELECTED)
			return;

		Component c = (Component) e.getSource();

		if (c.isEnabled()) {

			if (c.equals(cont)) {
				if (cont.getSelectedIndex() > 0) {
					Clause clause = (Clause) cont.getSelectedItem();
					logger.debug("Continuing clause selection changed for: " + clause);

					textPane.setContinuingClause(clause);
				} else {
					textPane.setContinuingClause(null);
				}

			} else if (c.equals(markerNUC)) {

				if (markerNUC.getSelectedIndex() >= 0) {
					String nuc = (String) markerNUC.getSelectedItem();
					logger.debug("Marker nuc selection changed for: " + nuc);

					textPane.setMarkerNUC(nuc);
				}
			} else if (c.equals(markerConnect)) {

				if (markerConnect.getSelectedIndex() >= 0) {
					String connect = (String) markerConnect.getSelectedItem();
					logger.debug("Marker connect selection changed for: " + connect);

					textPane.setMarkerConnect(connect);
				}
			}
		}
	}
}