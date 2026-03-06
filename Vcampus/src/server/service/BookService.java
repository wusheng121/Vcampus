package server.service;

import common.model.Book;
import java.util.List;

public interface BookService {

    // 根据书名查找
    Book findBookByName(String bookName);

    // 根据作者查找
    Book findBookByAuthor(String author);

    // 根据作者查找
    List<Book> findBookByCategory(String theme);

    // 查询所有书籍
    List<Book> getAllBooks();

    // 添加新书（管理员操作）
    boolean addBook(Book book);

    // 删除书籍
    boolean deleteBook(String bookId);

    // 修改书籍信息
    boolean updateBook(String bookId, Book newBook);

//    // 借书（借阅数+1，库存-1）
//    boolean borrowBook(String bookId);
//
//    // 还书（借阅数-1，库存+1）
//    boolean returnBook(String bookId);
}
