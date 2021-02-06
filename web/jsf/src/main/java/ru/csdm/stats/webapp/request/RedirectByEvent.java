package ru.csdm.stats.webapp.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.csdm.stats.webapp.DependentUtil;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@RequestScoped
@Named
@Slf4j
public class RedirectByEvent {
    @Autowired
    private DependentUtil util;

    public void onRowSelect(String formId, String dataTableId, String viewName, String paramName) {
        util.sendRedirect(formId, dataTableId, viewName, paramName);
    }
}