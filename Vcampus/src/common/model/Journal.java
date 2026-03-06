package common.model;

import java.io.Serializable;
import java.util.Date;

public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    private int journalId;
    private String name;
    private String category;
    private Date publishDate;
    private String publisher;
    private String description;
    private String link;

    public int getJournalId() { return journalId; }
    public void setJournalId(int journalId) { this.journalId = journalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getPublishDate() { return publishDate; }
    public void setPublishDate(Date publishDate) { this.publishDate = publishDate; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
