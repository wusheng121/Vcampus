package server.dao;

import common.model.Journal;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JournalDAOImpl implements JournalDAO {

    @Override
    public boolean addJournal(Journal journal) {
        String sql = "INSERT INTO Journal (name, category, publish_date, publisher, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, journal.getName());
            ps.setString(2, journal.getCategory());
            ps.setDate(3, new Date(journal.getPublishDate().getTime()));
            ps.setString(4, journal.getPublisher());
            ps.setString(5, journal.getDescription());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateJournal(Journal journal) {
        String sql = "UPDATE Journal SET name=?, category=?, publish_date=?, publisher=?, description=? WHERE journal_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, journal.getName());
            ps.setString(2, journal.getCategory());
            ps.setDate(3, new Date(journal.getPublishDate().getTime()));
            ps.setString(4, journal.getPublisher());
            ps.setString(5, journal.getDescription());
            ps.setInt(6, journal.getJournalId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteJournal(int journalId) {
        String sql = "DELETE FROM Journal WHERE journal_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Journal getJournalById(int journalId) {
        String sql = "SELECT * FROM Journal WHERE journal_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, journalId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Journal journal = new Journal();
                journal.setJournalId(rs.getInt("journal_id"));
                journal.setName(rs.getString("name"));
                journal.setCategory(rs.getString("category"));
                journal.setPublishDate(rs.getDate("publish_date"));
                journal.setPublisher(rs.getString("publisher"));
                journal.setDescription(rs.getString("description"));
                return journal;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Journal> getAllJournals() {
        List<Journal> list = new ArrayList<>();
        String sql = "SELECT * FROM Journal";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Journal journal = new Journal();
                journal.setJournalId(rs.getInt("journal_id"));
                journal.setName(rs.getString("name"));
                journal.setCategory(rs.getString("category"));
                journal.setPublishDate(rs.getDate("publish_date"));
                journal.setPublisher(rs.getString("publisher"));
                journal.setDescription(rs.getString("description"));
                list.add(journal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
