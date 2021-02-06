package ru.csdm.stats.webapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DefaultView implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/editInstance").setViewName("forward:/editInstance.xhtml");
        registry.addViewController("/editProject").setViewName("forward:/editProject.xhtml");
        registry.addViewController("/").setViewName("forward:/index.xhtml");
        registry.addViewController("/instances").setViewName("forward:/instances.xhtml");
        registry.addViewController("/knownServers").setViewName("forward:/knownServers.xhtml");
        registry.addViewController("/managers").setViewName("forward:/managers.xhtml");
        registry.addViewController("/newInstance").setViewName("forward:/newInstance.xhtml");
        registry.addViewController("/newProject").setViewName("forward:/newProject.xhtml");
        registry.addViewController("/projects").setViewName("forward:/projects.xhtml");
    }
}