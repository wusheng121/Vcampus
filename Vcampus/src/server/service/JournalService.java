// server/service/JournalService.java
package server.service;

import common.model.Journal;
import java.util.List;

public interface JournalService {
    boolean addJournal(Journal journal);
    boolean updateJournal(Journal journal);
    boolean deleteJournal(int journalId);
    Journal getJournalById(int journalId);
    List<Journal> getAllJournals();
}
