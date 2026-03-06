package server.dao;

import common.model.BorrowRecord;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAOImpl implements BorrowRecordDAO {

    @Override
    public List<BorrowRecord> findRecordsByUser(String userId) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, b.book_name AS book_title " +
                "FROM borrow_record r " +
                "JOIN book b ON r.book_id = b.book_id " +
                "WHERE r.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord record = mapRow(rs);
                record.setBookTitle(rs.getString("book_title")); // ⭐ 额外取书名
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public List<BorrowRecord> findRecordsByBook(String bookId) {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, b.book_name AS book_title " +
                "FROM borrow_record r " +
                "JOIN book b ON r.book_id = b.book_id " +
                "WHERE r.book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRecord record = mapRow(rs);
                record.setBookTitle(rs.getString("book_title"));
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean addRecord(BorrowRecord record) {
        String sql = "INSERT INTO borrow_record(user_id, book_id, borrow_date, due_date, status, fine) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getUserId());
            ps.setString(2, record.getBookId());
            ps.setTimestamp(3,Timestamp.valueOf(record.getBorrowDate()));
            ps.setTimestamp(4,Timestamp.valueOf(record.getDueDate()));
            ps.setString(5, record.getStatus());
            ps.setDouble(6, record.getFine());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

//    @Override
//    public boolean updateRecord(BorrowRecord record) {
//        String sql = "UPDATE borrow_record SET return_date=?, status=? WHERE id=?";
//        try (Connection conn = DBUtil.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            if (record.getReturnDate() != null) {
//                ps.setTimestamp(1, Timestamp.valueOf(record.getReturnDate()));
//            } else {
//                ps.setNull(1, Types.TIMESTAMP);
//            }
//            ps.setString(2, record.getStatus());
//            ps.setDouble(3, record.getFine());
//            //ps.setInt(4, record.getId());
//            return ps.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
@Override
public boolean updateRecord(BorrowRecord record) {
    String sql = "UPDATE borrow_record SET return_date=?, status=?, fine=? WHERE id=?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        if (record.getReturnDate() != null) {
            ps.setTimestamp(1, Timestamp.valueOf(record.getReturnDate()));
        } else {
            ps.setNull(1, Types.TIMESTAMP);
        }
        ps.setString(2, record.getStatus());
        ps.setDouble(3, record.getFine());
        ps.setInt(4, record.getId());

        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}



    @Override
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT r.*, b.book_name AS book_title " +
                "FROM borrow_record r " +
                "JOIN book b ON r.book_id = b.book_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BorrowRecord record = mapRow(rs);
                record.setBookTitle(rs.getString("book_title"));
                list.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public BorrowRecord findById(int id) {
        String sql = "SELECT r.*, b.book_name AS book_title " +
                "FROM borrow_record r " +
                "JOIN book b ON r.book_id = b.book_id " +
                "WHERE r.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BorrowRecord record = mapRow(rs);
                record.setBookTitle(rs.getString("book_title"));
                return record;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 映射工具方法
    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getString("user_id"));
        record.setBookId(rs.getString("book_id"));

        Timestamp borrowTs = rs.getTimestamp("borrow_date");
        record.setBorrowDate(borrowTs != null ? borrowTs.toLocalDateTime() : null);

        Timestamp returnTs = rs.getTimestamp("return_date");
        record.setReturnDate(returnTs != null ? returnTs.toLocalDateTime() : null);

        Timestamp dueTs = rs.getTimestamp("due_date");
        record.setDueDate(dueTs != null ? dueTs.toLocalDateTime() : null);

        record.setStatus(rs.getString("status"));
        record.setFine(rs.getDouble("fine"));

        return record;
    }

}
