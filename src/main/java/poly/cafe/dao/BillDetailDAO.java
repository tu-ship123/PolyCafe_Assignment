/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.cafe.entity.BillDetail;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class BillDetailDAO extends CrudDAO<BillDetail, Long> {
    // billDetail ko cần set id do tự tăng khi insert
    final String INSERT_SQL = "insert into BillDetails(billid, drinkid, unitprice, discount, quantity) values (?,?,?,?,?)";
    final String UPDATE_SQL = "UPDATE BillDetails SET billid = ?, drinkid=?, unitprice=?, discount=?, quantity=? WHERE Id = ?";
    final String DELETE_SQL = "DELETE FROM BillDetails WHERE Id = ?";
    final String SELECT_ALL_SQL = "SELECT * FROM BillDetails";
    final String SELECT_BY_ID_SQL = "SELECT * FROM BillDetails WHERE Id = ?";

    final String SELECT_BY_BILLID_SQL = "SELECT * FROM BillDetails WHERE Billid = ?";
    final String SELECT_BY_BILLID_DRINKID_SQL = "SELECT * FROM BillDetails WHERE Billid = ? AND DRINKID = ?";

    final String findByBillId_Lab4 = "SELECT bd.*, d.name AS drinkName FROM BillDetails bd JOIN Drinks d ON d.Id=bd.DrinkId WHERE bd.BillId=?";
    
    @Override
    public void insert(BillDetail entity) {
        XJDBC.update(INSERT_SQL, entity.getBillId(), entity.getDrinkId(), entity.getUnitPrice(), entity.getDiscount(), entity.getQuantity());
    }

    @Override
    public void update(BillDetail entity) {
        XJDBC.update(UPDATE_SQL, entity.getBillId(), entity.getDrinkId(), entity.getUnitPrice(), entity.getDiscount(), entity.getQuantity(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        XJDBC.update(DELETE_SQL, id);
    }

    @Override
    public List<BillDetail> selectAll() {
        return selectBySQL(SELECT_ALL_SQL);
    }

    public List<BillDetail> selectBillId(long billid) {
        return selectBySQL(SELECT_BY_BILLID_SQL, billid);
    }

    public List<BillDetail> selectBillId_lab4(long billid) {
        return selectBySQL2(findByBillId_Lab4, billid);
    }
    @Override
    public BillDetail selectByID(Long id) {
        List<BillDetail> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public BillDetail selectByBillDrinkId(Long billId, String drinkId) {
        List<BillDetail> list = selectBySQL(SELECT_BY_BILLID_DRINKID_SQL, billId, drinkId);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    @Override
    public List<BillDetail> selectBySQL(String sql, Object... args) {
        List<BillDetail> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                BillDetail entity = new BillDetail();
                entity.setId(rs.getLong("id"));
                entity.setBillId(rs.getLong("billid"));
                entity.setDrinkId(rs.getString("drinkid"));
                entity.setUnitPrice(rs.getDouble("unitprice"));
                entity.setDiscount(rs.getDouble("discount"));
                entity.setQuantity(rs.getInt("quantity"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // hiển thị bảng BillDetail có tên nước uống
    public List<BillDetail> selectBySQL2(String sql, Object... args) {
        List<BillDetail> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                BillDetail entity = new BillDetail();
                entity.setId(rs.getLong("id"));
                entity.setBillId(rs.getLong("billid"));
                entity.setDrinkId(rs.getString("drinkid"));
                entity.setDrinkName(rs.getString("drinkname"));
                entity.setUnitPrice(rs.getDouble("unitprice"));
                entity.setDiscount(rs.getDouble("discount"));
                entity.setQuantity(rs.getInt("quantity"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}

