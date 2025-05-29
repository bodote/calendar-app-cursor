package de.bas.bodo.woodle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public UrlBasedViewResolver viewResolver() {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setViewClass(RedirectView.class);
        resolver.setRedirectHttp10Compatible(false);
        return resolver;
    }
}