package ru.csdm.stats.webapp;

import ru.csdm.stats.common.Constants;
import ru.csdm.stats.common.model.collector.enums.ProjectDatabaseServerTimezone;

import javax.enterprise.context.Dependent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

@Named
@Dependent
public class DependentUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    public String getAbsoluteContextPath(boolean addApplicationPath) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        StringBuilder buffer = new StringBuilder();
        buffer.append(request.getScheme());
        buffer.append("://");
        buffer.append(request.getServerName());

        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            buffer.append(":").append(port);
        }

        if (addApplicationPath) {
            String appCtxPath = externalContext.getApplicationContextPath();

            if (!Objects.equals(appCtxPath, "")) {
                buffer.append(appCtxPath);
            }
        } else
            buffer.append("/");

        return buffer.toString();
    }

    public String declension(long n, String o1, String o2, String o3) {
        n = Math.abs(n) % 100;

        if (n > 10 && n < 20)
            return o3;

        n = n % 10;

        if (n > 1 && n < 5)
            return o2;

        return n == 1 ? o1 : o3;
    }
}