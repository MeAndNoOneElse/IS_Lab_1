package com.itmo.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Точка входа приложения лабораторной работы №1 (Информационные системы).
 * <p>
 * Используется Spring MVC + EclipseLink (JPA) + PostgreSQL.
 * Hibernate-автоконфигурация отключена, так как ORM-провайдером является EclipseLink.
 */
@SpringBootApplication(
        exclude = {
                HibernateJpaAutoConfiguration.class,
                DataSourceAutoConfiguration.class
        }
)
public class Lab1Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab1Application.class, args);
    }
}
