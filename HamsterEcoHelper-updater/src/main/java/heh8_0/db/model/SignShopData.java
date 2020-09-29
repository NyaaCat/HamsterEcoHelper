package heh8_0.db.model;

import java.util.ArrayList;
import java.util.List;

public class SignShopData extends DataModel {
    List<String> lores = new ArrayList<>();

    public SignShopData(){}

    public SignShopData(List<String> lores) {
        this.lores = lores;
    }
}
