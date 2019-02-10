package cat.nyaa.HamsterEcoHelper.database;


import javax.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "kitrecord")
@Access(AccessType.FIELD)
public class KitRecord {
    @Column(name = "id")
    @Id
    public Long id;
    @Column(name = "kit_name")
    public String kitName = "";
    @Column(name = "player")
    public UUID player;
}