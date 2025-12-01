/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.util;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class MsgBox {

    // Hiển thị hộp thoại thông báo
    public static void alert(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Hệ thống quản lý", JOptionPane.INFORMATION_MESSAGE);
    }

    // Hiển thị hộp thoại xác nhận (Yes/No)
    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message,
                "Hệ thống quản lý",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // Hiển thị hộp thoại nhập liệu
    public static String prompt(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message,
                "Hệ thống quản lý", JOptionPane.INFORMATION_MESSAGE);
    }
}
