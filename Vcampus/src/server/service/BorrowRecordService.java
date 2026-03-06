package server.service;

import common.model.BorrowRecord;
import server.dao.BorrowRecordDAO;
import server.dao.BorrowRecordDAOImpl;
import server.dao.BookDAO;
import server.dao.BookDAOImpl;
import common.model.Book;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class BorrowRecordService {

    private BorrowRecordDAO recordDAO = new BorrowRecordDAOImpl();
    private BookDAO bookDAO = new BookDAOImpl();

    // 借书
    public boolean borrowBook(String userId, String bookId) {
        Book book = bookDAO.findBookById(bookId);
        if (book == null || book.getNumber() <= 0) {
            return false;
        }

        // 更新库存
        book.setNumber(book.getNumber() - 1);
        book.setBorrowers(book.getBorrowers() + 1);
        boolean bookUpdated = bookDAO.updateBook(book);

        if (!bookUpdated) return false;

        // 插入借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setReturnDate(null);
        record.setDueDate(record.getBorrowDate().plusDays(30));
        record.setStatus("借出");
        record.setFine(0);

        return recordDAO.addRecord(record);
    }

    // 还书
    // 还书
    public boolean returnBook(BorrowRecord record) {
        // 先查找对应的借阅记录（通过 recordId）
        BorrowRecord dbRecord = recordDAO.findById(record.getId());
        if (dbRecord == null) {
            return false;
        }

        String bookId = dbRecord.getBookId();
        Book book = bookDAO.findBookById(bookId);
        if (book == null) {
            return false;
        }

        // 更新库存：数量+1，借阅人数-1
        book.setNumber(book.getNumber() + 1);
        book.setBorrowers(book.getBorrowers() - 1);
        boolean bookUpdated = bookDAO.updateBook(book);
        if (!bookUpdated) {
            return false;
        }

        // 更新借阅记录
        LocalDateTime now = LocalDateTime.now();
        dbRecord.setReturnDate(now);
        dbRecord.setStatus("已还");

        // 计算是否逾期
        if (dbRecord.getDueDate() != null && now.isAfter(dbRecord.getDueDate())) {
            long overdueDays = Duration.between(dbRecord.getDueDate(), now).toDays();
            double finePerDay = 1.0; // 每天罚款 1 元（可改为配置）
            dbRecord.setFine(overdueDays * finePerDay);
        } else {
            dbRecord.setFine(0);
        }

        return recordDAO.updateRecord(dbRecord);
    }


    // 查看某用户的所有借阅记录
    public List<BorrowRecord> getUserRecords(String userId) {
        return recordDAO.findRecordsByUser(userId);
    }

    // 查看所有借阅记录
    public List<BorrowRecord> getAllRecords() {
        return recordDAO.getAllBorrowRecords();
    }
}
