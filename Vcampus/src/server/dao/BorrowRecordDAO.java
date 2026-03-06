package server.dao;

import common.model.BorrowRecord;
import java.util.List;

public interface BorrowRecordDAO {
    // 查找某个用户的所有借阅记录
    List<BorrowRecord> findRecordsByUser(String userId);

    // 查找某本书的所有借阅记录
    List<BorrowRecord> findRecordsByBook(String bookId);

    // 添加新的借阅记录
    boolean addRecord(BorrowRecord borrowRecord);

    // 更新借阅记录（比如还书时更新状态/归还日期）
    boolean updateRecord(BorrowRecord borrowRecord);

    // 列出所有记录
    List<BorrowRecord> getAllBorrowRecords();

    BorrowRecord findById(int id);
}
