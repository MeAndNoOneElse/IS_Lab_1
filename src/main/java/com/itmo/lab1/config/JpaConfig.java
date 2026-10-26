package com.itmo.lab1.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация уровня persistence на базе EclipseLink (ORM-провайдер, требуемый ТЗ).
 * <p>
 * DataSource и EntityManagerFactory настраиваются явно, чтобы гарантировать
 * использование EclipseLink вместо Hibernate (Spring Boot по умолчанию подтягивает
 * Hibernate, а в {@link Lab1Application} его автоконфигурация отключена).
 * <p>
 * Настройки подключения берутся из {@code application.properties} через {@link Value} —
 * это единственный источник истины для URL/логина. Пароль не хранится в коде, а берётся
 * из переменной окружения
 */
@Configuration
@EnableTransactionManagement // включает поддержку @Transactional в сервисах
public class JpaConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * Создаёт DataSource — объект, через который приложение получает соединения с PostgreSQL.
     * Используется простой {@link DriverManagerDataSource} (не пул connection)
     * Параметры берутся из полей выше, которые заполняются Spring из application.properties.
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * Создаёт фабрику EntityManagerFactory для EclipseLink.
     * <ul>
     *   <li>{@link #dataSource()} — подключение к БД, которое будет использоваться для запросов;</li>
     *   <li>{@link PersistenceProvider} — явно EclipseLink, чтобы Spring не подставил Hibernate;</li>
     *   <li>{@code lab1PU} — имя persistence unit (дублирует persistence.xml);</li>
     *   <li>{@code packagesToScan} — пакет с сущностями {@code com.itmo.lab1.entity}, поэтому
     *       аннотации {@code @Entity} не нужно перечислять в persistence.xml вручную;</li>
     *   <li>{@code ddl-generation=none} — схему создаёт SQL-скрипт {@code init_schema.sql}, а не JPA;</li>
     *   <li>{@code logging.level=FINE} — подробное логирование SQL для отладки.</li>
     * </ul>
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPersistenceProvider(new PersistenceProvider());
        emf.setPersistenceUnitName("lab1PU");
        emf.setPackagesToScan("com.itmo.lab1.entity");

        Map<String, Object> props = new HashMap<>();
        props.put("eclipselink.target-database", "org.eclipse.persistence.platform.database.PostgreSQLPlatform");
        props.put("eclipselink.ddl-generation", "none");
        props.put("eclipselink.logging.level", "FINE");
        emf.setJpaPropertyMap(props);

        return emf;
    }

    /**
     * Создаёт менеджер транзакций JPA. Связывает EntityManagerFactory с механизмом
     * транзакций Spring, чтобы аннотация {@code @Transactional} в сервисах открывала
     * и коммитила/откатывала транзакцию автоматически.
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }
}
