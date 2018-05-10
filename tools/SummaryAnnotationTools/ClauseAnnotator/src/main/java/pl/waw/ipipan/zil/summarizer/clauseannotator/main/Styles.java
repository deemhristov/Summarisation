package pl.waw.ipipan.zil.summarizer.clauseannotator.main;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import pl.waw.ipipan.zil.summarizer.clauseannotator.basic.Word;

public class Styles {

	private static SimpleAttributeSet handleStyle;
	private static SimpleAttributeSet whitespaceStyle;

	static {
		handleStyle = new SimpleAttributeSet();
		StyleConstants.setBold(handleStyle, true);

		whitespaceStyle = new SimpleAttributeSet();
	}

	public static SimpleAttributeSet getWordStyle(Word w, boolean selected) {
		SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
		if (w.getClause().getContinuedClause() != null)
			StyleConstants.setBackground(defaultStyle, Color.getHSBColor(0.069f, 0.20f, 0.99f));

		if (w.getMarker() != null) {
			StyleConstants.setUnderline(defaultStyle, true);
			if (w.getMarker().getNUC().equals(""))
				StyleConstants.setBackground(defaultStyle, Color.magenta);
		}

		if (selected)
			StyleConstants.setBackground(defaultStyle, Color.yellow);

		if (w.isVerb())
			StyleConstants.setBold(defaultStyle, true);

		return defaultStyle;
	}

	public static SimpleAttributeSet getHandleStyle() {
		return handleStyle;
	}

	public static SimpleAttributeSet getWhitespaceStyle() {
		return whitespaceStyle;
	}

}
