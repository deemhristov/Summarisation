package pl.waw.ipipan.zil.summarizer.extrsumannotator.main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.extrsumannotator.basic.Text;
import pl.waw.ipipan.zil.summarizer.extrsumannotator.io.SummaryFileIO;

public class ExtrSumAnnotator extends JFrame implements Runnable, MyTabChangeListener {

	private static final String WINDOW_TITLE = "ESE v1.1";

	private static final long serialVersionUID = -3830790411182131318L;

	private static final Logger logger = Logger.getLogger(ExtrSumAnnotator.class);

	private static final int HEIGTH = 600;
	private static final int WIDTH = 800;

	public static double[] SUMMARIES_SIZES = new double[] { 0.2, 0.1, 0.05 };

	private JMenuItem save;

	private File currentFile = null;
	private boolean unsavedChanges = false;

	private JTabbedPane tabbedPane;

	private float displayFontSize;

	private Text currentText;

	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			logger.error("Wrong usage! Should be: java -jar " + ExtrSumAnnotator.class.getSimpleName()
					+ " [input file]");
			return;
		}
		final ExtrSumAnnotator summanno = new ExtrSumAnnotator();
		try {
			SwingUtilities.invokeAndWait(summanno);
		} catch (Exception e) {
			logger.error("Error starting application: " + e.getLocalizedMessage());
			e.printStackTrace();
			return;
		}

		if (args.length == 1) {
			final File input = new File(args[0]);
			if (input.exists()) {
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						summanno.loadFile(input);
					}
				});
			} else {
				logger.error("Input file: " + input + " doesn't exist!");
			}
		}
	}

	public void run() {
		displayFontSize = loadDisplayFontSize();
		tabbedPane = new JTabbedPane();
		this.getContentPane().add(tabbedPane);
		this.setTitle(WINDOW_TITLE);
		this.createMenu();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new MainWindowListener(this));
		this.setSize(WIDTH, HEIGTH);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGTH) / 2);
		this.setVisible(true);
	}

	private float loadDisplayFontSize() {
		float result = 12f;
		logger.info("Using default font size.");
		return result;
	}

	private void createMenu() {
		JMenuBar menu = new JMenuBar();
		this.setJMenuBar(menu);

		JMenu file = new JMenu("File");
		menu.add(file);

		JMenuItem open = new JMenuItem("Open");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		open.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				requestOpenFile();
			}
		});
		file.add(open);

		save = new JMenuItem("Save");
		save.setEnabled(true);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				requestSaveFile();
			}
		});
		file.add(save);

		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				requestExit();
			}
		});
		file.add(exit);

		JMenu other = new JMenu("Other");

		ButtonGroup fontSizeButtonGroup = new ButtonGroup();
		JMenu setFontSize = new JMenu("Change font size");
		for (int z = 8; z < 30; z = z + 2) {

			final int currentFontSize = z;

			JRadioButtonMenuItem temp = new JRadioButtonMenuItem(z + "");
			if (z == displayFontSize) {
				temp.setSelected(true);
			}
			temp.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent ae) {
					requestSetFontSize(currentFontSize);
				}
			});
			fontSizeButtonGroup.add(temp);
			setFontSize.add(temp);
		}
		other.add(setFontSize);

		JMenuItem help = new JMenuItem("Help");
		help.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				requestShowHelp();
			}
		});
		other.add(help);

		menu.add(other);
	}

	protected void requestSetFontSize(float currentFontSize) {
		logger.info("Setting font size: " + currentFontSize);
		displayFontSize = currentFontSize;

		Enumeration<Object> enumer = UIManager.getDefaults().keys();
		while (enumer.hasMoreElements()) {
			Object key = enumer.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof Font) {
				UIManager.put(key, new javax.swing.plaf.FontUIResource(((Font) value).deriveFont(currentFontSize)));
			}
		}
		SwingUtilities.updateComponentTreeUI(this);
	}

	private final void requestShowHelp() {
		JFrame helpFrame = new JFrame("Help");
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		URL helpURL = ExtrSumAnnotator.class.getClassLoader().getResource("help.html");
		if (helpURL != null) {
			try {
				editorPane.setPage(helpURL);
			} catch (IOException e) {
				logger.error("Attempted to read a bad URL: " + helpURL);
			}
		} else {
			logger.error("Couldn't find file: help.html");
		}

		// Put the editor pane in a scroll pane.
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setPreferredSize(new Dimension(800, 600));
		editorScrollPane.setMinimumSize(new Dimension(30, 30));

		helpFrame.getContentPane().add(editorScrollPane);
		helpFrame.pack();
		helpFrame.setVisible(true);
	}

	private void requestSaveFile() {

		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			MyTab tab = (MyTab) tabbedPane.getComponentAt(i);
			currentText.setSummary(i, tab.getSelectedIndices());
		}

		
		JFileChooser chooser;
		if (currentFile == null)
			chooser = new JFileChooser();
		else
			chooser = new JFileChooser(currentFile.getParentFile());
		chooser.setSelectedFile(currentFile);

		chooser.setFileFilter(new SummannotatorFileFilter());
		int result = chooser.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			boolean success = SummaryFileIO.saveSummary(chooser.getSelectedFile(), currentText);

			if (success) {
				unsavedChanges = false;
				setTitle(WINDOW_TITLE);
				save.setEnabled(true);
				currentFile = chooser.getSelectedFile();
			} else {
				showError("Error saving file: " + currentFile);
			}
		}
	}

	private final void requestOpenFile() {
		if (!askToSaveChanges())
			return;

		JFileChooser chooser;
		if (currentFile == null)
			chooser = new JFileChooser();
		else
			chooser = new JFileChooser(currentFile.getParentFile());

		chooser.setFileFilter(new SummannotatorFileFilter());
		int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			JSpinner lSpinner = new JSpinner(new SpinnerNumberModel(SUMMARIES_SIZES[0] * 100, 1, 100, 1));
			JSpinner mSpinner = new JSpinner(new SpinnerNumberModel(SUMMARIES_SIZES[1] * 100, 1, 100, 1));
			JSpinner sSpinner = new JSpinner(new SpinnerNumberModel(SUMMARIES_SIZES[2] * 100, 1, 100, 1));

			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel("L:"));
			myPanel.add(lSpinner);
			myPanel.add(new JLabel("%"));
			myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			myPanel.add(new JLabel("M:"));
			myPanel.add(mSpinner);
			myPanel.add(new JLabel("%"));
			myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			myPanel.add(new JLabel("S:"));
			myPanel.add(sSpinner);
			myPanel.add(new JLabel("%"));

			int resresult = JOptionPane.showConfirmDialog(null, myPanel, 
					"Please enter résumé sizes", JOptionPane.OK_CANCEL_OPTION);
			if (resresult == JOptionPane.OK_OPTION) {
				System.out.println("L value: " + lSpinner.getValue());
				System.out.println("M value: " + mSpinner.getValue());
				System.out.println("S value: " + sSpinner.getValue());
				SUMMARIES_SIZES[0] = (double)lSpinner.getValue() / 100.0;
				SUMMARIES_SIZES[1] = (double)mSpinner.getValue() / 100.0;
				SUMMARIES_SIZES[2] = (double)sSpinner.getValue() / 100.0;
			}
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.loadFile(chooser.getSelectedFile());
			this.setCursor(null);
		}
	}

	private boolean askToSaveChanges() {
		if (!unsavedChanges)
			return true;

		int result = JOptionPane.showOptionDialog(this, "Do you want to save changes?", "Unsaved changes",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);

		if (result == JOptionPane.CANCEL_OPTION)
			return false;

		if (result == JOptionPane.YES_OPTION)
			requestSaveFile();

		return true;
	}

	private final void requestExit() {
		if (!askToSaveChanges())
			return;

		logger.info("Exiting.");
		System.exit(0);
	}

	private final void loadFile(File input) {
		tabbedPane.removeAll();

		if (!input.exists()) {
			String err = "File " + input + " doesn't exist!";
			logger.error(err);
			showError(err);
			return;
		}

		currentText = SummaryFileIO.loadText(input);

		int all = currentText.getTextWordsCount();
		List<MyTab> allTabs = new ArrayList<MyTab>();

		MyTab firstTab = new MyTab((int) (SUMMARIES_SIZES[0] * all), currentText.getText());
		allTabs.add(firstTab);

		MyTab toListen = firstTab;
		for (int i = 1; i < SUMMARIES_SIZES.length; i++) {
			MyTab tab = new MyTab((int) (SUMMARIES_SIZES[i] * all), currentText.getText());
			toListen.addChangeListener(tab);
			toListen = tab;
			allTabs.add(tab);
		}

		SortedSet<Integer> fullSelection = new TreeSet<Integer>();
		for (int i = 0; i < currentText.getText().length(); i++)
			fullSelection.add(i);
		firstTab.fullTextChanged(fullSelection);

		int i = 0;
		for (SortedSet<Integer> sum : currentText.getSummaries()) {
			allTabs.get(i++).setSummary(sum);
		}

		i = 0;
		for (MyTab tab : allTabs) {
			tab.addChangeListener(this);
			tabbedPane.addTab(((int) (SUMMARIES_SIZES[i++] * 100)) + "% summary", tab);
		}

		currentFile = input;
	}

	private void showError(String string) {
		JOptionPane.showMessageDialog(this, string, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private final class SummannotatorFileFilter extends FileFilter {
		@Override
		public String getDescription() {
			return "TXT files";
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".txt") || f.isDirectory();
		}
	}

	private class MainWindowListener extends WindowAdapter {

		private final ExtrSumAnnotator summanotator;

		public MainWindowListener(ExtrSumAnnotator sumannotator) {
			this.summanotator = sumannotator;
		}

		@Override
		public void windowClosing(WindowEvent we) {
			this.summanotator.requestExit();
		}
	}

	public void fullTextChanged(SortedSet<Integer> selectedCharsIndices) {
		unsavedChanges = true;
		save.setEnabled(true);
		setTitle(WINDOW_TITLE + " " + "[unsaved changes]");
	}

	public void undo() {
	}

	public void saveStateForUndo(boolean thisTabChanged) {
	}

}
