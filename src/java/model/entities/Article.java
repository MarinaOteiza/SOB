package model.entities;

import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Entity
@XmlRootElement
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Article_Gen", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Article_Gen")
    private Long id;
    
    @Column(nullable = false)
    private String title;
    private String author;
    
    @Column(length = 500)
    private String summary; // Expected to be trimmed to 20 words
    private LocalDate publicationDate;
    private double views; // Views stored as double to support abbreviations like 3.3k
    private String featuredImageUrl;
    private boolean priv;
    
    @ManyToMany // Updated: Many-to-Many relationship with `Topic`
    @JoinTable(
        name = "ARTICLE_TOPIC", // Join table for Article <-> Topic
        joinColumns = @JoinColumn(name = "ARTICLE_ID"),
        inverseJoinColumns = @JoinColumn(name = "TOPIC_ID")
    )
    private List<Topic> topics=new ArrayList<>();
    // Getters and Setters
    public List<Topic> getTopic() {
        return topics;
    }
    public void setTopics(Topic topic1, Topic topic2) {
       topics.add(topic1);
       topics.add(topic2);
    }
    
    public void setId(Long id) {
        this.id = id;
    }
     public Long getId() {
        return id;
    }
    
      public boolean getPriv() {
        return priv;
    }
    
    
    public void setPriv(boolean priv) {
        this.priv = priv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        if (summary != null) {
            String[] words = summary.split("\\s+");
            if (words.length > 20) {
                this.summary = String.join(" ", List.of(words).subList(0, 20)) + "...";
            } else {
                this.summary = summary;
            }
        } else {
            this.summary = null;
        }
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public double getViews() {
        return views;
    }

    public void setViews(double views) {
        this.views = views;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    // Overrides
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Article)) {
            return false;
        }
        Article other = (Article) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Article[ title=" + title + ", author=" + author + " ]";
    }
}