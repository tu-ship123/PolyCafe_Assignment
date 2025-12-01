/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import poly.cafe.dao.UserDAO;
import poly.cafe.entity.User;
import poly.cafe.util.XAuth;

public class LoginTest {

    UserDAO dao;
    // DỮ LIỆU MẪU TRONG DB (Cần đảm bảo có sẵn)
    String validUser = "tu"; 
    String validPass = "123456";
    String managerUser = "tu"; // manager = true
    String staffUser = "nguyen";    // manager = false

    @BeforeEach
    public void setUp() { dao = new UserDAO(); }

    private boolean login(String u, String p) {
        if(u==null || u.isEmpty() || p==null || p.isEmpty()) return false;
        User user = dao.selectByID(u);
        return user != null && user.getPassword().equals(p);
    }

    // TC16: Login thành công
    @Test public void TC16_Login_Success() {
        Assertions.assertTrue(login(validUser, validPass));
    }

    // TC17: User trống
    @Test public void TC17_Login_EmptyUser() {
        Assertions.assertFalse(login("", validPass));
    }

    // TC18: Pass trống
    @Test public void TC18_Login_EmptyPass() {
        Assertions.assertFalse(login(validUser, ""));
    }

    // TC19: Cả 2 trống
    @Test public void TC19_Login_BothEmpty() {
        Assertions.assertFalse(login("", ""));
    }

    // TC20: Sai User
    @Test public void TC20_Login_WrongUser() {
        Assertions.assertFalse(login("UserAo", validPass));
    }

    // TC21: Sai Pass
    @Test public void TC21_Login_WrongPass() {
        Assertions.assertFalse(login(validUser, "SaiPass"));
    }

    // TC22: User biên tối đa (Giả sử max DB là 50)
    @Test public void TC22_Login_MaxLenUser() {
        // Nhập user rất dài -> Fail (hoặc Pass nếu đúng là user đó)
        String longUser = "A".repeat(50);
        Assertions.assertFalse(login(longUser, validPass));
    }

    // TC23: Pass biên tối đa
    @Test public void TC23_Login_MaxLenPass() {
        String longPass = "A".repeat(50);
        Assertions.assertFalse(login(validUser, longPass));
    }

    // --- CÁC TEST CASE PHÂN QUYỀN (TC24 - TC25) ---
    // Sử dụng XAuth và thuộc tính 'boolean manager' trong User
// --- CÁC TEST CASE PHÂN QUYỀN (Sửa lỗi NullPointer) ---

    @Test 
    @DisplayName("TC24: Phân quyền - Đăng nhập Quản lý")
    public void TC24_Role_Manager() {
        // 1. Lấy user từ DB
        User u = dao.selectByID(managerUser);
        Assertions.assertNotNull(u, "User Quản lý phải tồn tại");

        // 2. QUAN TRỌNG: Phải gán vào XAuth để tránh NullPointer
        XAuth.user = u; 

        // 3. Kiểm tra
        Assertions.assertTrue(XAuth.isManager(), "Phải là Quản lý (True)");
    }

    @Test 
    @DisplayName("TC25: Phân quyền - Đăng nhập Nhân viên")
    public void TC25_Role_Staff() {
        // 1. Lấy user từ DB
        User u = dao.selectByID(staffUser);
        Assertions.assertNotNull(u, "User Nhân viên phải tồn tại");

        // 2. QUAN TRỌNG: Phải gán vào XAuth
        XAuth.user = u;

        // 3. Kiểm tra
        Assertions.assertFalse(XAuth.isManager(), "Phải là Nhân viên (False)");
    }
}