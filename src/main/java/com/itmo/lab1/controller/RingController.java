package com.itmo.lab1.controller;

import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.service.RingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер колец (Ring). Обрабатывает веб-запросы по пути {@code /rings}.
 * <p>
 * Реализует CRUD для колец: список с пагинацией, сортировкой и фильтрами,
 * просмотр деталей, создание, редактирование и удаление (включая AJAX).
 */
@Controller
@RequestMapping("/rings")
public class RingController {

    private final RingService service;

    public RingController(RingService service) {
        this.service = service;
    }

    /**
     * Отображает список колец с пагинацией, сортировкой и фильтрацией.
     * Параметры: page, size, sort, dir, filterColumn, filterValue.
     * В модель передаются элементы страницы, параметры пагинации и текущие значения фильтров.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") @Min(1) int page,
                       @RequestParam(defaultValue = "10") @Min(1) int size,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String dir,
                       @RequestParam(required = false) String filterColumn,
                       @RequestParam(required = false) String filterValue,
                       Model model) {
        String sortColumn = (sort != null && !sort.isBlank()) ? sort : "id";
        String sortDir = (dir != null && !dir.isBlank()) ? dir : "asc";
        List<RingDto> items = service.findPaged(page, size, sortColumn, sortDir,
                filterColumn, filterValue);
        long total = service.count(filterColumn, filterValue);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("items", items);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("sort", sortColumn);
        model.addAttribute("dir", sortDir);
        model.addAttribute("filterColumn", filterColumn);
        model.addAttribute("filterValue", filterValue);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);
        return "rings/list";
    }

    /**
     * Отображает форму создания нового кольца.
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("ring", new RingDto());
        return "rings/form";
    }

    /**
     * Создаёт новое кольцо из данных формы.
     * {@code @Valid} запускает валидацию, {@code BindingResult} содержит ошибки.
     * При успехе — редирект на список с flash-сообщением, при ошибке — форма повторно.
     */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("ring") RingDto dto,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ring", dto);
            return "rings/form";
        }
        service.create(dto);
        redirectAttributes.addFlashAttribute("success", "Кольцо создано");
        return "redirect:/rings";
    }

    /**
     * Отображает форму редактирования кольца с предзаполненными данными по ID.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("ring", service.findById(id));
        return "rings/form";
    }

    /**
     * Обновляет кольцо по ID данными из формы. Логика аналогична {@link #create}.
     * После успешного обновления — редирект на страницу деталей кольца.
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("ring") RingDto dto,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ring", dto);
            return "rings/form";
        }
        service.update(id, dto);
        redirectAttributes.addFlashAttribute("success", "Кольцо обновлено");
        return "redirect:/rings/" + id;
    }

    /**
     * Отображает детальную страницу кольца по его ID.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("ring", service.findById(id));
        return "rings/detail";
    }

    /**
     * Удаляет кольцо по ID и перенаправляет на список с сохранением параметров страницы и фильтров.
     * Результат передаётся через flash-атрибуты.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") @Min(1) int page,
                         @RequestParam(defaultValue = "10") @Min(1) int size,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false) String dir,
                         @RequestParam(required = false) String filterColumn,
                         @RequestParam(required = false) String filterValue,
                         RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("success", "Кольцо удалено");
        return "redirect:/rings?page=" + page + "&size=" + size
                + (sort != null ? "&sort=" + sort : "")
                + (dir != null ? "&dir=" + dir : "")
                + (filterColumn != null ? "&filterColumn=" + filterColumn : "")
                + (filterValue != null ? "&filterValue=" + filterValue : "");
    }

    /**
     * AJAX-удаление кольца по ID. Возвращает JSON-карту вида
     * {@code {success: true/false, message: "..."}} для обновления интерфейса без перезагрузки.
     */
    @PostMapping("/{id}/delete-ajax")
    @ResponseBody
    public Map<String, Object> deleteAjax(@PathVariable Long id) {
        service.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Кольцо удалено");
        return result;
    }
}
