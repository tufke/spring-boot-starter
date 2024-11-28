package nl.kabisa.spring.boot.starter.database;

import nl.kabisa.spring.boot.starter.database.entity.QuoteEntity;
import nl.kabisa.spring.boot.starter.database.repositories.QuoteRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
		"spring.active.profiles: database"
})
class ApplicationTest {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private QuoteRepository quoteRepository;

	@Test
	void contextLoads() {
		assertNotNull(applicationContext);
		DataSource dataSource = applicationContext.getBean(DataSource.class);
		assertNotNull(dataSource);
		assertNotNull(applicationContext.getBean(Flyway.class));
	}

	@Test
	void testRepository() {
		QuoteEntity quote = new QuoteEntity();
		quote.setText("to quote");
		quote.setAuthor("Mark");

		quote = quoteRepository.save(quote);

		assertEquals("to quote", quote.getText());
		assertEquals("Mark", quote.getAuthor());
	}
}
