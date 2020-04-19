package ru.csdm.stats.web.error;

import org.springframework.boot.autoconfigure.web.ErrorProperties;

import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.ALWAYS;

class DefaultErrorProperties extends ErrorProperties {
    DefaultErrorProperties() {
        setIncludeException(true);
        setIncludeStacktrace(ALWAYS);
    }
}
