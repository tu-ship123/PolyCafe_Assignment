/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.cafe.entity.User;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class UserDAO extends CrudDAO<User, String> {
    final String INSERT_SQL = "INSERT INTO Users(UserName,Password,Enabled,Fullname,Photo,Manager) VALUES (?,?,?,?,?,?)";
    final String UPDATE_SQL = "UPDATE Users SET Password=?,Enabled=?,Fullname=?,Photo=?,Manager=? WHERE UserName = ?";
    final String DELETE_SQL = "DELETE FROM Users WHERE UserName = ?";
    final String SELECT_ALL_SQL = "SELECT * FROM Users";
    final String SELECT_BY_ID_SQL = "SELECT * FROM Users WHERE UserName = ?";

    @Override
    public void insert(User entity) {
        XJDBC.update(INSERT_SQL, entity.getUsername(), entity.getPassword(), entity.isEnabled(), entity.getFullname(), entity.getPhoto(), entity.isManager());
    }

    @Override
    public void update(User entity) {
        XJDBC.update(UPDATE_SQL, entity.getPassword(), entity.isEnabled(), entity.getFullname(), entity.getPhoto(), entity.isManager(), entity.getUsername());
    }

    @Override
    public void delete(String id) {
        XJDBC.update(DELETE_SQL, id);
    }

    @Override
    public List<User> selectAll() {
        return selectBySQL(SELECT_ALL_SQL);
    }
    @Override
    public User selectByID(String id) {
        List<User> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<User> selectBySQL(String sql, Object... args) {
        List<User> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                User entity = new User();
                entity.setUsername(rs.getString("UserName"));
                entity.setPassword(rs.getString("Password"));
                entity.setEnabled(rs.getBoolean("Enabled"));
                entity.setFullname(rs.getString("fullname"));
                entity.setPhoto(rs.getString("photo"));
                entity.setManager(rs.getBoolean("manager"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
