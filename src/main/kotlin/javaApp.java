import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class javaApp {
    public static void main(String[] args) {
        System.out.println("Deploy started...");

        DeploymentOptions options = new DeploymentOptions();
        options.setHa(true);

        Vertx
                .vertx()
                .deployVerticle(new CrudVerticle(),
                        options,
                        res -> {
                            if (res.succeeded()) {
                                System.out.print("Deployment id is:" + res.result());
                            } else {
                                System.out.print("Deployment failed!");
                            }
                        }
                );
    }
}
