/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poly.cafe.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import poly.cafe.entity.Card;
import poly.cafe.util.XJDBC;

/**
 *
 * @author acer
 */
public class CardDAO extends CrudDAO<Card, Integer> {

    final String INSERT_SQL = "insert into Cards(id,status) values (?,?)";
    final String UPDATE_SQL = "UPDATE Cards SET status = ? WHERE Id = ?";
    final String DELETE_SQL = "DELETE FROM Cards WHERE Id = ?";
    final String SELECT_ALL_SQL = "SELECT * FROM Cards";
    final String SELECT_BY_ID_SQL = "SELECT * FROM Cards WHERE Id = ?";

    @Override
    public void insert(Card entity) {
        XJDBC.update(INSERT_SQL, entity.getId(), entity.getStatus());
    }

    @Override
    public void update(Card entity) {
        XJDBC.update(UPDATE_SQL, entity.getStatus(), entity.getId());
    }

    @Override
    public void delete(Integer id) {
        XJDBC.update(DELETE_SQL, id);
    }

    @Override
    public List<Card> selectAll() {
        return selectBySQL(SELECT_ALL_SQL);
    }

    @Override
    public Card selectByID(Integer id) {
        List<Card> list = selectBySQL(SELECT_BY_ID_SQL, id);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<Card> selectBySQL(String sql, Object... args) {
        List<Card> list = new ArrayList<>();
        try {
            ResultSet rs = XJDBC.query(sql, args);
            while (rs.next()) {
                Card entity = new Card();
                entity.setId(rs.getInt("Id"));
                entity.setStatus(rs.getInt("status"));
                list.add(entity);
            }
            rs.getStatement().getConnection().close();
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
