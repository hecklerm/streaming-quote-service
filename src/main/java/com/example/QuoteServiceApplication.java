package com.example;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@SpringBootApplication
public class QuoteServiceApplication {

    @Autowired
    @Bean
    CommandLineRunner commandLineRunner(QuoteRepository quoteRepository) {
        return args -> {
            quoteRepository.save(new Quote("Test quote 1.", "Me"));
            quoteRepository.save(new Quote("Test quote 2.", "Myself"));
            quoteRepository.save(new Quote("Test quote 3.", "I"));

            quoteRepository.findAll().forEach(System.out::println);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(QuoteServiceApplication.class, args);
    }
}

@RepositoryRestResource
interface QuoteRepository extends CrudRepository<Quote, Long> {
    @Query("select q from Quote q order by RAND()")
    List<Quote> getQuotesRand();
}

@RestController
class QuoteController {
    @Autowired
    QuoteRepository quoteRepository;

    @RequestMapping("/random")
    Quote getRandomQuote() {
        return quoteRepository.getQuotesRand().get(0);
    }
}

interface ReactiveRepository<T> {
    Mono<Void> insert(Publisher<T> elements);

    Flux<T> list();

    Mono<T> findById(String id);
}

@Repository
class H2Repository implements ReactiveRepository<Quote> {
    @Autowired
    QuoteRepository quoteRepository;

    @Override
    public Mono<Void> insert(Publisher<Quote> elements) {
        return null;
    }

    @Override
    public Flux<Quote> list() {
        Iterable<Quote> quotes = quoteRepository.findAll();
        return Flux.fromIterable(quotes);
    }

    @Override
    public Mono<Quote> findById(String id) {
        return null;
    }
}

@RestController
class TestReactor {
    @Autowired
    H2Repository h2repo;

    @RequestMapping("/list")
    public Flux<Quote> getList() {
        return h2repo.list();
    }
}

@Entity
class Quote {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
    private String source;

    public Quote() {
    }

    public Quote(String text, String source) {

        this.text = text;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}