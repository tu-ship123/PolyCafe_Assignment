/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import poly.cafe.entity.Revenue;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class RevenueDAO {
    String revenueByCategorySql
        = "SELECT category.Name AS Category, "
        + "SUM(detail.UnitPrice * detail.Quantity * (1 - detail.Discount)) AS Revenue, "
        + "SUM(detail.Quantity) AS Quantity, "
        + "MIN(detail.UnitPrice) AS MinPrice, "
        + "MAX(detail.UnitPrice) AS MaxPrice, "
        + "AVG(detail.UnitPrice) AS AvgPrice "
        + "FROM BillDetails AS detail "
        + "JOIN Drinks AS drink ON drink.Id = detail.DrinkId "
        + "JOIN Categories AS category ON category.Id = drink.CategoryId "
        + "JOIN Bills AS bill ON bill.Id = detail.BillId "
        + "WHERE bill.Status = 1 "
        + "AND bill.Checkout IS NOT NULL "
        + "AND bill.Checkout BETWEEN ? AND ? "
        + "GROUP BY category.Name "
        + "ORDER BY Revenue DESC";
    
    String revenueByUserSql
    = "SELECT bill.Username AS [User], "
    + "SUM(detail.UnitPrice * detail.Quantity * (1 - detail.Discount)) AS Revenue, "
    + "COUNT(DISTINCT detail.BillId) AS Quantity, "
    + "MIN(bill.Checkin) AS FirstTime, "
    + "MAX(bill.Checkin) AS LastTime "
    + "FROM BillDetails AS detail "
    + "JOIN Bills AS bill ON bill.Id = detail.BillId "
    + "WHERE bill.Status = 1 "
    + "AND bill.Checkout IS NOT NULL "
    + "AND bill.Checkout BETWEEN ? AND ? "
    + "GROUP BY bill.Username "
    + "ORDER BY Revenue DESC";
    
    public List<Revenue.ByCategory> selectRevenueByCategory(Date from, Date to) {
        List<Revenue.ByCategory> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(revenueByCategorySql, from, to);
            while (rs.next()) {
                Revenue.ByCategory entity = Revenue.ByCategory.builder()
                    .category(rs.getString("Category"))
                    .revenue(rs.getDouble("Revenue"))
                    .quantity(rs.getInt("Quantity"))
                    .minPrice(rs.getDouble("MinPrice"))
                    .maxPrice(rs.getDouble("MaxPrice"))
                    .avgPrice(rs.getDouble("AvgPrice"))
                    .build();
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<Revenue.ByUser> selectRevenueByUser(Date from, Date to) {
        List<Revenue.ByUser> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(revenueByUserSql, from, to);
            while (rs.next()) {
                Revenue.ByUser entity = Revenue.ByUser.builder()
                    .user(rs.getString("User"))
                    .revenue(rs.getDouble("Revenue"))
                    .quantity(rs.getInt("Quantity"))
                    .firstTime(rs.getTimestamp("FirstTime"))
                    .lastTime(rs.getTimestamp("LastTime"))
                    .build();
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
