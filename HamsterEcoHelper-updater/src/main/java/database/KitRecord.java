package database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;

import java.util.UUID;

@Table("kitrecord")
public class KitRecord {
    @Column(primary = true)
    public Long id;
    @Column(name = "kit_name")
    public String kitName = "";
    @Column
    public UUID player;
}