package pl.waw.ipipan.zil.summarizer.clausesumannotator.main;

import java.awt.Cursor;
import java.awt.Dimension;
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
import java.util.List;

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
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;
import pl.waw.ipipan.zil.summarizer.clausesumannotator.io.SummaryFileIO;
import pl.waw.ipipan.zil.summarizer.clausesumannotator.io.XMLReader;

public class ClauseSumAnnotator extends JFrame implements Runnable, MyTabChangeListener {

	private static final String WINDOW_TITLE = "Clause-extraction summary annotator";

	private static final long serialVersionUID = -3830790411182131318L;

	private static final Logger logger = Logger.getLogger(ClauseSumAnnotator.class);

	private static final int HEIGTH = 600;
	private static final int WIDTH = 800;

	private JMenuItem save;

	private File currentFile = null;
	private boolean unsavedChanges = false;

	private JTabbedPane tabbedPane;

	private float displayFontSize;

	public static void main(String[] args) {
		if (args.length != 0 && args.length != 1) {
			logger.error("Wrong usage! Should be: java -jar " + ClauseSumAnnotator.class.getSimpleName()
					+ " [input file]");
			return;
		}
		final ClauseSumAnnotator summanno = new ClauseSumAnnotator();
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
		// File fontsizeFile = new File(fontsizePath);
		// if (fontsizeFile.exists()) {
		// try {
		// Scanner s = new Scanner(fontsizeFile);
		// result = s.nextFloat();
		// s.close();
		// logger.info("Font size found in file: " + fontsizeFile);
		// } catch (FileNotFoundException e) {
		// logger.error(e.getLocalizedMessage());
		// }
		// } else {
		// logger.info("Font size not found in file: " + fontsizeFile);
		logger.info("Using default font size.");
		// }
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
		for (int z = 8; z < 30; z = z + 2) {

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

	protected void requestSetFontSize(int currentFontSize) {
		logger.info("Setting font size: " + currentFontSize);
		displayFontSize = currentFontSize;

		// File fontsizeFile = new File(fontsizePath);
		// logger.info("Saving font size in file: " + fontsizeFile);
		//
		// try {
		// Writer w = new FileWriter(fontsizeFile);
		// w.write(Float.toString(displayFontSize));
		// w.write("\n");
		// w.close();
		// } catch (IOException e) {
		// logger.error(e.getLocalizedMessage());
		// }
	}

	private final void requestShowHelp() {
		JFrame helpFrame = new JFrame("Help");
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		URL helpURL = ClauseSumAnnotator.class.getClassLoader().getResource("help.html");
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
		File targetFile = SummaryFileIO.getTargetFile(currentFile);
		boolean success = SummaryFileIO.saveSummary(targetFile, tabbedPane);

		if (success) {
			unsavedChanges = false;
			setTitle(WINDOW_TITLE);
			save.setEnabled(false);
		} else {
			showError("Error saving file: " + targetFile);
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
		List<Clause> clauses = new ArrayList<Clause>();
		String error = XMLReader.loadText(input, clauses);
		if (error != null) {
			logger.error(error);
			showError(error);
			return;
		}
		if (clauses.size() == 0) {
			String err = "No clauses found in file: " + input;
			logger.error(err);
			showError(err);
			return;
		}

		int all = getAllWordsCount(clauses);

		MyTab tab1 = new MyTab(all, 0.2, displayFontSize);
		MyTab tab2 = new MyTab(all, 0.1, displayFontSize);
		MyTab tab3 = new MyTab(all, 0.05, displayFontSize);

		tab1.addChangeListener(tab2);
		tab2.addChangeListener(tab3);
		tab1.clausesChanged(clauses);

		tabbedPane.addTab(tab1.getName(), tab1);
		tabbedPane.addTab(tab2.getName(), tab2);
		tabbedPane.addTab(tab3.getName(), tab3);

		currentFile = input;

		File summaryFile = SummaryFileIO.getTargetFile(input);
		if (summaryFile.exists()) {
			boolean success = SummaryFileIO.loadSummary(summaryFile, tabbedPane);
			if (success) {
				logger.info("Summary file found and loaded: " + summaryFile);
			} else {
				showError("Error loading summary file: " + summaryFile);
			}
		}

		tab1.addChangeListener(this);
		tab2.addChangeListener(this);
		tab3.addChangeListener(this);
	}

	private void showError(String string) {
		JOptionPane.showMessageDialog(this, string, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private static int getAllWordsCount(List<Clause> clauses) {
		int all = 0;
		for (Clause c : clauses)
			all += c.getWords().size();
		return all;
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

	private class MainWindowListener extends WindowAdapter {

		private final ClauseSumAnnotator summanotator;

		public MainWindowListener(ClauseSumAnnotator sumannotator) {
			this.summanotator = sumannotator;
		}

		@Override
		public void windowClosing(WindowEvent we) {
			this.summanotator.requestExit();
		}
	}

	@Override
	public void clausesChanged(List<Clause> clauses) {
		unsavedChanges = true;
		save.setEnabled(true);
		setTitle(WINDOW_TITLE + " " + "[unsaved changes]");
	}

}
