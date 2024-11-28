package nl.kabisa.spring.boot.starter.service.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test to check applicationContext that will NOT have bean of {@link AppStartupListener} initialized.
 * Bean is excluded from initialization with property service.starter.application.listener=false
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationListenerConfig.class)
@TestPropertySource(properties = {"service.starter.application.listener.enabled=false"})
public class ApplicationListenerConfigExcludedTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testNoListener() {
        applicationContext.getBean(ApplicationListener.class);
    }

}
