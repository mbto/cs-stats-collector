package ru.csdm.stats.webapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DefaultView implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.xhtml");
        registry.addViewController("/index").setViewName("forward:/index.xhtml");
        registry.addViewController("/projects").setViewName("forward:/projects.xhtml");
        registry.addViewController("/editProject").setViewName("forward:/editProject.xhtml");
        registry.addViewController("/knownServers").setViewName("forward:/knownServers.xhtml");
        registry.addViewController("/managers").setViewName("forward:/managers.xhtml");
    }
}