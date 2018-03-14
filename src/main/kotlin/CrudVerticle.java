import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

import java.util.NoSuchElementException;

public class CrudVerticle extends AbstractVerticle {

    private Store store;

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/api/users/:id").handler(this::validateId);

        router.get("/api/users").handler(this::readAll);
        router.post("/api/users").handler(this::create);
        router.get("/api/users/:id").handler(this::read);
        router.put("/api/users/:id").handler(this::update);
        router.delete("/api/users/:id").handler(this::delete);

        store = new StubStoreImpl();

        HttpServer server = vertx.createHttpServer();
        server
                .requestHandler(router::accept)
                .listen(0, res -> {
                    if (res.succeeded()) {
                        System.out.println("Server is now listening on actual port:" + server.actualPort());
                    } else {
                        System.out.println("Failed to bind!");
                    }
                });
    }

    private void validateId(RoutingContext context) {
        try {
            context.put("userId", Long.parseLong(context.pathParam("id")));
            context.next();
        } catch (NumberFormatException e) {
            error(context, 400, "invalid id: " + e.getCause());
        }
    }

    private void readAll(RoutingContext context) {
        HttpServerResponse response =
                context
                        .response()
                        .putHeader("Content-Type", "application/json");
        JsonArray res = new JsonArray();
        store.readAll()
                .subscribe(
                        res::add,
                        err -> error(context, 415, err),
                        () -> response.end(res.encodePrettily())
                );
    }

    private void read(RoutingContext context) {
        HttpServerResponse response = context.response()
                .putHeader("Content-Type", "application/json");

        store.read(context.get("userId"))
                .subscribe(
                        json -> response.end(json.encodePrettily()),
                        err -> {
                            if (err instanceof NoSuchElementException) {
                                error(context, 404, err);
                            } else if (err instanceof IllegalArgumentException) {
                                error(context, 415, err);
                            } else {
                                error(context, 500, err);
                            }
                        }
                );
    }

    private void create(RoutingContext context) {
        JsonObject item;
        try {
            item = context.getBodyAsJson();
        } catch (RuntimeException e) {
            error(context, 415, "invalid payload");
            return;
        }

        if (item == null) {
            error(context, 415, "invalid payload");
            return;
        }

        store.create(item)
                .subscribe(
                        json ->
                                context.response()
                                        .putHeader("Location", "/api/users/" + json.getLong("id"))
                                        .putHeader("Content-Type", "application/json")
                                        .setStatusCode(201)
                                        .end(json.encodePrettily()),
                        err -> writeError(context, err)
                );
    }

    private void update(RoutingContext context) {
        JsonObject item;
        try {
            item = context.getBodyAsJson();
        } catch (RuntimeException e) {
            error(context, 415, "invalid payload");
            return;
        }

        if (item == null) {
            error(context, 415, "invalid payload");
            return;
        }

        store.update(context.get("userId"), item)
                .subscribe(
                        () ->
                                context.response()
                                        .putHeader("Content-Type", "application/json")
                                        .setStatusCode(200)
                                        .end(item.put("id", context.<Long>get("userId")).encodePrettily()),
                        err -> writeError(context, err)
                );
    }

    private void writeError(RoutingContext context, Throwable err) {
        if (err instanceof NoSuchElementException) {
            error(context, 404, err);
        } else if (err instanceof IllegalArgumentException) {
            error(context, 422, err);
        } else {
            error(context, 409, err);
        }
    }

    private void delete(RoutingContext context) {
        store.delete(context.get("userId"))
                .subscribe(
                        () ->
                                context.response()
                                        .setStatusCode(204)
                                        .end(),
                        err -> {
                            if (err instanceof NoSuchElementException) {
                                error(context, 404, err);
                            } else {
                                error(context, 415, err);
                            }
                        }
                );
    }

    private void error(RoutingContext context, int status, String cause) {
        JsonObject error = new JsonObject()
                .put("error", cause)
                .put("code", status)
                .put("path", context.request().path());
        context.response()
                .putHeader("Content-Type", "application/json")
                .setStatusCode(status)
                .end(error.encodePrettily());
    }

    private void error(RoutingContext context, int status, Throwable cause) {
        error(context, status, cause.getMessage());
    }
}
