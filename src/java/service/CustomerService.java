/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.entities.Article;
import model.entities.Topic;
import model.entities.Customer;
/**
 *
 * @author usuario
 */
public class CustomerService {
      
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;
    @GET
@Path("/rest/api/v1/customer")
@Produces(MediaType.APPLICATION_JSON)
public Response getAllCustomers(@HeaderParam("Authorization") String authToken) {
    // Verificar si el usuario está autenticado
    if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be authenticated to access the customer list")
                .build();
    }

    // Obtener todos los clientes
    List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();

    // Crear la lista para devolver
    List<Map<String, Object>> customersData = new ArrayList<>();

    // Iterar sobre cada cliente
    for (Customer customer : customers) {
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("id", customer.getId());
        customerData.put("name", customer.getUsername());
        customerData.put("email", customer.getEmail());
        // Aquí no incluimos la contraseña

        // Verificar si el cliente es autor de algún artículo
        List<Article> articles = em.createQuery("SELECT a FROM Article a WHERE a.author = :author", Article.class)
                .setParameter("author", customer.getUsername())
                .getResultList();

        if (!articles.isEmpty()) {
            // Si es autor, añadir el enlace al último artículo publicado
            Article latestArticle = articles.get(articles.size() - 1); // El último artículo publicado
            Map<String, String> links = new HashMap<>();
            links.put("article", "/article/" + latestArticle.getId());
            customerData.put("links", links);
        }

        // Añadir los datos del cliente a la lista
        customersData.add(customerData);
    }

    // Retornar la lista de clientes como respuesta
    return Response.ok(customersData).build();
}
@GET
@Path("/customer/{id}")
@Produces(MediaType.APPLICATION_JSON)
public Response getCustomerById(@PathParam("id") Long id, @HeaderParam("Authorization") String authToken) {
    // Verificar si el usuario está autenticado
    if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be authenticated to access customer details")
                .build();
    }

    // Buscar el cliente por ID
    Customer customer = em.find(Customer.class, id);
    if (customer == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Customer not found")
                .build();
    }

    // Crear un mapa para retornar los datos del cliente
    Map<String, Object> customerData = new HashMap<>();
    customerData.put("id", customer.getId());
    customerData.put("name", customer.getUsername());
    customerData.put("email", customer.getEmail());
    // No se incluye la contraseña

    return Response.ok(customerData).build();
}
@PUT
@Path("/customer/{id}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public Response updateCustomer(@PathParam("id") Long id, Customer updatedCustomer, @HeaderParam("Authorization") String authToken) {
    // Verificar si el usuario está autenticado
    if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be authenticated to update customer data")
                .build();
    }

    // Buscar el cliente por ID
    Customer customer = em.find(Customer.class, id);
    if (customer == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Customer not found")
                .build();
    }

    // Actualizar los datos del cliente
    customer.setUsername(updatedCustomer.getUsername());
    customer.setEmail(updatedCustomer.getEmail());
    // Aquí podrías actualizar otros campos, pero no la contraseña

    // Persistir los cambios
    em.merge(customer);

    return Response.status(Response.Status.NO_CONTENT).build();
}
 // Método auxiliar para verificar la autenticación
    private boolean isUserAuthenticated(String authToken) {
        // Aquí se valida el token. Esto puede involucrar una llamada a un sistema de autenticación externo.
        // Por simplicidad, asumimos que un token válido es "valid_token".
        return "valid_token".equals(authToken);
    }
}