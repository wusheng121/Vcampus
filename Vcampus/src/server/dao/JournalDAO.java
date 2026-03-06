package server.dao;

import common.model.Journal;
import java.util.List;

public interface JournalDAO {
    boolean addJournal(Journal journal);
    boolean updateJournal(Journal journal);
    boolean deleteJournal(int journalId);
    Journal getJournalById(int journalId);
    List<Journal> getAllJournals();
}
