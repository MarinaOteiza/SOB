package model.entities;

import authn.Credentials;
import jakarta.persistence.Column;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@Entity
@Table(name = "CUSTOMER")
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "Customer_Gen", sequenceName = "CUSTOMER_GEN", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Customer_Gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @ManyToMany
    private List<Article> art;

    @OneToOne
    @JoinColumn(name = "credentials_id", referencedColumnName = "id")
    private Credentials credentials;

    public Customer() {
    }
    public Customer(String username, String password, String email, List<Article> art){
        this.username=username;
        this.password=password;
        this.email=email;
        this.art=art;
    }
    public Customer(Customer c) {
        this.username = c.getUsername();
        this.password = c.getPassword();
        this.email = c.getEmail();
    }
    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
    
        public boolean isAuthor() {
        // El usuario es autor si tiene artículos asociados
        return art != null && !art.isEmpty();
    }

    public String getArticleLink() {
        // Si el usuario es autor y tiene artículos, devolver el enlace al último artículo
        if (isAuthor()) {
            Article lastArticle = art.get(art.size() - 1);  // Último artículo de la lista
            return "/article/" + lastArticle.getId(); // Suponiendo que Article tiene un método getId()
        }
        return null;  // Si no es autor, no se proporciona el enlace
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "Customer[ username=" + username + ", email=" + email + " ]";
    }
}
