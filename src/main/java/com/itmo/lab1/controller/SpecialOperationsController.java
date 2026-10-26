package com.itmo.lab1.controller;

import com.itmo.lab1.dto.SpecialOpResultDto;
import com.itmo.lab1.service.SpecialOperationsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер специальных операций. Обрабатывает веб-запросы по пути {@code /special}.
 * <p>
 * Выполняет специальные запросы к базе, которые не вписываются в обычный CRUD:
 * <ul>
 *   <li>сумма уровней атаки всех существ;</li>
 *   <li>поиск существ по подстроке имени;</li>
 *   <li>уникальные уровни атаки;</li>
 *   <li>обмен кольцами между двумя существами;</li>
 *   <li>уничтожение городов эльфов.</li>
 * </ul>
 * Все результаты возвращаются на одну страницу {@code special/operations} через {@code SpecialOpResultDto}.
 */
@Controller
@RequestMapping("/special")
public class SpecialOperationsController {

    private final SpecialOperationsService service;

    public SpecialOperationsController(SpecialOperationsService service) {
        this.service = service;
    }

    /**
     * Отображает страницу специальных операций (пустая форма без результата).
     */
    @GetMapping
    public String operations(Model model) {
        return "special/operations";
    }

    /**
     * Вычисляет сумму уровней атаки всех существ и отображает результат на странице операций.
     */
    @PostMapping("/sum")
    public String sumAttackLevel(Model model) {
        SpecialOpResultDto result = service.sumAttackLevel();
        model.addAttribute("result", result);
        return "special/operations";
    }

    /**
     * Ищет существ, чье имя содержит заданную подстроку (без учета регистра).
     * Подстрока передаётся параметром {@code substring}; результат и сама подстрока попадают в модель.
     */
    @PostMapping("/name")
    public String findByName(@RequestParam String substring, Model model) {
        SpecialOpResultDto result = service.findByNameSubstring(substring);
        model.addAttribute("result", result);
        model.addAttribute("substring", substring);
        return "special/operations";
    }

    /**
     * Возвращает список уникальных уровней атаки всех существ и отображает на странице операций.
     */
    @PostMapping("/unique-levels")
    public String uniqueLevels(Model model) {
        SpecialOpResultDto result = service.uniqueAttackLevels();
        model.addAttribute("result", result);
        return "special/operations";
    }

    /**
     * Меняет кольца местами между двумя существами по их ID ({@code id1} и {@code id2}).
     * Результат операции отображается на странице специальных операций.
     */
    @PostMapping("/swap-rings")
    public String swapRings(@RequestParam Long id1, @RequestParam Long id2, Model model) {
        SpecialOpResultDto result = service.swapRings(id1, id2);
        model.addAttribute("result", result);
        return "special/operations";
    }

    /**
     * Удаляет все города, правителями которых являются эльфы.
     * Результат операции отображается на странице специальных операций.
     */
    @PostMapping("/destroy-elf-cities")
    public String destroyElfCities(Model model) {
        SpecialOpResultDto result = service.destroyElfCities();
        model.addAttribute("result", result);
        return "special/operations";
    }
}
