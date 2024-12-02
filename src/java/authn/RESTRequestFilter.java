package authn;

import com.sun.xml.messaging.saaj.util.Base64;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.StringTokenizer;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import model.entities.Article;

/**
 * @author Marc Sanchez
 */
@Priority(Priorities.AUTHENTICATION)
@Provider
public class RESTRequestFilter implements ContainerRequestFilter {
    private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";      
    
    // to access the resource class and resource method matched by the current request
    @Context
    private ResourceInfo resourceInfo;

    @PersistenceContext(unitName = "Homework1PU")
    private EntityManager em;

    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        Method method = resourceInfo.getResourceMethod();
        if (method != null && method.getName().equals("findArticleById")) {
            // Obtener el parámetro del ID del artículo desde la URL o el contexto
            String articleId = requestCtx.getUriInfo().getPathParameters().getFirst("id");
            if (articleId != null) {
                try {
                    // Consulta JPQL para verificar si el artículo es privado
                    TypedQuery<Boolean> query = em.createQuery(
                        "SELECT a.isPrivate FROM Article a WHERE a.id = :id", Boolean.class);
                    Boolean isPrivate = query.setParameter("id", Long.parseLong(articleId)).getSingleResult();

                    if (Boolean.TRUE.equals(isPrivate)) {
                        // Si el artículo es privado, realizar autenticación
                        List<String> headers = requestCtx.getHeaders().get(HttpHeaders.AUTHORIZATION);

                        if (headers != null && !headers.isEmpty()) {
                            String username;
                            String password;
                            try {
                                String auth = headers.get(0);
                                auth = auth.replace(AUTHORIZATION_HEADER_PREFIX, "");
                                String decode = Base64.base64Decode(auth);
                                StringTokenizer tokenizer = new StringTokenizer(decode, ":");
                                username = tokenizer.nextToken();
                                password = tokenizer.nextToken();
                            } catch (@SuppressWarnings("unused") Exception e) {
                                requestCtx.abortWith(
                                    Response.status(Response.Status.BAD_REQUEST).build()
                                );
                                return;
                            }

                            try {
                                // Validar credenciales
                                TypedQuery<Credentials> credQuery = em.createNamedQuery("Credentials.findUser", Credentials.class);
                                Credentials c = credQuery.setParameter("username", username)
                                    .getSingleResult();
                                if (!c.getPassword().equals(password)) {
                                    requestCtx.abortWith(
                                        Response.status(Response.Status.FORBIDDEN).build()
                                    );
                                }
                            } catch (@SuppressWarnings("unused") NoResultException e) {
                                requestCtx.abortWith(
                                    Response.status(Response.Status.UNAUTHORIZED).build()
                                );
                            }
                        } else {
                            requestCtx.abortWith(
                                Response.status(Response.Status.UNAUTHORIZED).build()
                            );
                        }
                    }
                } catch (NoResultException e) {
                    // Si el artículo no existe
                    requestCtx.abortWith(
                        Response.status(Response.Status.NOT_FOUND).build()
                    );
                } catch (Exception e) {
                    // Manejo genérico de errores
                    requestCtx.abortWith(
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
                    );
                }
            } else {
                requestCtx.abortWith(
                    Response.status(Response.Status.BAD_REQUEST).build()
                );
            }
        }
    }
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response findArticlesById(@PathParam("id") Long id, final @Context HttpServletRequest request) {
        try {
            // Recuperar el nombre de usuario autenticado del contexto
            String authenticatedUser = (String) request.getAttribute("authenticated");

            if (authenticatedUser != null) {
                // Si está autenticado, buscar el artículo
                TypedQuery<Article> query = em.createQuery(
                    "SELECT a FROM Article a WHERE a.id = :id", Article.class);
                Article article = query.setParameter("id", id).getSingleResult();
                return Response.ok(article).build();
            } else {
                // Si no está autenticado, devolver error
                return Response.status(Response.Status.UNAUTHORIZED).entity("Authentication required").build();
            }
        } catch (NoResultException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("Article not found").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error retrieving article").build();
        }
    }
 

}