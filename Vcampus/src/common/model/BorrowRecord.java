package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 */
public class BorrowRecord implements Serializable {
    private int id;             // 记录 ID（主键，自增）
    private String userId;      // 借书人 ID（关联用户表）
    private String bookId;      // 图书 ID（关联 book 表）
    private String bookTitle;
    private LocalDateTime borrowDate;    // 借书时间
    private LocalDateTime returnDate;    // 还书时间（未归还则为 null）
    private LocalDateTime dueDate;     // 应还日期
    private String status;      // 状态：借出 / 已还
    private double fine;               // 罚款金额

    public BorrowRecord() {}

    public BorrowRecord(int id, String userId, String bookId, String bookTitle, LocalDateTime borrowDate, LocalDateTime returnDate, LocalDateTime dueDate,String status, double fine) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.status = status;
        this.fine = fine;
    }

    public BorrowRecord(String userId, String bookId) {
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = LocalDateTime.now();
        //this.dueDate = LocalDateTime.now();
    }

    // getter 和 setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }
    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }
    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public double getFine() {
        return fine;
    }
    public void setFine(double fine) {
        this.fine = fine;
    }

    // 方便打印调试
    @Override
    public String toString() {
        return "BorrowRecord{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", borrowDate=" + borrowDate +
                ", returnDate=" + returnDate +
                ", dueDate='" + dueDate + '\'' +
                ", status='" + status + '\'' +
                ", fine='" + fine + '\'' +
                '}';
    }
}
