package er.extensions.migration;

public class ERXMigrationForeignKey {
	private ERXMigrationColumn[] _sourceColumns;
	private ERXMigrationColumn[] _destinationColumns;
	
	public ERXMigrationForeignKey(ERXMigrationColumn[] sourceColumns, ERXMigrationColumn[] destinationColumns) {
		_sourceColumns = sourceColumns;
		_destinationColumns = destinationColumns;
	}
	
	public ERXMigrationColumn[] sourceColumns() {
		return _sourceColumns;
	}
	
	public ERXMigrationColumn[] destinationColumns() {
		return _destinationColumns;
	}
}
