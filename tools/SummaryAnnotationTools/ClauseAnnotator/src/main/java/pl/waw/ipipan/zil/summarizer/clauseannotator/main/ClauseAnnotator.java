package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Document;
import pl.waw.ipipan.zil.summarizer.clauseannotator.io.XMLReader;
import pl.waw.ipipan.zil.summarizer.clauseannotator.io.XMLWriter;

public class ClauseAnnotator extends JFrame implements Runnable {

	private static final String WINDOW_TITLE = "Clause and marker annotator";

	private static final long serialVersionUID = -3830790411182131318L;

	private static final Logger logger = Logger.getLogger(ClauseAnnotator.class);

	private static final int HEIGTH = 600;
	private static final int WIDTH = 800;

	public static final String verbposregexpFilename = "verb.regexp";
	public static String VERB_POS_REGEXP = null;

	static {
		loadVerPosRegexp();
	}

	private JMenuItem save;

	private File currentFile = null;

	private MainSplitPanel mainSplitPane;

	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			logger.error("Wrong usage! Should be: java -jar " + ClauseAnnotator.class.getSimpleName() + " [input file]");
			return;
		}
		final ClauseAnnotator clauseannotator = new ClauseAnnotator();
		try {
			SwingUtilities.invokeAndWait(clauseannotator);
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
						clauseannotator.loadFile(input);
					}
				});
			} else {
				logger.error("Input file: " + input + " doesn't exist!");
			}
		}
	}

	private static void loadVerPosRegexp() {
		InputStream f = ClauseAnnotator.class.getClassLoader().getResourceAsStream(verbposregexpFilename);
		if (f != null) {
			String regex = null;
			try {
				BufferedReader bw = new BufferedReader(new InputStreamReader(f));
				regex = bw.readLine();

				Pattern.compile(regex);
			} catch (Exception e) {
				logger.error("Error reading regexp file: " + e.getLocalizedMessage());
				return;
			}
			VERB_POS_REGEXP = regex;
			logger.info("Loaded verb regexp: " + regex);
		}
	}

	public void run() {
		this.setTitle(WINDOW_TITLE);
		mainSplitPane = new MainSplitPanel(this);
		this.getContentPane().add(mainSplitPane);
		this.createMenu();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new MainWindowListener(this));
		this.setSize(WIDTH, HEIGTH);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGTH) / 2);
		this.setVisible(true);
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
		save.setEnabled(false);
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
		JMenuItem help = new JMenuItem("Help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestShowHelp();
			}
		});

		other.add(help);
		menu.add(other);
	}

	private void requestSaveFile() {
		boolean success = XMLWriter.saveFile(currentFile, mainSplitPane.getSentences());

		if (success) {
			setTitle(WINDOW_TITLE);
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
		if (!save.isEnabled())
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

	private final void requestShowHelp() {
		JFrame helpFrame = new JFrame("Help");
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		URL helpURL = ClauseAnnotator.class.getClassLoader().getResource("help.html");
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

	private final void loadFile(File input) {
		if (!input.exists()) {
			String err = "File " + input + " doesn't exist!";
			logger.error(err);
			showError(err);
			return;
		}
		Document doc = new Document();
		String error = XMLReader.loadText(input, doc);
		if (error != null) {
			logger.error(error);
			showError(error);
			return;
		}
		if (doc.getSentences().size() == 0) {
			String err = "No clauses found in file: " + input;
			logger.error(err);
			showError(err);
			return;
		}

		mainSplitPane.setSentences(doc.getSentences());

		currentFile = input;
		save.setEnabled(false);
		setTitle(WINDOW_TITLE);
	}

	private void showError(String string) {
		JOptionPane.showMessageDialog(this, string, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private final class SummannotatorFileFilter extends FileFilter {
		@Override
		public String getDescription() {
			return "XML files";
		}

		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".xml") || f.isDirectory();
		}
	}

	public void annotationChanged() {
		save.setEnabled(true);
		setTitle(WINDOW_TITLE + " " + "[unsaved changes]");
	}

	private class MainWindowListener extends WindowAdapter {

		private ClauseAnnotator summanotator;

		public MainWindowListener(ClauseAnnotator sumannotator) {
			this.summanotator = sumannotator;
		}

		public void windowClosing(WindowEvent we) {
			this.summanotator.requestExit();
		}
	}

}
