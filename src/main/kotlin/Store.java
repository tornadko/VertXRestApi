import io.vertx.core.json.JsonObject;
import rx.Completable;
import rx.Observable;
import rx.Single;

public interface Store {

    Single<JsonObject> create(JsonObject item);

    Observable<JsonObject> readAll();

    Single<JsonObject> read(long id);

    Completable update(long id, JsonObject item);

    Completable delete(long id);
}