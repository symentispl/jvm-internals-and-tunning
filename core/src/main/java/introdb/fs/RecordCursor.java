package introdb.fs;

import java.util.Iterator;

import introdb.record.PersistentRecord;

public interface RecordCursor extends Iterator<PersistentRecord>, AutoCloseable {

	int position();

}
