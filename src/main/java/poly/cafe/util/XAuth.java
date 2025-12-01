/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.util;

import poly.cafe.entity.User;

/**
 *
 * @author ASUS
 */
public class XAuth {
    public static User user = null;
    
    public static void clear(){
        XAuth.user = null;
    }
    
    public static boolean isLogin(){
        return XAuth.user !=null;// user khác null thì trả về true
    }
    //Kiểm tra user và trưởng phòng hay nhân viên
    public static boolean isManager(){
        return XAuth.isLogin()&& user.isManager();
    }
}
