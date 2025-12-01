/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import poly.cafe.entity.Bill;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class BillDAO extends CrudDAO<Bill, Long> {
    final String INSERT_SQL = "insert into Bills(username, cardid,checkin,checkout,status) values (?,?,?,?,?)";
    final String UPDATE_SQL = "UPDATE Bills SET username = ?,cardid=?,checkin=?,checkout=?,status=? WHERE Id = ?";
    final String DELETE_SQL = "DELETE FROM Bills WHERE Id = ?";
    final String SELECT_ALL_SQL = "SELECT * FROM Bills";
    final String SELECT_BY_ID_SQL = "SELECT * FROM Bills WHERE Id = ?";

    //chua thanhtoan # null: "SELECT * FROM Bills WHERE CardId=? AND Status=0";
    final String SELECT_BY_IDSTATUS0_SQL = "SELECT * FROM Bills WHERE CardId=? AND Status=0";
    String findByTimeRangeSql = "SELECT * FROM Bills WHERE Checkin BETWEEN ? AND ? ORDER BY Checkin DESC";

    @Override
    public void insert(Bill entity) {
        XJDBC.update(INSERT_SQL, entity.getUsername(), entity.getCardId(), entity.getCheckin(), entity.getCheckout(), entity.getStatus());
    }

    @Override
    public void update(Bill entity) {
        XJDBC.update(UPDATE_SQL, entity.getUsername(), entity.getCardId(), entity.getCheckin(), entity.getCheckout(), entity.getStatus(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        XJDBC.update(DELETE_SQL, id);
    }

    @Override
    public List<Bill> selectAll() {
        return selectBySQL(SELECT_ALL_SQL);
    }

    public List<Bill> findByTimeRange(Date begin, Date end) {
        return selectBySQL(findByTimeRangeSql,begin,end);
    }

    @Override
    public Bill selectByID(Long id) {
        List<Bill> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    // hàm lấy id có trạng thái status = 0 : chua thanh toán
    public Bill selectByIDStatus0(Integer id) {
        List<Bill> list = selectBySQL2(SELECT_BY_IDSTATUS0_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    // lấy cả ngày và giờ
    @Override
    public List<Bill> selectBySQL(String sql, Object... args) {
        List<Bill> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                Bill entity = new Bill();
                entity.setId(rs.getLong("id"));
                entity.setUsername(rs.getString("username"));
                entity.setCardId(rs.getInt("cardid"));
                entity.setCheckin(rs.getTimestamp("Checkin")); // để lấy cả ngày + giờ
                entity.setCheckout(rs.getTimestamp("Checkout"));
                entity.setStatus(rs.getInt("status"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // chỉ lấy ngày:
    public List<Bill> selectBySQL2(String sql, Object... args) {
        List<Bill> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                Bill entity = new Bill();
                entity.setId(rs.getLong("id"));
                entity.setUsername(rs.getString("username"));
                entity.setCardId(rs.getInt("cardid"));
                entity.setCheckin(rs.getDate("checkin")); // chỉ lấy ngày
                entity.setCheckout(rs.getDate("checkout"));
                entity.setStatus(rs.getInt("status"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer selectMaxBillId() {
        String sql = "SELECT MAX(Id) AS MaxId FROM Bills";
        try {
            ResultSet rs = XJDBC.query(sql);
            if (rs.next()) {
                int maxId = rs.getInt("MaxId");
                rs.getStatement().getConnection().close();
                return maxId;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    
    public List<Bill> findByUserAndTimeRange(String username, Date begin, Date end) {
        String sql = "SELECT * FROM Bills WHERE username = ? AND checkin BETWEEN ? AND ? ORDER BY checkin DESC";
        return selectBySQL(sql, username, begin, end);
    }
}
