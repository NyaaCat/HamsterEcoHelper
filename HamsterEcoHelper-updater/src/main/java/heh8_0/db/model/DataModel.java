package heh8_0.db.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DataModel {
    @SerializedName("version")
    int version = 0;
    private static final Gson gson = new Gson();

    public static Gson getGson(){
        return gson;
    }

    public String toJson(){
        return gson.toJson(this);
    }
}
