package client.controller;

import client.net.ClientSocket;
import common.model.Book;
import common.net.Message;

import java.util.ArrayList;
import java.util.List;

public class BookController {
    private ClientSocket clientSocket = new ClientSocket();

    // 查询所有书籍
    public List<Book> getAllBooks() {
        try {
            Message request = new Message();
            request.setType("findAllBooks");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<Book>) response.getData();
            } else {
                System.out.println("查询所有书籍失败：" + response.getMsg());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据书名查询书籍
    public Book findBookByName(String bookName) {
        try {
            Book book = new Book();
            book.setBookName(bookName);

            Message request = new Message();
            request.setType("findBookByName");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Book) response.getData();
            } else {
                System.out.println("按书名查询失败：" + response.getMsg());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据作者查询书籍
    public Book findBookByAuthor(String author) {
        try {
            Book book = new Book();
            book.setAuthor(author);

            Message request = new Message();
            request.setType("findBookByAuthor");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Book) response.getData();
            } else {
                System.out.println("按作者查询失败：" + response.getMsg());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据ID查询书籍
    public Book findBookById(String bookId) {
        try {
            Book book = new Book();
            book.setBookId(bookId);

            Message request = new Message();
            request.setType("findBookById");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Book) response.getData();
            } else {
                System.out.println("按ID查询失败：" + response.getMsg());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据类别查询书籍

    public List<Book> findBooksByCategory(String category) {
        try {
            Book book = new Book();
            book.setTheme(category);

            Message request = new Message();
            request.setType("findBooksByCategory");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                // 服务端应该返回 List<Book>
                return (List<Book>) response.getData();
            } else {
                System.out.println("按类别查询失败：" + response.getMsg());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    // 新增书籍（管理员操作）
    public boolean addBook(Book book) {
        try {
            Message request = new Message();
            request.setType("addBook");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除书籍
    public boolean deleteBook(String bookId) {
        try {
            Book book = new Book();
            book.setBookId(bookId);

            Message request = new Message();
            request.setType("deleteBook");
            request.setData(book);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新书籍信息
    public boolean updateBook(String bookId, Book newBook) {
        try {
            // 直接把 bookId 放在 newBook 中，让服务端知道要修改哪本书
            newBook.setBookId(bookId);

            Message request = new Message();
            request.setType("updateBook");
            request.setData(newBook);

            Message response = clientSocket.sendRequest(request);
            return "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // BookController.java
    public List<String> getAllCategories() {
        try {
            Message request = new Message();
            request.setType("getAllCategories");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<String>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


}
