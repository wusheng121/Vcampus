package client.controller;
import common.net.MessageType;

import client.net.ClientSocket;
import common.model.Book;
import common.net.Message;

import java.util.ArrayList;
import java.util.List;

public class BookController {
    private ClientSocket clientSocket = ClientSocket.getInstance();

    // 查询所有书籍
    public List<Book> getAllBooks() {
        try {
            Message request = new Message();
            request.setType(MessageType.FIND_ALL_BOOKS);

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

    // 根据类别查询书籍

    public List<Book> findBooksByCategory(String category) {
        try {
            Book book = new Book();
            book.setTheme(category);

            Message request = new Message();
            request.setType(MessageType.FIND_BOOKS_BY_CATEGORY);
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
            request.setType(MessageType.ADD_BOOK);
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
            request.setType(MessageType.DELETE_BOOK);
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
            request.setType(MessageType.UPDATE_BOOK);
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
            request.setType(MessageType.GET_ALL_CATEGORIES);

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
