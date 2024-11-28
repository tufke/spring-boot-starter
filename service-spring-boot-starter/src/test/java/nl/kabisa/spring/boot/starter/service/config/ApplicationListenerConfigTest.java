package nl.kabisa.spring.boot.starter.service.config;

import nl.kabisa.spring.boot.starter.service.config.AppStartupListener;
import nl.kabisa.spring.boot.starter.service.config.ApplicationListenerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Test to check applicationContext that will have bean of {@link AppStartupListener} initialized.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ApplicationListenerConfig.class})
public class ApplicationListenerConfigTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testApplicationContext() {
        ApplicationListener listener = applicationContext.getBean(ApplicationListener.class);
        assertNotNull(listener);
    }

}