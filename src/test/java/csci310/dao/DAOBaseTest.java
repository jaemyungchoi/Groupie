package csci310.dao;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DAOBaseTest {

    @Test
    public void testGetConnection() throws SQLException {
        DAOBase base = new DAOBase(System.getProperty("dbUrl"));
        try(Connection connection = base.getConnection();
            PreparedStatement queryForeignKeys = connection.prepareStatement("PRAGMA foreign_keys")) {
            try(ResultSet result = queryForeignKeys.executeQuery()) {
                assertEquals(1, result.getInt("foreign_keys"));
            }
        }
    }
}