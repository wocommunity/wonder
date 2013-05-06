package er.neo4jadaptor.utils.cursor;

import java.util.Iterator;

import er.neo4jadaptor.utils.iteration.FlattenedIterator;


public class FlattenedCursor <V> extends FlattenedIterator<V> implements Cursor<V> {

	public FlattenedCursor(Cursor<Cursor<V>> it) {
		super(it);
	}

	public void close() {
		external().close();
		Cursor<V> internal = internal();
		
		if (internal != null) {
			internal.close();
		}
	}

	@Override
	protected Cursor<? extends Iterator<V>> external() {
		return (Cursor<? extends Iterator<V>>) super.external();
	}

	@Override
	protected Cursor<V> internal() {
		return (Cursor<V>) super.internal();
	}
}
