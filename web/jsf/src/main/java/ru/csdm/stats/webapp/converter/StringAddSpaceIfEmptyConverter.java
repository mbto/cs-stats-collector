package ru.csdm.stats.webapp.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import java.util.Objects;

@FacesConverter(value = "stringAddSpaceIfEmptyConverter")
@Named
public class StringAddSpaceIfEmptyConverter implements Converter<String> {
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if(Objects.isNull(value))
            return " ";

        value = value.trim();
        return value.isEmpty() ? " " : value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, String value) {
        return Objects.isNull(value) ? "" : value.trim();
    }
}