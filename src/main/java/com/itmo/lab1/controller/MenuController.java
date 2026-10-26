package com.itmo.lab1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер главного меню. Обрабатывает запросы к корню приложения ({@code /} и {@code /menu}).
 * <p>
 * Просто возвращает главную страницу {@code menu}, с которой пользователь может перейти
 * к спискам существ, городов, колец и специальным операциям.
 */
@Controller
@RequestMapping
public class MenuController {

    /**
     * Отображает главную страницу приложения (меню навигации).
     */
    @GetMapping({"/", "/menu"})
    public String menu() {
        return "menu";
    }
}
