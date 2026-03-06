// server/service/JournalServiceImpl.java
package server.service;

import common.model.Journal;
import server.dao.JournalDAO;
import server.dao.JournalDAOImpl;
import java.util.List;

public class JournalServiceImpl implements JournalService {
    private JournalDAO journalDAO = new JournalDAOImpl();

    @Override
    public boolean addJournal(Journal journal) {
        return journalDAO.addJournal(journal);
    }

    @Override
    public boolean updateJournal(Journal journal) {
        return journalDAO.updateJournal(journal);
    }

    @Override
    public boolean deleteJournal(int journalId) {
        return journalDAO.deleteJournal(journalId);
    }

    @Override
    public Journal getJournalById(int journalId) {
        return journalDAO.getJournalById(journalId);
    }

    @Override
    public List<Journal> getAllJournals() {
        return journalDAO.getAllJournals();
    }
}
