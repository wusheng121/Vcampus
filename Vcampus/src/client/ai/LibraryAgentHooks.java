package client.ai;

import client.controller.BookController;
import client.controller.BorrowRecordController;
import client.controller.JournalController;
import common.model.Book;
import common.model.BorrowRecord;
import common.model.Journal;
import common.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书馆模块的 Hook：提供 system prompt、数据集、欢迎文案
*/
public class LibraryAgentHooks implements AgentHooks {
    private final User user;
    private final BookController bookController = new BookController();
    private final BorrowRecordController recordController = new BorrowRecordController();
    private final JournalController journalController = new JournalController();

    public LibraryAgentHooks(User user) { this.user = user; }

    @Override
    public String systemPrompt() {
        return AgentPrompts.librarySystemPrompt();
    }

    @Override
    public void preload() {
        // 需要可预取数据（可留空）
        // bookController.getAllBooks();
        // recordController.getUserRecords(user.getUserId());
        // journalController.getAllJournals();
    }

    @Override
    public String buildDataset() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<DATASET>>\n");

        List<Book> books = bookController.getAllBooks();
        if (books != null && !books.isEmpty()) {
            sb.append("{\"books\":[\n");
            sb.append(books.stream().map(b -> String.format(
                    "  {\"id\":\"%s\",\"name\":\"%s\",\"author\":\"%s\",\"theme\":\"%s\",\"status\":\"%s\",\"stock\":%d,\"borrowers\":%d}",
                    safe(b.getBookId()), safe(b.getBookName()), safe(b.getAuthor()), safe(b.getTheme()),
                    safe(b.getStatus()), b.getNumber(), b.getBorrowers()
            )).collect(Collectors.joining(",\n")));
            sb.append("\n]},\n");
        }

        List<BorrowRecord> recs = recordController.getUserRecords(user.getUserId());
        if (recs != null && !recs.isEmpty()) {
            sb.append("{\"my_borrow_records\":[\n");
            sb.append(recs.stream().map(r -> String.format(
                    "  {\"id\":%d,\"book_id\":\"%s\",\"book_title\":\"%s\",\"borrow_date\":\"%s\",\"due_date\":\"%s\",\"return_date\":\"%s\",\"status\":\"%s\",\"fine\":%.2f}",
                    r.getId(), safe(r.getBookId()), safe(nz(r.getBookTitle())),
                    String.valueOf(r.getBorrowDate()), String.valueOf(r.getDueDate()),
                    String.valueOf(r.getReturnDate()), safe(nz(r.getStatus())), r.getFine()
            )).collect(Collectors.joining(",\n")));
            sb.append("\n]},\n");
        }

        List<Journal> js = journalController.getAllJournals();
        if (js != null && !js.isEmpty()) {
            sb.append("{\"journals\":[\n");
            sb.append(js.stream().map(j -> String.format(
                    "  {\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"publisher\":\"%s\",\"link\":\"%s\"}",
                    j.getJournalId(), safe(j.getName()), safe(j.getCategory()), safe(j.getPublisher()), safe(nz(j.getLink()))
            )).collect(Collectors.joining(",\n")));
            sb.append("\n]}\n");
        }

        sb.append("</DATASET>\n");
        return sb.toString();
    }

    @Override
    public String helpText() {
        return """
                我是你的图书馆助手机器人

                可用指令：
                • /help —— 查看帮助
                • /clear —— 清空对话与上下文

                我能做什么：
                • 查询馆藏书籍/作者/分类、可借状态
                • 查看你的借阅记录、是否逾期与欠费
                • 查看期刊信息（含分类/出版社/链接）
                """;
    }

    private static String nz(String s){ return s == null ? "" : s; }
    private static String safe(String s){
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ").replace("\r"," ");
    }
}
