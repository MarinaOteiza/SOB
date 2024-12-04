package service;

import authn.Secured;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import model.entities.Article;
import model.entities.Customer;


@Stateless
@Path("/customer")
public class CustomerService extends AbstractFacade<Customer> {
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public CustomerService() {
        super(Customer.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    @GET
@Produces(MediaType.APPLICATION_JSON)
public Response getCustomers(){
    List<Customer> customers = super.findAll();  // Obtén todos los customers
    List<Customer> customerDTO = new LinkedList<Customer>();  // Cambiar a CustomerDTO para manejar el artículo
    
    for (Customer c : customers) {
        // Crear un DTO de Customer
        Customer customerCopy = new Customer();
        customerCopy.setId(c.getId());
        customerCopy.setUsername(c.getUsername());
        customerCopy.setEmail(c.getEmail());

        // Verificar si es autor
        if (isAuthor(c.getId())) {
            customerCopy.setArticleLink(c.getLink());
        }
        
        customerDTO.add(customerCopy);  // Añadir el DTO a la lista
    }

    return Response.ok(customerDTO).build();  // Devolver los DTOs
}
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(@PathParam("id") Long id){
        Customer c = em.find(Customer.class, id);
        //Only 1 customer may be found with the ID.
        if (c!=null){
           Customer customerCopy= new Customer();
           customerCopy.setId(c.getId());
           customerCopy.setUsername(c.getUsername());
           customerCopy.setEmail(c.getEmail());
           return Response.status(Response.Status.OK).entity(customerCopy).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Customer with this id does not exist.").build();
    }
    
    @PUT
    @Path("/{id}")
    @Secured
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putCustomer(@PathParam("id") Long id, Customer c){
        Customer customer = em.find(Customer.class, id);
        if (customer==null){
            //Control info provided, if new customer, password and username is required.
            if(c.getCredentials().getPassword()==null || c.getUsername()==null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing necessary information to create customer.").build();
            }else{
                em.persist(c);
            }
        }else{
            //Edit customer with provided info.
            super.edit(c);
        }
        Customer customerCopy= new Customer();
        customerCopy.setUsername(c.getUsername());
        customerCopy.setEmail(c.getEmail());
        customerCopy.getCredentials().setPassword(c.getCredentials().getPassword());
        customerCopy.getCredentials().setUsername(customerCopy.getUsername());
        return Response.ok().entity(customerCopy).build();
    }
    
        public boolean isAuthor(Long id) {
            boolean isAuthor = false;

            try {
                // Consulta para verificar si el id está presente en la tabla CUSTOMER_ARTICLE
                String jpql = "SELECT COUNT(ca) FROM CustomerArticle ca WHERE ca.customerId = :id";
                Query query = em.createQuery(jpql);
                query.setParameter("id", id);

                // Obtener el resultado de la consulta
                Long count = (Long) query.getSingleResult();
                if (count != null && count > 0) {
                    isAuthor = true;
                }
            } catch (Exception e) {
                e.printStackTrace(); // Manejo básico de excepciones; puedes mejorarlo
            }
            return isAuthor;
        }
}