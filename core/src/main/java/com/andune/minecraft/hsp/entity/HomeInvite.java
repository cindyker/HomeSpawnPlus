/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
/**
 *
 */
package com.andune.minecraft.hsp.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotNull;

import javax.persistence.*;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author andune
 */
@Entity()
@Table(name = "hsp_homeinvite",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"home_id", "invited_player"})
        }
)
public class HomeInvite implements BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Column(name = "home_id")
    private HomeImpl home;

    @NotNull
    @Length(max = 32)
    @Column(name = "invited_player")
    private String invitedPlayer;

    /* If this invite is temporary, the expiration time is recorded here. If the
     * invite is permanent, this will be null.
     * 
     */
    private Date expires;

    @Version
    private Timestamp lastModified;

    @CreatedTimestamp
    private Timestamp dateCreated;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HomeImpl getHome() {
        return home;
    }

    public void setHome(Home home) {
        this.home = (HomeImpl) home;
    }

    public String getInvitedPlayer() {
        return invitedPlayer;
    }

    public void setInvitedPlayer(String invitedPlayer) {
        this.invitedPlayer = invitedPlayer;
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

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
