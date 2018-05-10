package pl.waw.ipipan.zil.summarizer.abssumannotator.main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
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
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.abssumannotator.basic.Text;
import pl.waw.ipipan.zil.summarizer.abssumannotator.io.SummaryFileIO;

public class AbsSumAnnotator extends JFrame implements Runnable {

	public static final int SUMMARIES_COUNT = 3;

	private static final String WINDOW_TITLE = "ASE v1.0";

	private static final long serialVersionUID = -3830790411182131318L;

	private static final Logger logger = Logger.getLogger(AbsSumAnnotator.class);

	private static final int TEXT_HEIGTH = 400;
	private static final int HEIGTH = 600;
	private static final int WIDTH = 800;

	private JMenuItem save;

	private File currentFile = null;
	private boolean unsavedChanges = false;

	private JTabbedPane tabbedPane;
	private JTextArea textPane;

	private float displayFontSize;

	private Text currentText;

	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			logger.error("Wrong usage! Should be: java -jar " + AbsSumAnnotator.class.getSimpleName() + " [input file]");
			return;
		}
		final AbsSumAnnotator summanno = new AbsSumAnnotator();
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
					@Override
					public void run() {
						summanno.loadFile(input);
					}
				});
			} else {
				logger.error("Input file: " + input + " doesn't exist!");
			}
		}
	}

	@Override
	public void run() {
		displayFontSize = loadDisplayFontSize();

		textPane = new JTextArea();
		textPane.setEditable(false);
		textPane.setLineWrap(true);
		textPane.setWrapStyleWord(true);
		textPane.setMargin(new Insets(10, 10, 10, 10));

		tabbedPane = new JTabbedPane();

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		splitPane.setBottomComponent(tabbedPane);
		splitPane.setDividerLocation(TEXT_HEIGTH);
		this.getContentPane().add(splitPane);

		this.setTitle(WINDOW_TITLE);
		this.createMenu();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new MainWindowListener());
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
			@Override
			public void actionPerformed(ActionEvent e) {
				requestOpenFile();
			}
		});
		file.add(open);

		save = new JMenuItem("Save");
		save.setEnabled(false);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestSaveFile();
			}
		});
		file.add(save);

		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestExit();
			}
		});
		file.add(exit);

		JMenu other = new JMenu("Other");

		ButtonGroup fontSizeButtonGroup = new ButtonGroup();
		JMenu setFontSize = new JMenu("Change font size");
		for (int z = 8; z < 25; z = z + 2) {

			final int currentFontSize = z;

			JRadioButtonMenuItem temp = new JRadioButtonMenuItem(z + "");
			if (z == displayFontSize) {
				temp.setSelected(true);
			}
			temp.addActionListener(new ActionListener() {
				@Override
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
			@Override
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
		URL helpURL = AbsSumAnnotator.class.getClassLoader().getResource("help.html");
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
		boolean success = SummaryFileIO.saveSummary(currentFile, currentText);

		if (success) {
			unsavedChanges = false;
			setTitle(WINDOW_TITLE + " file: " + currentFile.getName());
			save.setEnabled(false);
		} else {
			showError("Error saving file: " + currentFile);
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
		textPane.setText(currentText.getText());
		textPane.setCaretPosition(0);

		int allWords = currentText.getTextWordsCount();
		double[] proportions = { 0.2, 0.1, 0.05 };
		String[] labels = { "20%", "10%", "5%" };

		for (int i = 0; i < 3; i++) {
			int wordsInSumm = (int) (allWords * proportions[i]);
			MyTab tab = new MyTab(this, currentText.getSummary(i), i, allWords, wordsInSumm);
			tabbedPane.addTab(labels[i] + " summary", tab);
		}

		currentFile = input;
		setTitle(WINDOW_TITLE + " file: " + currentFile.getName());
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

		@Override
		public void windowClosing(WindowEvent we) {
			requestExit();
		}
	}

	public void summaryChanged(int summNumber, String text) {
		unsavedChanges = true;
		save.setEnabled(true);
		setTitle(WINDOW_TITLE + " file: " + currentFile.getName() + " " + "[unsaved changes]");
		currentText.setSummary(summNumber, text);
	}

}
