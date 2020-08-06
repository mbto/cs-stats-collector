/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.types.UInteger;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Rank implements Serializable {

    private static final long serialVersionUID = 1182370984;

    private UInteger id;
    private UInteger level;
    private String   name;

    public Rank() {}

    public Rank(Rank value) {
        this.id = value.id;
        this.level = value.level;
        this.name = value.name;
    }

    public Rank(
        UInteger id,
        UInteger level,
        String   name
    ) {
        this.id = id;
        this.level = level;
        this.name = name;
    }

    public UInteger getId() {
        return this.id;
    }

    public void setId(UInteger id) {
        this.id = id;
    }

    @NotNull
    public UInteger getLevel() {
        return this.level;
    }

    public void setLevel(UInteger level) {
        this.level = level;
    }

    @NotNull
    @Size(max = 60)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Rank other = (Rank) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (level == null) {
            if (other.level != null)
                return false;
        }
        else if (!level.equals(other.level))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.level == null) ? 0 : this.level.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Rank (");

        sb.append(id);
        sb.append(", ").append(level);
        sb.append(", ").append(name);

        sb.append(")");
        return sb.toString();
    }
}
