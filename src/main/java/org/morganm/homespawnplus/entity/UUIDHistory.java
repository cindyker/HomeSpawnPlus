package org.morganm.homespawnplus.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.sql.Timestamp;

/**
 * UUID history, because, why not? I track name/UUID to make it easy for admins to
 * look through DB/YAML files to find data, so I have to track UUID changes if
 * they happen. Might as well log them for admins to see as well.
 * <p/>
 * This entity should be treated as insert-only and entity data never changed
 * once it is created. Unfortunately JPA doesn't make it easy to enforce
 * immutability with the normal Java OOP tools, so this is a contract by
 * convention.
 * <p/>
 * (this probably should be provided by a common utility, perhaps Bukkit itself or
 * a common utility similar to Vault. But seems to be little interest from the
 * Bukkit community for this, so doing it myself to scratch my own itch.)
 *
 * @author andune
 */
@Entity()
@Table(name = "hsp_uuid_history",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"uuid, player_name"})
        }
)
public class UUIDHistory {
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
    @NotNull
    private String UUIDString;

    @Length(max = 32)
    @NotNull
    private String playerName;

    @javax.persistence.Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    private transient java.util.UUID uuid;

    public UUIDHistory() {
    }

    public UUIDHistory(java.util.UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.UUIDString = uuid.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUUIDString() {
        return UUIDString;
    }

    public void setUUIDString(String UUIDString) {
        this.UUIDString = UUIDString;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
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
