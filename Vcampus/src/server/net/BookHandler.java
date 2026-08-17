package server.net;

import common.model.Book;
import common.model.BorrowRecord;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.service.AuthService;
import server.service.BookService;
import server.service.BookServiceImpl;
import server.service.BorrowRecordService;

import java.util.List;

/**
 * 图书馆：图书与借阅记录域
 */
public class BookHandler implements Handler {
    private final BookService bookService = new BookServiceImpl();
    private final BorrowRecordService borrowRecordService = new BorrowRecordService();
    private final AuthService auth = new AuthService();

    private boolean denied(Message request, Message response, String perm) {
        User c = request.getCaller();
        if (c == null || !auth.hasPermission(c.getType(), perm)) {
            response.setStatus("fail");
            response.setMsg("无权限：" + perm);
            return true;
        }
        return false;
    }

    @Override
    public Message handle(Message request) {
        Message response = new Message();
        switch (request.getType()) {
            case MessageType.FIND_ALL_BOOKS -> {
                response.setData(bookService.getAllBooks());
                response.setStatus("success");
                response.setMsg("获取所有书籍成功");
            }
            case MessageType.FIND_BOOKS_BY_CATEGORY -> {
                Book book = (Book) request.getData();
                response.setStatus("success");
                response.setData(bookService.findBookByCategory(book.getTheme()));
            }
            case MessageType.ADD_BOOK -> {
                if (denied(request, response, Permissions.BOOK_CREATE)) break;
                Book book = (Book) request.getData();
                boolean result = bookService.addBook(book);
                response.setStatus(result ? "success" : "fail");
                response.setMsg(result ? "新增书籍成功" : "新增书籍失败");
            }
            case MessageType.DELETE_BOOK -> {
                if (denied(request, response, Permissions.BOOK_DELETE)) break;
                Book book = (Book) request.getData();
                boolean result = bookService.deleteBook(book.getBookId());
                response.setStatus(result ? "success" : "fail");
                response.setMsg(result ? "删除成功" : "删除失败");
            }
            case MessageType.UPDATE_BOOK -> {
                if (denied(request, response, Permissions.BOOK_UPDATE)) break;
                Book book = (Book) request.getData();
                boolean result = bookService.updateBook(book.getBookId(), book);
                response.setStatus(result ? "success" : "fail");
                response.setMsg(result ? "修改成功" : "修改失败");
            }
            case MessageType.BORROW_BOOK -> {
                if (denied(request, response, Permissions.BOOK_BORROW)) break;
                BorrowRecord record = (BorrowRecord) request.getData();
                boolean result = borrowRecordService.borrowBook(record.getUserId(), record.getBookId());
                response.setStatus(result ? "success" : "fail");
                response.setMsg(result ? "借书成功" : "借书失败");
            }
            case MessageType.RETURN_BOOK -> {
                if (denied(request, response, Permissions.BOOK_RETURN)) break;
                BorrowRecord record = (BorrowRecord) request.getData();
                boolean result = borrowRecordService.returnBook(record);
                response.setStatus(result ? "success" : "fail");
                response.setMsg(result ? "还书成功" : "还书失败");
            }
            case MessageType.GET_ALL_RECORDS -> {
                response.setData(borrowRecordService.getAllRecords());
                response.setStatus("success");
                response.setMsg("获取所有借阅记录成功");
            }
            case MessageType.GET_USER_RECORDS -> {
                String userId = (String) request.getData();
                response.setData(borrowRecordService.getUserRecords(userId));
                response.setStatus("success");
                response.setMsg("获取用户借阅记录成功");
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
