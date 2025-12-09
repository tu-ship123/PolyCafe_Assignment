/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ASUS
 */
public class XJDBC {

    private static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String dburl = "jdbc:sqlserver://localhost;database=PolyCafe;encrypt=true;trustServerCertificate=true";

    // Mật khẩu mặc định ở máy local của bạn
    private static String user = "sa";
    private static String pass = "123456";

    // Viết PreparedStatement. Object... args: ds các tham số
    public static PreparedStatement getStmt(String sql, Object... args) throws SQLException {
        
        // --- ĐOẠN CODE MỚI THÊM VÀO ---
        // Logic: Nếu tìm thấy biến môi trường DB_PASSWORD (trên GitHub) thì dùng nó.
        // Còn không thì cứ dùng biến 'pass' (123456) như cũ.
        String currentPass = pass;
        if (System.getenv("DB_PASSWORD") != null) {
            currentPass = System.getenv("DB_PASSWORD");
        }
        // -----------------------------

        Connection connection = DriverManager.getConnection(dburl, user, currentPass); // Sử dụng currentPass thay vì pass
        PreparedStatement pstmt;

        if (sql.trim().startsWith("{")) {
            pstmt = connection.prepareCall(sql); // thủ tục (Stored Procedure)
        } else {
            pstmt = connection.prepareStatement(sql); // câu lệnh thường
        }

        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]); // gán tham số từ 1
        }
        return pstmt;
    }

    // Phương thức update = insert + update + delete:
    public static int update(String sql, Object... args) {
        try {
            PreparedStatement stmt = getStmt(sql, args);
            try {
                return stmt.executeUpdate();
            } finally {
                stmt.getConnection().close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức thực hiện truy vấn sql dùng select:
    public static ResultSet query(String sql, Object... args) {
        try {
            PreparedStatement stmt = getStmt(sql, args);
            return stmt.executeQuery();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Trả về 1 đối tượng 
    public static Object value(String sql, Object... args) {
        try (ResultSet rs = query(sql, args)) {
            if (rs.next()) {
                return rs.getObject(1); // trong jdbc bắt đầu từ 1
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}