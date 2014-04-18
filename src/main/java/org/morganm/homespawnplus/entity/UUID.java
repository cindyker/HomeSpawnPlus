package org.morganm.homespawnplus.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Class to store UUID/name mappings. This entity always represents the latest
 * known UUID/name pairing and both UUID and name are guaranteed to be unique.
 *
 * @author andune
 */
@Entity()
@Table(name = "hsp_uuid",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"uuid"}),
                @UniqueConstraint(columnNames = {"name"})
        }
)
public class UUID {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    /*
     * We store UUIDString in the database so it's easily text readable by an admin, as
     * opposed to storing the binary uuid object. It's 20 more bytes, but even on a
     * server with 10,000 players, that's only 160k in extra DB storage. Well worth
     * the trade-off for admin readability.
     */
    @Length(max = 36)
    @Column(name = "uuid")
    @NotNull
    private String UUIDString;

    @Length(max = 32)
    @NotNull
    private String name;

    @javax.persistence.Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    @Transient
    private transient java.util.UUID uuid;

    public UUID() {
    }

    public UUID(java.util.UUID uuid) {
        this.uuid = uuid;
        this.UUIDString = uuid.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUUIDString(String UUIDString) {
        this.UUIDString = UUIDString;
        uuid = java.util.UUID.fromString(UUIDString);
    }

    public String getUUIDString() {
        return UUIDString;
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }
}
