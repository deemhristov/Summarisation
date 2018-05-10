package pl.waw.ipipan.zil.summarizer.clausesumannotator.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;

public class ClauseTable extends JTable implements MouseListener, KeyListener {

	private static final long serialVersionUID = 5865240498654370980L;

	private final List<Clause> allClauses = new ArrayList<Clause>();
	private final Set<Clause> chosenClauses = new HashSet<Clause>();

	private final AbstractTableModel model;

	private final MyTab parentTab;

	public ClauseTable(MyTab parentTab, float displayFontSize) {
		this.parentTab = parentTab;

		setFillsViewportHeight(true);
		setTableHeader(null);
		addMouseListener(this);
		addKeyListener(this);
		setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setDefaultRenderer(Object.class, new MyCellRenderer(displayFontSize));

		model = new SimpleClauseTableModel();
		setModel(model);
	}

	public synchronized void setClauseList(List<Clause> clauses) {
		allClauses.clear();
		allClauses.addAll(clauses);
		chosenClauses.retainAll(allClauses);
		model.fireTableDataChanged();
	}

	public synchronized void rowsSelected() {
		if (this.getSelectedRows().length == 0)
			return;

		for (int i : this.getSelectedRows()) {
			changeRowStatus(i);
		}
		clearSelection();
		parentTab.chosenClausesChanged();
	}

	public synchronized void changeRowStatus(int row) {
		Clause c = allClauses.get(row);
		if (chosenClauses.contains(c))
			chosenClauses.remove(c);
		else
			chosenClauses.add(c);
	}

	private boolean isClauseChosen(Clause value) {
		return chosenClauses.contains(value);
	}

	public List<Clause> getChosenClauses() {
		List<Clause> result = new ArrayList<Clause>();
		for (Clause c : allClauses)
			if (chosenClauses.contains(c))
				result.add(c);
		return result;
	}

	public synchronized void setChosenClauses(Set<String> clauseIds) {
		chosenClauses.clear();
		for (Clause c : allClauses)
			if (clauseIds.contains(c.getId()))
				chosenClauses.add(c);
		parentTab.chosenClausesChanged();
	}

	/************* listeners ******************************/

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		rowsSelected();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			rowsSelected();
	}

	private final class SimpleClauseTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 5563319901787241258L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return allClauses.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return allClauses.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}
	}

	/********************** cell renderer **************************/

	private class MyCellRenderer extends JTextArea implements TableCellRenderer {

		private static final long serialVersionUID = 3003554184145715021L;

		public MyCellRenderer(float displayFontSize) {
			super();
			setFont(getFont().deriveFont(displayFontSize));
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int col) {

			String text = value.toString();
			setLineWrap(true);
			setWrapStyleWord(true);
			setText(text);
			setSize(table.getWidth(), table.getRowHeight());

			if (isSelected)
				setBackground(Color.gray);
			else if (isClauseChosen((Clause) value))
				setBackground(Color.yellow);
			else
				setBackground(null);

			int heightWanted = getPreferredSize().height;
			if (heightWanted != table.getRowHeight(row))
				table.setRowHeight(row, heightWanted);

			return this;
		}
	}

}
