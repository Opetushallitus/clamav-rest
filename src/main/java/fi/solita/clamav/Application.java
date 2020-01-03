package fi.solita.clamav;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.MultipartConfigElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.servlets.QoSFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.web.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
/**
 * Simple Spring Boot application which acts as a REST endpoint for
 * clamd server.
 */
public class Application {

  private Log log = LogFactory.getLog(Application.class);

  @Value("${clamd.maxfilesize}")
  private String maxfilesize;

  @Value("${clamd.maxrequestsize}")
  private String maxrequestsize;

  @Value("${clamd.qos.maxrequests}")
  private int maxrequests;

  @Value("${clamd.qos.waitms}")
  private int waitMs;

  @Value("${clamd.qos.suspendms}")
  private int suspendMs;

  @Bean
  MultipartConfigElement multipartConfigElement() {
    log.info(String.format("Configuring multipart support, maxFileSize=%s, maxRequestSize=%s",
            maxfilesize, maxrequestsize));
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(maxfilesize);
    factory.setMaxRequestSize(maxrequestsize);
    return factory.createMultipartConfig();
  }

  @Bean
  @ConditionalOnExpression("${clamd.qos.maxrequests} > 0")
  FilterRegistrationBean qosFitlerRegistrationBean() {
    log.info(String.format("Configuring QoSFilter, maxRequests=%d, waitMs=%d, suspendMs=%d",
            maxrequests, waitMs, suspendMs));
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(new QoSFilter());
    registration.addInitParameter("maxRequests", Integer.toString(maxrequests));
    registration.addInitParameter("waitMs", Integer.toString(waitMs));
    registration.addInitParameter("suspendMs", Integer.toString(suspendMs));
    registration.addUrlPatterns("/scan", "/scanReply");
    registration.setOrder(OrderedHiddenHttpMethodFilter.DEFAULT_ORDER - 1);
    return registration;
  }

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Application.class);
    Map<String, Object> defaults = new HashMap<String, Object>();
    defaults.put("clamd.host", "192.168.50.72");
    defaults.put("clamd.port", 3310);
    defaults.put("clamd.timeout", 500);
    defaults.put("clamd.maxfilesize", "20000KB");
    defaults.put("clamd.maxrequestsize", "20000KB");
    defaults.put("clamd.qos.maxrequests", -1);
    defaults.put("clamd.qos.waitms", 50);
    defaults.put("clamd.qos.suspendms", -1);
    app.setDefaultProperties(defaults);
    app.run(args);
  }
}
