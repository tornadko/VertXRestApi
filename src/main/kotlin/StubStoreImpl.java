import io.vertx.core.json.JsonObject;
import rx.Completable;
import rx.Observable;
import rx.Single;

public class StubStoreImpl implements Store {
    @Override
    public Single<JsonObject> create(JsonObject item) {
        return Single.just(
                new JsonObject()
                        .put("description", "User '" + item.getString("name") + "' has been created"));
    }

    @Override
    public Observable<JsonObject> readAll() {
        return Observable.from(new String[]{"User1", "User2", "User3"})
                .map(item ->
                        new JsonObject()
                                .put("name", item));
    }

    @Override
    public Single<JsonObject> read(long id) {
        return Single.just(
                new JsonObject()
                        .put("name", "User" + id));
    }

    @Override
    public Completable update(long id, JsonObject item) {
        return Completable.complete();
    }

    @Override
    public Completable delete(long id) {
        return Completable.complete();
    }
}
