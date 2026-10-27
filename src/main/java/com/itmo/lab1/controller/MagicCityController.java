package com.itmo.lab1.controller;

import com.itmo.lab1.dto.BookCreatureTypeDto;
import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.service.MagicCityService;
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
 * Контроллер магических городов (MagicCity). Обрабатывает веб-запросы по пути {@code /magiccities}.
 * <p>
 * Реализует CRUD для городов: список с пагинацией, сортировкой и фильтрами,
 * просмотр деталей, создание, редактирование и удаление (включая AJAX).
 * Город имеет поля: название, тип governing-существа (хоббит/эльф/человек/голлум),
 * флаг столицы и дату основания.
 */
@Controller
@RequestMapping("/magiccities")
public class MagicCityController {

    private final MagicCityService service;

    public MagicCityController(MagicCityService service) {
        this.service = service;
    }

    /**
     * Добавляет в каждую модель список типов существ (хоббит, эльф, человек, голлум),
     * которые могут быть правителями города — для выпадающего списка в форме.
     */
    @ModelAttribute("creatureTypes")
    public List<BookCreatureTypeDto> creatureTypes() {
        return List.of(
                new BookCreatureTypeDto("HOBBIT", "Хоббит"),
                new BookCreatureTypeDto("ELF", "Эльф"),
                new BookCreatureTypeDto("HUMAN", "Человек"),
                new BookCreatureTypeDto("GOLLUM", "Голлум")
        );
    }

    /**
     * Отображает список городов с пагинацией, сортировкой и фильтрацией.
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
        List<MagicCityDto> items = service.findPaged(page, size, sortColumn, sortDir,
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
        return "magiccities/list";
    }

    /**
     * Отображает форму создания нового города.
     * По умолчанию gouverneur выставлен в "Эльф", флаг столицы — false.
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        MagicCityDto city = new MagicCityDto();
        city.setGovernor(new BookCreatureTypeDto("ELF", "Эльф"));
        city.setCapital(false);
        model.addAttribute("city", city);
        return "magiccities/form";
    }

    /**
     * Создаёт новый город из данных формы.
     * {@code @Valid} запускает валидацию, {@code BindingResult} содержит ошибки.
     * При успехе — редирект на список городов с flash-сообщением. При ошибке — форма повторно.
     */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("city") MagicCityDto dto,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("city", dto);
            return "magiccities/form";
        }
        service.create(dto);
        redirectAttributes.addFlashAttribute("success", "Город создан");
        return "redirect:/magiccities";
    }

    /**
     * Отображает форму редактирования города с предзаполненными данными по ID.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("city", service.findById(id));
        return "magiccities/form";
    }

    /**
     * Обновляет город по ID данными из формы. Логика аналогична {@link #create}.
     * После успешного обновления — редирект на страницу деталей города.
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("city") MagicCityDto dto,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("city", dto);
            return "magiccities/form";
        }
        service.update(id, dto);
        redirectAttributes.addFlashAttribute("success", "Город обновлён");
        return "redirect:/magiccities/" + id;
    }

    /**
     * Отображает детальную страницу города по его ID.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("city", service.findById(id));
        return "magiccities/detail";
    }

    /**
     * Удаляет город по ID и перенаправляет на список с сохранением параметров страницы и фильтров.
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
        redirectAttributes.addFlashAttribute("success", "Город удалён");
        return "redirect:/magiccities?page=" + page + "&size=" + size
                + (sort != null ? "&sort=" + sort : "")
                + (dir != null ? "&dir=" + dir : "")
                + (filterColumn != null ? "&filterColumn=" + filterColumn : "")
                + (filterValue != null ? "&filterValue=" + filterValue : "");
    }

    /**
     * AJAX-удаление города по ID. Возвращает JSON-карту вида
     * {@code {success: true/false, message: "..."}} для обновления интерфейса без перезагрузки.
     */
    @PostMapping("/{id}/delete-ajax")
    @ResponseBody
    public Map<String, Object> deleteAjax(@PathVariable Long id) {
        service.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Город удалён");
        return result;
    }
}
