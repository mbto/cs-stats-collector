package ru.csdm.stats.webapp.validator;

import lombok.extern.slf4j.Slf4j;
import ru.csdm.stats.common.model.collector.tables.pojos.KnownServer;
import ru.csdm.stats.webapp.Row;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.List;

import static ru.csdm.stats.common.Constants.IPADDRESS_PORT_PATTERN;

@FacesValidator(value = "checkUniqueIpPortValidator")
@Slf4j
public class CheckUniqueIpPortValidator implements Validator<String> {
    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if(!IPADDRESS_PORT_PATTERN.matcher(value).matches())
            throw makeValidatorException(value, "");

        int rowIndexVar = (int) component.getAttributes().get("rowIndexVar");
        List<Row<KnownServer>> currentInstanceRows = (List<Row<KnownServer>>) component.getAttributes().get("currentInstanceRows");

        if(log.isDebugEnabled())
            log.debug("\nrowIndexVar=" + rowIndexVar + ", currentInstanceRows.size=" + currentInstanceRows.size());

        for (int i = 0; i < currentInstanceRows.size(); i++) {
            if(i == rowIndexVar)
                continue;

            Row<KnownServer> currentInstanceRow = currentInstanceRows.get(i);
            KnownServer knownServer = currentInstanceRow.getPojo();

            if(!value.equals(knownServer.getIpport()))
                continue;

            throw makeValidatorException(value, "Already exists at known server "
                    + knownServer.getIpport()
                    + " (" + knownServer.getName() + ")");
        }
    }

    private ValidatorException makeValidatorException(String value, String detail) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Failed validation ip:port '" + value + "'",
                detail));
    }

}