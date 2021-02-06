package ru.csdm.stats.webapp.converter;

import ru.csdm.stats.common.model.collector.enums.ProjectDatabaseServerTimezone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

@FacesConverter(forClass = ProjectDatabaseServerTimezone.class)
@Named
public class ProjectDatabaseServerTimezoneConverter implements Converter<ProjectDatabaseServerTimezone> {
    @Override
    public String getAsString(FacesContext context, UIComponent component, ProjectDatabaseServerTimezone value) {
        return value.name();
    }

    @Override
    public ProjectDatabaseServerTimezone getAsObject(FacesContext context, UIComponent component, String value) {
        return ProjectDatabaseServerTimezone.valueOf(value);
    }
}