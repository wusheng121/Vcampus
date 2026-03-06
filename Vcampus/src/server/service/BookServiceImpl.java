package server.service;

import common.model.Book;
import server.dao.BookDAO;
import server.dao.BookDAOImpl;
import server.service.BookService;

import java.util.List;
import java.util.UUID;

public class BookServiceImpl implements BookService {

    private BookDAO bookDAO = new BookDAOImpl();

    @Override
    public Book findBookByName(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return null;
        }
        return bookDAO.findBookByName(bookName);
    }

    @Override
    public Book findBookByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return null;
        }
        return bookDAO.findBookByAuthor(author);
    }

    @Override
    public List<Book> findBookByCategory(String theme) {
        if (theme == null || theme.trim().isEmpty()) {
            return null;
        }
        return bookDAO.findBookByCategory(theme);
    }

    @Override
    public List<Book> getAllBooks() {
        // 需要在 DAO 中补充一个 getAllBooks() 方法
        return bookDAO.getAllBooks();
    }

    @Override
    public boolean addBook(Book book) {
        if (book == null) {
            return false;
        }

        // 如果没有 bookId，自动生成
        if (book.getBookId() == null || book.getBookId().trim().isEmpty()) {
            book.setBookId(UUID.randomUUID().toString());
        }

        // 校验书名和作者不能为空
        if (book.getBookName() == null || book.getBookName().trim().isEmpty()) {
            return false;
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            return false;
        }

        // 默认数量至少为 0
        if (book.getNumber() < 0) {
            book.setNumber(0);
        }

        // 默认状态
        if (book.getStatus() == null || book.getStatus().trim().isEmpty()) {
            book.setStatus("可借阅");
        }

        // 默认主题
        if (book.getTheme() == null) {
            book.setTheme("未分类");
        }

        //默认借阅人数
        if (book.getBorrowers() < 0) {
            book.setBorrowers(0);
        }

        return bookDAO.addBook(book);
    }


    @Override
    public boolean deleteBook(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return false;
        }
        Book book = new Book();
        book.setBookId(bookId);
        return bookDAO.deleteBook(bookId);
    }

    @Override
    public boolean updateBook(String bookId, Book newBook) {
        if (bookId == null || newBook == null) {
            return false;
        }
        //确保 newBook 的主键是原来的 bookId
        newBook.setBookId(bookId);
        return bookDAO.updateBook(newBook);
    }

//
//    @Override
//    public boolean borrowBook(String bookId) {
//        Book book = bookDAO.findBookById(bookId); //按ID查找
//        if (book == null || book.getNumber() <= 0) {
//            return false;
//        }
//        // 修改库存和借阅数量
//
//        book.setNumber(book.getNumber() - 1);
//        book.setBorrowers(book.getBorrowers() + 1);
//
//
//        return bookDAO.updateBook(book); //更新整个对象
//    }
//
//    @Override
//    public boolean returnBook(String bookId) {
//        Book book = bookDAO.findBookById(bookId); //按ID查找
//        if (book == null || book.getBorrowers() <= 0) {
//            return false;
//        }
//        // 修改库存和借阅数量
//        book.setNumber(book.getNumber() + 1);
//        book.setBorrowers(book.getBorrowers() - 1);
//
//        return bookDAO.updateBook(book); //更新整个对象
//    }
}
