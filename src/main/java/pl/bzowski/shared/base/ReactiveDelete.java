package pl.bzowski.shared.base;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.UUID;
import java.util.function.Function;

public class ReactiveDelete {
    public static <T> Uni<Response> reactiveDelete(UUID id, String method, Function<UUID, Uni<?>> findByIdFunction, String redirectPath) {
        if (!"delete".equalsIgnoreCase(method)) {
            return Uni.createFrom().item(
                    Response.seeOther(UriBuilder.fromPath(redirectPath).build()).build()
            );
        }
        return findByIdFunction.apply(id)
                .flatMap(entity -> {
                    if (entity == null) {
                        return Uni.createFrom().item(
                                Response.seeOther(UriBuilder.fromPath(redirectPath).build()).build()
                        );
                    } else {
                        return ((PanacheEntityBase) entity)
                                .delete()
                                .map(deleted -> Response.seeOther(UriBuilder.fromPath("/web/events").build()).build());
                    }
                });
    }
}
