/*
 * This file is generated by jOOQ.
 */
package ru.csdm.stats.common.model.tables.pojos;


import org.jooq.types.UInteger;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * Who to share API access to endpoints /stats/ *
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ApiUser implements Serializable {

    private static final long serialVersionUID = 1489609932;

    private UInteger      id;
    private Boolean       active;
    private String        username;
    private String        password;
    private Boolean       manage;
    private Boolean       view;
    private LocalDateTime regDatetime;

    public ApiUser() {}

    public ApiUser(ApiUser value) {
        this.id = value.id;
        this.active = value.active;
        this.username = value.username;
        this.password = value.password;
        this.manage = value.manage;
        this.view = value.view;
        this.regDatetime = value.regDatetime;
    }

    public ApiUser(
        UInteger      id,
        Boolean       active,
        String        username,
        String        password,
        Boolean       manage,
        Boolean       view,
        LocalDateTime regDatetime
    ) {
        this.id = id;
        this.active = active;
        this.username = username;
        this.password = password;
        this.manage = manage;
        this.view = view;
        this.regDatetime = regDatetime;
    }

    public UInteger getId() {
        return this.id;
    }

    public void setId(UInteger id) {
        this.id = id;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @NotNull
    @Size(max = 31)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotNull
    @Size(max = 60)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getManage() {
        return this.manage;
    }

    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    public Boolean getView() {
        return this.view;
    }

    public void setView(Boolean view) {
        this.view = view;
    }

    public LocalDateTime getRegDatetime() {
        return this.regDatetime;
    }

    public void setRegDatetime(LocalDateTime regDatetime) {
        this.regDatetime = regDatetime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ApiUser other = (ApiUser) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (active == null) {
            if (other.active != null)
                return false;
        }
        else if (!active.equals(other.active))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        }
        else if (!username.equals(other.username))
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        }
        else if (!password.equals(other.password))
            return false;
        if (manage == null) {
            if (other.manage != null)
                return false;
        }
        else if (!manage.equals(other.manage))
            return false;
        if (view == null) {
            if (other.view != null)
                return false;
        }
        else if (!view.equals(other.view))
            return false;
        if (regDatetime == null) {
            if (other.regDatetime != null)
                return false;
        }
        else if (!regDatetime.equals(other.regDatetime))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.active == null) ? 0 : this.active.hashCode());
        result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
        result = prime * result + ((this.password == null) ? 0 : this.password.hashCode());
        result = prime * result + ((this.manage == null) ? 0 : this.manage.hashCode());
        result = prime * result + ((this.view == null) ? 0 : this.view.hashCode());
        result = prime * result + ((this.regDatetime == null) ? 0 : this.regDatetime.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApiUser (");

        sb.append(id);
        sb.append(", ").append(active);
        sb.append(", ").append(username);
        sb.append(", ").append(password);
        sb.append(", ").append(manage);
        sb.append(", ").append(view);
        sb.append(", ").append(regDatetime);

        sb.append(")");
        return sb.toString();
    }
}
