package nl.kabisa.spring.boot.starter.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Lombok issues on JPA entities:
 * https://dzone.com/articles/lombok-and-jpa-what-may-go-wrong
 *
 * Lombok @ToString may be used but @ToString.Exclude must be set on properties with (default) FetchType LAZY
 *          to avoid that they do get loaded by the toString method.
 * Lombok @Data must not be used because it will generate a Hashcode method which is not reliable since
 *          JPA entities are mutable, it can also cause LazyInitializationException outside a transaction.
 * Lombok @NoArgsConstructor must be provided when using @Builder or @AllArgsConstructor
 * Lombok @EqualsAndHashCode must not be used, instead use the default java Object implementations.
 *
 */
@Entity(name = "Quote")
@Table(name = "QUOTE")
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "quote_generator")
    @SequenceGenerator(name="quote_generator", sequenceName = "quote_sequence", allocationSize = 50)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "text", length=255, nullable=false)
    private String text;

    @Column(name = "author", length=255, nullable=false)
    private String author;

    @Builder.Default
    @Column(name = "creation_date", columnDefinition = "DATE", nullable = false)
    private LocalDate creationDate = LocalDate.now();

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;



}
