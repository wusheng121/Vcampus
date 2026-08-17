package server.net;

import common.model.Journal;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.service.AuthService;
import server.service.JournalService;
import server.service.JournalServiceImpl;

import java.util.List;

/**
 * 期刊域
 */
public class JournalHandler implements Handler {
    private final JournalService journalService = new JournalServiceImpl();
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
            case MessageType.ADD_JOURNAL -> {
                if (denied(request, response, Permissions.JOURNAL_CREATE)) break;
                Journal journalToAdd = (Journal) request.getData();
                boolean addResult = journalService.addJournal(journalToAdd);
                response.setStatus(addResult ? "success" : "fail");
                response.setData(addResult);
                response.setMsg(addResult ? "新增期刊成功" : "新增期刊失败");
            }
            case MessageType.UPDATE_JOURNAL -> {
                if (denied(request, response, Permissions.JOURNAL_UPDATE)) break;
                Journal journalToUpdate = (Journal) request.getData();
                boolean updateResult = journalService.updateJournal(journalToUpdate);
                response.setStatus(updateResult ? "success" : "fail");
                response.setData(updateResult);
                response.setMsg(updateResult ? "更新期刊成功" : "更新期刊失败");
            }
            case MessageType.DELETE_JOURNAL -> {
                if (denied(request, response, Permissions.JOURNAL_DELETE)) break;
                int journalIdToDelete = (Integer) request.getData();
                boolean deleteResult = journalService.deleteJournal(journalIdToDelete);
                response.setStatus(deleteResult ? "success" : "fail");
                response.setData(deleteResult);
                response.setMsg(deleteResult ? "删除期刊成功" : "删除期刊失败");
            }
            case MessageType.GET_JOURNAL_BY_ID -> {
                int journalId = (Integer) request.getData();
                Journal journal = journalService.getJournalById(journalId);
                response.setStatus(journal != null ? "success" : "fail");
                response.setData(journal);
                response.setMsg(journal != null ? "获取期刊成功" : "未找到期刊");
            }
            case MessageType.GET_ALL_JOURNALS -> {
                List<Journal> journals = journalService.getAllJournals();
                response.setStatus("success");
                response.setData(journals);
                response.setMsg("获取所有期刊成功");
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
