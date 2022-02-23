package csci310.dao;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import java.sql.*;

public class DAOBase {

    static {
        new JDBC(); // HACK: load java.sql.JDBC so DriverManager finds the correct driver
    }

    protected final String dbURL;

    public DAOBase(String dbURL) { this.dbURL = dbURL; }

    // make sure foreign key support is enabled for this connection
    public Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        return DriverManager.getConnection(dbURL, config.toProperties());
    }
}
