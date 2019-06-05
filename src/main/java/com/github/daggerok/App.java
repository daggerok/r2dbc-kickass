package com.github.daggerok;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Data
@RequiredArgsConstructor
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MyData {
  @Id
  Long id;
  @NonNull
  String name;
}

interface MyDataRepository extends ReactiveCrudRepository<MyData, Long> {
  @Query(" delete from my_data ")
  Mono<MyData> deleteMyData();

  @Query("select id, name from my_data md where md.name = :name ")
  Flux<MyData> findByName(String name);

  @Query("select id, name from my_data md where md.name like concat('%', :name, '%')")
  Flux<MyData> findByNameLike(String name);
}

@Log4j2
@Configuration
@RequiredArgsConstructor
class R2dbcConfig {

  @Bean
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(
        PostgresqlConnectionConfiguration.builder()
                                         .database("postgres")
                                         .username("postgres")
                                         .password("postgres")
                                         .host("127.0.0.1")
                                         .port(5432)
                                         .build());
  }

  @Bean
  public InitializingBean initializingBean(DatabaseClient databaseClient) {
    return () -> Stream
        .of(" drop table if exists my_data ; ",
            " create table if not exists my_data(id bigserial not null constraint my_data_pkey primary key, name varchar(255) not null) ; ")
        .forEach(it -> databaseClient.execute()
                                     .sql(it)
                                     .fetch()
                                     .rowsUpdated()
                                     .doAfterSuccessOrError((integer, e) -> log.info("executed '{}' statement", it))
                                     .as(StepVerifier::create) // what about production???
                                     .verifyComplete())
        ;
  }
}

@Log4j2
@Service
@RequiredArgsConstructor
class Handlers {

  private final MyDataRepository myDataRepository;

  public Mono<ServerResponse> getAll(ServerRequest request) {
    return ok().body(myDataRepository.findAll(), MyData.class);
  }

  @Transactional
  public Mono<ServerResponse> save(ServerRequest request) {
    return request.bodyToMono(Map.class)
                  .map(map -> map.get("name"))
                  .map(String::valueOf)
                  .map(MyData::new)
                  .flatMap(myData -> ok().body(myDataRepository.save(myData), MyData.class));
  }

  public Mono<ServerResponse> deleteAll(ServerRequest request) {
    return ok().body(myDataRepository.deleteMyData(), MyData.class);
  }
}

@Configuration
class Rest {

  @Bean
  public RouterFunction<ServerResponse> routes(Handlers handlers) {
    return route().DELETE("/**", handlers::deleteAll)
                  .GET("/**", handlers::getAll)
                  .POST("/**", handlers::save)
                  .build();
  }
}

@Slf4j
@SpringBootApplication
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}
