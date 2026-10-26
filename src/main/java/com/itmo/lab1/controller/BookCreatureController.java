package com.itmo.lab1.controller;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.BookCreatureTypeDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.dto.RingDto;
import com.itmo.lab1.exception.EntityNotFoundException;
import com.itmo.lab1.service.BookCreatureService;
import com.itmo.lab1.service.MagicCityService;
import com.itmo.lab1.service.RingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер существ (BookCreature). Обрабатывает все веб-запросы по пути {@code /bookcreatures}.
 * <p>
 * Реализует полный CRUD для существ: список с пагинацией, сортировкой и фильтрами,
 * просмотр деталей, создание, редактирование и удаление (включая AJAX-удалениe).
 * Также предоставляет вспомогательные данные (города, кольца, типы существ) для форм.
 */
@Controller
@RequestMapping("/bookcreatures")
public class BookCreatureController {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookCreatureService creatureService;
    private final MagicCityService magicCityService;
    private final RingService ringService;

    public BookCreatureController(BookCreatureService creatureService,
                                  MagicCityService magicCityService,
                                  RingService ringService) {
        this.creatureService = creatureService;
        this.magicCityService = magicCityService;
        this.ringService = ringService;
    }

    /**
     * Добавляет в каждую модель список доступных типов существ (хоббит, эльф, человек, голлум),
     * чтобы в любой форме на странице существа можно было выбрать тип из выпадающего списка.
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
     * Отображает список существ с пагинацией, сортировкой и фильтрацией.
     * <p>
     * Параметры запроса:
     * <ul>
     *   <li>{@code page} / {@code size} — страница и размер страницы (по умолчанию 1 и 10);</li>
     *   <li>{@code sort} / {@code dir} — колонка и направление сортировки;</li>
     *   <li>{@code filterColumn} / {@code filterValue} — колонка и значение фильтра;</li>
     *   <li>{@code maxDistance}, {@code creationFrom}, {@code creationTo} — фильтры по дистанции и дате;</li>
     *   <li>{@code creatureLocationId}, {@code ringId} — фильтры по городу и кольцу.</li>
     * </ul>
     * В модель передаются элементы страницы, параметры пагинации, текущие значения фильтров
     * и списки городов/колец для фильтров на форме.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) String dir,
                       @RequestParam(required = false) String filterColumn,
                       @RequestParam(required = false) String filterValue,
                       @RequestParam(required = false) String maxDistance,
                       @RequestParam(required = false) String creationFrom,
                       @RequestParam(required = false) String creationTo,
                       @RequestParam(required = false) String creatureLocationId,
                       @RequestParam(required = false) String ringId,
                       Model model) {
        String sortColumn = (sort != null && !sort.isBlank()) ? sort : "id";
        String sortDir = (dir != null && !dir.isBlank()) ? dir : "asc";
        List<BookCreatureListDto> items = creatureService.findPaged(page, size, sortColumn, sortDir,
                filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
        long total = creatureService.count(filterColumn, filterValue, maxDistance, creationFrom, creationTo,
                creatureLocationId, ringId);
        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("items", items);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("sort", sortColumn);
        model.addAttribute("dir", sortDir);
        model.addAttribute("filterColumn", filterColumn);
        model.addAttribute("filterValue", filterValue);
        model.addAttribute("maxDistance", maxDistance);
        model.addAttribute("creationFrom", creationFrom);
        model.addAttribute("creationTo", creationTo);
        model.addAttribute("creatureLocationId", creatureLocationId);
        model.addAttribute("ringId", ringId);
        model.addAttribute("cities", magicCityService.findAll());
        model.addAttribute("rings", ringService.findAll());
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);
        return "bookcreatures/list";
    }

    /**
     * Отображает детальную страницу существа по его ID.
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BookCreatureListDto dto = creatureService.findById(id);
        model.addAttribute("creature", dto);
        return "bookcreatures/detail";
    }

    /**
     * Отображает форму создания нового существа.
     * В модель передаются пустые DTO существа и координат, а также списки городов и колец.
     */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("creature", new BookCreatureDto());
        model.addAttribute("coordinates", new CoordinatesDto());
        model.addAttribute("cities", magicCityService.findAll());
        model.addAttribute("rings", ringService.findAll());
        return "bookcreatures/form";
    }

    /**
     * Создаёт нового существа из данных формы.
     * <ul>
     *   <li>{@code @Valid} — запускает валидацию полей {@code BookCreatureDto};</li>
     *   <li>{@code BindingResult} — содержит ошибки валидации, если они есть;</li>
     *   <li>{@code CoordinatesDto} — вложенный объект координат, связывается отдельно, т.к. он
     *       не является частью основной формы напрямую;</li>
     *   <li>{@code RedirectAttributes} — передаёт flash-сообщение об успехе/ошибке при редиректе.</li>
     * </ul>
     * При ошибках валидации или исключении в сервисе форма отображается повторно с сообщением об ошибке.
     * После успешного создания — редирект на список с сохранением текущих параметров страницы и фильтров.
     */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("creature") BookCreatureDto dto,
                         BindingResult bindingResult,
                         @ModelAttribute("coordinates") CoordinatesDto coordinates,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false) String dir,
                         @RequestParam(required = false) String filterColumn,
                         @RequestParam(required = false) String filterValue,
                         @RequestParam(required = false) String maxDistance,
                       @RequestParam(required = false) String creationFrom,
                       @RequestParam(required = false) String creationTo,
                       @RequestParam(required = false) String creatureLocationId,
                       @RequestParam(required = false) String ringId) {
        // Координаты — обязательный вложенный объект; если не пришли из формы, подставляем отдельный DTO
        if (dto.getCoordinates() == null) {
            dto.setCoordinates(coordinates);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("creature", dto);
            return "bookcreatures/form";
        }
        try {
            creatureService.create(dto);
            redirectAttributes.addFlashAttribute("success", "Существо успешно создано");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("creature", dto);
            return "bookcreatures/form";
        }
        return "redirect:/bookcreatures?page=" + page + "&size=" + size
                + (sort != null ? "&sort=" + sort : "")
                + (dir != null ? "&dir=" + dir : "")
                + (filterColumn != null ? "&filterColumn=" + filterColumn : "")
                + (filterValue != null ? "&filterValue=" + filterValue : "")
                + (maxDistance != null ? "&maxDistance=" + maxDistance : "")
                + (creationFrom != null ? "&creationFrom=" + creationFrom : "")
                + (creationTo != null ? "&creationTo=" + creationTo : "")
                + (creatureLocationId != null ? "&creatureLocationId=" + creatureLocationId : "")
                + (ringId != null ? "&ringId=" + ringId : "");
    }

    /**
     * Отображает форму редактирования существа с предзаполненными данными.
     * Города и кольца передаются для выпадающих списков на форме.
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        BookCreatureListDto dto = creatureService.findById(id);
        BookCreatureDto form = new BookCreatureDto();
        form.setId(dto.getId());
        form.setName(dto.getName());
        form.setAge(dto.getAge());
        form.setAttackLevel(dto.getAttackLevel());
        form.setDefenseLevel(dto.getDefenseLevel());
        form.setCreatureType(dto.getCreatureType() != null
                ? new BookCreatureTypeDto(dto.getCreatureType(), dto.getCreatureType()) : null);
        form.setCreatureLocationId(null); // заполняется в форме при необходимости
        form.setRingId(null);
        if (dto.getCoordinates() != null) {
            form.setCoordinates(dto.getCoordinates());
        }
        model.addAttribute("creature", form);
        model.addAttribute("cities", magicCityService.findAll());
        model.addAttribute("rings", ringService.findAll());
        return "bookcreatures/form";
    }

    /**
     * Обновляет существо по ID данными из формы.
     * Логика analogous {@link #create}: валидация, обработка ошибок, редирект на список
     * с сохранением параметров пагинации и фильтров.
     */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("creature") BookCreatureDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false) String dir,
                         @RequestParam(required = false) String filterColumn,
                         @RequestParam(required = false) String filterValue,
                         @RequestParam(required = false) String maxDistance,
                         @RequestParam(required = false) String creationFrom,
                         @RequestParam(required = false) String creationTo,
                         @RequestParam(required = false) String creatureLocationId,
                         @RequestParam(required = false) String ringId) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("creature", dto);
            return "bookcreatures/form";
        }
        try {
            creatureService.update(id, dto);
            redirectAttributes.addFlashAttribute("success", "Существо успешно обновлено");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("creature", dto);
            return "bookcreatures/form";
        }
        return "redirect:/bookcreatures?page=" + page + "&size=" + size
                + (sort != null ? "&sort=" + sort : "")
                + (dir != null ? "&dir=" + dir : "")
                + (filterColumn != null ? "&filterColumn=" + filterColumn : "")
                + (filterValue != null ? "&filterValue=" + filterValue : "")
                + (maxDistance != null ? "&maxDistance=" + maxDistance : "")
                + (creationFrom != null ? "&creationFrom=" + creationFrom : "")
                + (creationTo != null ? "&creationTo=" + creationTo : "")
                + (creatureLocationId != null ? "&creatureLocationId=" + creatureLocationId : "")
                + (ringId != null ? "&ringId=" + ringId : "");
    }

    /**
     * Удаляет существо по ID и перенаправляет на список с сохранением параметров страницы и фильтров.
     * Результат (успех или ошибка) передаётся через flash-атрибуты.
     */
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String sort,
                         @RequestParam(required = false) String dir,
                         @RequestParam(required = false) String filterColumn,
                         @RequestParam(required = false) String filterValue,
                         @RequestParam(required = false) String maxDistance,
                         @RequestParam(required = false) String creationFrom,
                         @RequestParam(required = false) String creationTo,
                         @RequestParam(required = false) String creatureLocationId,
                         @RequestParam(required = false) String ringId,
                         RedirectAttributes redirectAttributes) {
        try {
            creatureService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Существо успешно удалено");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookcreatures?page=" + page + "&size=" + size
                + (sort != null ? "&sort=" + sort : "")
                + (dir != null ? "&dir=" + dir : "")
                + (filterColumn != null ? "&filterColumn=" + filterColumn : "")
                + (filterValue != null ? "&filterValue=" + filterValue : "")
                + (maxDistance != null ? "&maxDistance=" + maxDistance : "")
                + (creationFrom != null ? "&creationFrom=" + creationFrom : "")
                + (creationTo != null ? "&creationTo=" + creationTo : "")
                + (creatureLocationId != null ? "&creatureLocationId=" + creatureLocationId : "")
                + (ringId != null ? "&ringId=" + ringId : "");
    }

    /**
     * AJAX-удаление существа по ID. Возвращает JSON-карту вида
     * {@code {success: true/false, message: "..."}} для обновления интерфейса без перезагрузки страницы.
     */
    @PostMapping("/{id}/delete-ajax")
    @ResponseBody
    public Map<String, Object> deleteAjax(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            creatureService.delete(id);
            result.put("success", true);
            result.put("message", "Существо успешно удалено");
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", ex.getMessage());
        }
        return result;
    }
}
