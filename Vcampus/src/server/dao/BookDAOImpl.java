package server.dao;

import common.model.Book;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookDAOImpl implements BookDAO {

    @Override
    public Book findBookByName(String bookName) {
        String sql = "SELECT * FROM book WHERE book_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Book findBookByAuthor(String author) {
        String sql = "SELECT * FROM book WHERE author = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, author);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Book findBookById(String bookId) {
        String sql = "SELECT * FROM book WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Book> findBookByCategory(String category) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book WHERE theme = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
    }



    @Override
    public boolean addBook(Book book) {
        String sql = "INSERT INTO book(book_id, book_name, author, `number`, status, theme, borrowers) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getBookId());
            ps.setString(2, book.getBookName());
            ps.setString(3, book.getAuthor());
            ps.setInt(4, book.getNumber());
            ps.setString(5, book.getStatus());
            ps.setString(6, book.getTheme());
            ps.setInt(7, book.getBorrowers());

            int rows = ps.executeUpdate();
            System.out.println("[BookDAOImpl] 插入书籍结果: " + rows + " 行, 书籍ID=" + book.getBookId());
            return rows > 0;
        } catch (Exception e) {
            System.err.println("[BookDAOImpl] 插入书籍失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    public boolean deleteBook(String bookId) {
        String sql = "DELETE FROM book WHERE book_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    //修改书籍信息（推荐用这个方法，支持更新多个字段）
    public boolean updateBook(Book book) {
        String sql = "UPDATE book SET book_name=?, author=?, number=?, status=?, theme=?, borrowers=? WHERE book_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getBookName());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getNumber());
            ps.setString(4, book.getStatus());
            ps.setString(5, book.getTheme());
            ps.setInt(6, book.getBorrowers());
            ps.setString(7, book.getBookId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //列出所有书籍
    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM book";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToBook(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 工具方法：把 ResultSet 映射成 Book 对象
    private Book mapResultSetToBook(ResultSet rs) throws Exception {
        Book book = new Book();
        book.setBookId(rs.getString("book_id"));
        book.setBookName(rs.getString("book_name"));
        book.setAuthor(rs.getString("author"));
        book.setNumber(rs.getInt("number"));
        book.setStatus(rs.getString("status"));
        book.setTheme(rs.getString("theme"));
        book.setBorrowers(rs.getInt("borrowers"));
        return book;
    }
}
