package client.controller;

import client.net.ClientSocket;
import common.model.BorrowRecord;
import common.net.Message;

import java.time.LocalDateTime;
import java.util.List;

public class BorrowRecordController {

    private ClientSocket clientSocket = new ClientSocket();

    public boolean handleBorrow(String userId, String bookId) {
        try {
            BorrowRecord record = new BorrowRecord(userId, bookId);

            Message request = new Message();
            request.setType("borrowBook");
            request.setData(record);

            Message response = clientSocket.sendRequest(request);
            return response != null && "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean handleReturn(int recordId) {
        try {
            BorrowRecord record = new BorrowRecord();
            record.setId(recordId);
            record.setReturnDate(LocalDateTime.now());

            Message request = new Message();
            request.setType("returnBook");
            request.setData(record);

            Message response = clientSocket.sendRequest(request);
            return response != null && "success".equals(response.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<BorrowRecord> getAllRecords() {
        try {
            Message request = new Message();
            request.setType("getAllRecords");
            // data 可不传
            Message response = clientSocket.sendRequest(request);
            if (response != null && "success".equals(response.getStatus())) {
                return (List<BorrowRecord>) response.getData(); // 假设服务端返回 List<BorrowRecord>
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<BorrowRecord> getUserRecords(String userId) {
        try {
            Message request = new Message();
            request.setType("getUserRecords");
            request.setData(userId);
            Message response = clientSocket.sendRequest(request);
            if (response != null && "success".equals(response.getStatus())) {
                return (List<BorrowRecord>) response.getData(); // 假设服务端返回 List<BorrowRecord>
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    // 更新借阅记录（用于还书）
//    public boolean updateRecord(BorrowRecord record) {
//        try {
//            Message request = new Message();
//            request.setType("updateBorrowRecord");
//            request.setData(record);
//
//            Message response = clientSocket.sendRequest(request);
//            return "success".equals(response.getStatus());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

}
