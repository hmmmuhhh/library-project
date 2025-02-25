import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Connection successful: " + conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}