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
import model.entities.Customer;


@Stateless
@Path("/rest/api/v1/customer")
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
        List<Customer> customers = super.findAll();
        List<Customer> customerDTOs = new LinkedList<Customer>();
        for(Customer c : customers){
            customerDTOs.add(new Customer(c));
        }
        return Response.ok(customerDTOs).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(@PathParam("id") Long id){
        Customer customer = em.find(Customer.class, id);
        //Only 1 customer may be found with the ID.
        if (customer!=null){
            Customer customercopy = new Customer(customer);
            return Response.status(Response.Status.OK).entity(customercopy).build();
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
            if(c.getPassword()==null || c.getUsername()==null){
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing necessary information to create customer.").build();
            }else{
                em.persist(c);
            }
        }else{
            //Edit customer with provided info.
            super.edit(c);
        }
        return Response.ok().entity(new Customer(c)).build();
    }
}