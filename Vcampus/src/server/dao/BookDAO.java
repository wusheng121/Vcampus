package server.dao;

import common.model.Book;
import common.model.User;

import java.util.List;

public interface BookDAO {
    // 根据书名查找
    Book findBookByName(String bookName);

    //根据作者查找
    Book findBookByAuthor(String Author);

    // 添加新书本（由管理员添加）
    boolean addBook(Book book);

    //删除课本信息
    boolean deleteBook(String bookId);

    //根据编号查找
    Book findBookById(String bookId);

    List<Book> findBookByCategory(String theme);

    // 修改书本信息
    boolean updateBook(Book book);

    List<Book> getAllBooks();
}

