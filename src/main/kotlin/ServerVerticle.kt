import io.vertx.core.AbstractVerticle

class ServerVerticle : AbstractVerticle() {
    override fun start() {
        val server = vertx.createHttpServer()
        server
                .requestHandler({
                    it.response().end("Hello from "
                            + Thread.currentThread().getName())
                })
                .listen(0, { res ->
                    if (res.succeeded()) {
                        println("Server is now listening on actual port: ${server.actualPort()}")
                    } else {
                        println("Failed to bind!")
                    }
                })
    }
}