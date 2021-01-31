package ru.csdm.stats.webapp.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;

@FacesValidator(value = "ipPortValidator")
public class IpPortValidator implements Validator<String> {
    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if(!IPADDRESS_PORT_PATTERN.matcher(value).matches())
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN, "Failed validation ip:port '" + value + "'", ""));
    }
}