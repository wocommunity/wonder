# Improves the support for SQL Server with WebObjects.

This plugin uses sequences to generate primary keys instead of the `EO_PK_TABLE` strategy used by the standard `MicrosoftPlugIn`. It obtains the sequence name per entity according to the following algorithm:

```java
entity.primaryKeyRootName() + "_seq".
```

It throws an exception if a sequence is not found.

## Additional Setup Info

Specify these parameters in EntityModeler to connect to the SQL Server database:

- **URL**: jdbc:sqlserver://<host>:<port>;databaseName=<db_name>;[;property=value...]
- **Driver**: com.microsoft.sqlserver.jdbc.SQLServerDriver
- **Plugin**: ERMicrosoftPlugIn