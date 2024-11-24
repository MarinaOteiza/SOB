package model.entities;

import java.io.Serializable;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
    
    private String title;
    private String author;
    private String summary; // Expected to be trimmed to 20 words
    private LocalDate publicationDate;
    private double views; // Views stored as double to support abbreviations like 3.3k
    private String featuredImageUrl;
    private boolean priv;
     
    @ManyToMany
    private Topic topic1;
    private Topic topic2;
    // Getters and Setters
      public List<Topic> getTopic() {
    List<Topic> l = new ArrayList<>();
    l.add(topic1);
    l.add(topic2);
    return l;
}
       public void setTopic(Topic topic1, Topic topic2) {
          this.topic1=topic1;
          this.topic2=topic2;
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
        this.summary = summary;
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