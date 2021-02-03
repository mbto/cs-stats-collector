package ru.csdm.stats.webapp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import static ru.csdm.stats.common.Constants.PROJECT_DATABASE_SERVER_TIMEZONES;

@Named
@Dependent
@Slf4j
public class DependentUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private static final SelectItem[] AVAILABLE_TIME_ZONES = Arrays.stream(PROJECT_DATABASE_SERVER_TIMEZONES)
            .map(timezone -> new SelectItem(timezone, timezone.getLiteral()))
            .toArray(SelectItem[]::new);

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

    public void sendRedirect(String url) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext exCtx = fc.getExternalContext();
        try {
            exCtx.redirect(url);
        } catch (IOException e) {
            String msg = "Failed redirect to '" + url + "'";
            log.warn(msg, e);
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg, ""));
        } finally {
            fc.responseComplete();
        }
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