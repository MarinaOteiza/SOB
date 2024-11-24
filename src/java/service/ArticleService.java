/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import model.entities.Article;
import model.entities.Customer;
/**
 *
 * @author usuario
 */
    public class ArticleService extends AbstractFacade<Article>{
    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    public ArticleService() {
        super(Article.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Article> findByFilters(
        @QueryParam("topic1") String topic1,
        @QueryParam("topic2") String topic2, 
        @QueryParam("author") String author) {

    // Construcción de la consulta dinámica
    StringBuilder queryString = new StringBuilder("SELECT a FROM Article a WHERE 1=1");

    // Condición para filtrar por topic1 y topic2 si están presentes
    if (topic1 != null && !topic1.isEmpty()) {
        queryString.append(" AND (a.category.name = :topic1");
        if (topic2 != null && !topic2.isEmpty()) {
            queryString.append(" OR a.category.name = :topic2");
        }
        queryString.append(")");
    }

    // Condición para filtrar por author si está presente
    if (author != null && !author.isEmpty()) {
        queryString.append(" AND a.author = :author");
    }

    // Ordenar por popularidad en orden descendente
    queryString.append(" ORDER BY a.views DESC");

    // Crear consulta JPA
    jakarta.persistence.Query query = em.createQuery(queryString.toString(), Article.class);

    // Asignar parámetros si están presentes
    if (topic1 != null && !topic1.isEmpty()) {
        query.setParameter("topic1", topic1);
    }
    if (topic2 != null && !topic2.isEmpty()) {
        query.setParameter("topic2", topic2);
    }
    if (author != null && !author.isEmpty()) {
        query.setParameter("author", author);
    }

    // Ejecutar la consulta y devolver resultados
    return query.getResultList();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(
            @PathParam("id") Long id,
            @HeaderParam("Authorization") String authToken) {

        // Buscar el artículo por su ID
        Article article = em.find(Article.class, id);
        if (article == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Article not found")
                    .build();
        }

        // Verificar si el artículo es privado
        if (article.getPriv()) { // Uso del método getter para 'privateArticle'
            if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("You must be authenticated to access this private article")
                        .build();
            }
        }

        // Incrementar el número de visualizaciones
        article.setViews(article.getViews() + 1);
        em.merge(article);

        // Retornar el artículo completo
        return Response.ok(article).build();
    }

   
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteArticle(
    @PathParam("id") Long id,
    @HeaderParam("Authorization") String authToken) {

    // Buscar el artículo por su ID
    Article article = em.find(Article.class, id);
    if (article == null) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Article not found")
                .build();
    }

    // Verificar si el usuario está autenticado
    if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be authenticated to delete an article")
                .build();
    }

    // Verificar si el usuario es el autor del artículo
    String username = getUsernameFromToken(authToken);
    if (username == null || !article.getAuthor().equals(username)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be the author of the article to delete it")
                .build();
    }

    // Eliminar el artículo
    em.remove(article);

    // Retornar respuesta de éxito
    return Response.status(Response.Status.NO_CONTENT).build();
    }


    @POST
    @Path("rest/api/v1/article")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createArticle(
        @HeaderParam("Authorization") String authToken,
        Article article) {

    // Verificar si el usuario está autenticado
    if (authToken == null || authToken.isEmpty() || !isUserAuthenticated(authToken)) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("You must be authenticated to create an article")
                .build();
    }

    // Validar los tópicos
    

    // Determinar la fecha de publicación automáticamente
    article.setPublicationDate(LocalDate.now());

    // Verificar si el usuario que intenta crear el artículo existe
    Customer customer = findCustomerByToken(authToken);
    if (customer == null) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("User not found or not authenticated")
                .build();
    }

    // Aquí puedes asociar el artículo con el usuario autenticado si fuera necesario
    // article.setAuthor(customer.getUsername()); // Si quieres asociar el nombre del usuario con el artículo

    // Persistir el artículo en la base de datos
    em.persist(article);

    // Devolver el código de estado HTTP 201 Created junto con el identificador del artículo
    return Response.status(Response.Status.CREATED)
            .entity("{\"id\": \"" + article.getId() + "\"}")
            .build();
    }


    
    // Método auxiliar para encontrar un cliente por el token
    private Customer findCustomerByToken(String authToken) {
        // Aquí deberías buscar al usuario por su token de autenticación.
        // Suponemos que puedes hacerlo mediante una consulta en la base de datos.
        return em.createQuery("SELECT c FROM Customer c WHERE c.authToken = :authToken", Customer.class)
                .setParameter("authToken", authToken)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    // Método auxiliar para verificar la autenticación
    private boolean isUserAuthenticated(String authToken) {
        // Aquí se valida el token. Esto puede involucrar una llamada a un sistema de autenticación externo.
        // Por simplicidad, asumimos que un token válido es "valid_token".
        return "valid_token".equals(authToken);
    }
    private String getUsernameFromToken(String authToken) {
    // Aquí deberías implementar la lógica para obtener el nombre de usuario desde el token. 
    // Este ejemplo asume que el token es "valid_token" y el nombre de usuario es "author_name".
    if ("valid_token".equals(authToken)) {
        return "author_name";  // Cambia esto por la lógica real de extracción de usuario desde el token.
    }
    return null;
}
}