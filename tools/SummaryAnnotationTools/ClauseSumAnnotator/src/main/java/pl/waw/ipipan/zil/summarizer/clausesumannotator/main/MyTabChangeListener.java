package pl.waw.ipipan.zil.summarizer.clausesumannotator.main;

import java.util.List;

import pl.waw.ipipan.zil.summarizer.clausesumannotator.basic.Clause;

public interface MyTabChangeListener {

	public abstract void clausesChanged(List<Clause> clauses);

}