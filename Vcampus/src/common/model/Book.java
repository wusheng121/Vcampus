package common.model;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号

    private String bookId;    // 书本编号
    private String bookName;  // 书名
    private String author;    // 作者（注意拼写改正）
    private int number;       // 库存数量
    private String status;    // 状态：可借阅/无剩余
    private String theme;     //主题
    private int borrowers;    //借阅人数

    // 无参构造函数（序列化/反射需要）
    public Book() {}

    // 带参构造函数
    public Book(String bookId, String bookName, String author, int number, String status) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.number = number;
        this.status = status;
    }

    // Getter 和 Setter 方法
    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    public String getTheme() {
        return theme;
    }
    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getBorrowers() {
        return borrowers;
    }
    public void setBorrowers(int borrowers) {
        this.borrowers = borrowers;
    }

    // 可选：toString 便于调试
    @Override
    public String toString() {
        return "Book{" +
                "bookId='" + bookId + '\'' +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", number=" + number +
                ", status='" + status + '\'' +
                '}';
    }
}
