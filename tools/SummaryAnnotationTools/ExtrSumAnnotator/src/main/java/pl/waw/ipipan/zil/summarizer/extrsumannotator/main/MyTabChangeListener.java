package pl.waw.ipipan.zil.summarizer.extrsumannotator.main;

import java.util.SortedSet;

public interface MyTabChangeListener {

	public abstract void fullTextChanged(SortedSet<Integer> selectedCharsIndices);

	public abstract void undo();

	public abstract void saveStateForUndo(boolean thisTabChanged);

}