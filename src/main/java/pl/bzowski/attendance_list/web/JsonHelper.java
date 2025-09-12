package pl.bzowski.attendance_list.web;

import jakarta.inject.Singleton;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@Singleton
class JsonHelper {
    private final Jsonb jsonb = JsonbBuilder.create();

    public String toJson(Object obj) {
        return jsonb.toJson(obj);
    }
}
