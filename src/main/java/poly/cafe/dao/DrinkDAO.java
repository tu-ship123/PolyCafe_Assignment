/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.cafe.entity.Drink;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class DrinkDAO extends CrudDAO<Drink, String> {
    final String INSERT_SQL = "insert into Drinks(id,name, unitprice,discount,image,available,categoryid) values (?,?,?,?,?,?,?)";
    final String UPDATE_SQL = "UPDATE Drinks SET name = ?, unitprice=?,discount=?, image=?, available=?, categoryid=? WHERE Id = ?";
    final String DELETE_SQL = "DELETE FROM Drinks WHERE Id = ?";
    final String SELECT_ALL_SQL = "SELECT * FROM Drinks";
    final String SELECT_BY_ID_SQL = "SELECT * FROM Drinks WHERE Id = ?";
    final String SELECT_BY_Category_SQL = "SELECT * FROM Drinks WHERE CategoryId = ?";

    @Override
    public void insert(Drink entity) {
        XJDBC.update(INSERT_SQL, entity.getId(), entity.getName(), entity.getUnitPrice(), entity.getDiscount(), entity.getImage(), entity.isAvailable(), entity.getCategoryId());
    }

    @Override
    public void update(Drink entity) {
        XJDBC.update(UPDATE_SQL, entity.getName(), entity.getUnitPrice(), entity.getDiscount(), entity.getImage(), entity.isAvailable(), entity.getCategoryId(), entity.getId());
    }

    @Override
    public void delete(String id) {
        XJDBC.update(DELETE_SQL, id);
    }

    @Override
    public List<Drink> selectAll() {
        return selectBySQL(SELECT_ALL_SQL);
    }

    @Override
    public Drink selectByID(String id) {
        List<Drink> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Drink> selectBySQL(String sql, Object... args) {
        List<Drink> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                Drink entity = new Drink();
                entity.setId(rs.getString("Id"));
                entity.setName(rs.getString("Name"));
                entity.setImage(rs.getString("image"));
                entity.setUnitPrice(rs.getFloat("unitprice"));
                entity.setDiscount(rs.getFloat("discount"));
                entity.setAvailable(rs.getBoolean("available"));
                entity.setCategoryId(rs.getString("Categoryid"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Lọc tất cả với categoryID = id trong Drink
    public List<Drink> selectAllCategoryID(String id) {
        List<Drink> list = selectBySQL(SELECT_BY_Category_SQL, id);
        return list;
    }
}
