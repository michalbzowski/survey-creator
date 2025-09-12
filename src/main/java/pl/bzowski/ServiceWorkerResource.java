package pl.bzowski;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.InputStream;

@Path("/js")
public class ServiceWorkerResource {

    @GET
    @Path("/service-worker.js")
    @Produces("application/javascript")
    public Response getServiceWorker(@Context UriInfo uriInfo) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
        cacheControl.setProxyRevalidate(true);
        cacheControl.setMaxAge(0);

        InputStream swStream = getClass().getResourceAsStream("/META-INF/resources/js/service-worker.js");
        if (swStream == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(swStream)
                .cacheControl(cacheControl)
                .build();
    }
}
