package com.itmo.lab1.controller;

import com.itmo.lab1.dto.BookCreatureDto;
import com.itmo.lab1.dto.BookCreatureListDto;
import com.itmo.lab1.dto.CoordinatesDto;
import com.itmo.lab1.service.BookCreatureService;
import com.itmo.lab1.service.MagicCityService;
import com.itmo.lab1.service.RingService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тесты {@link BookCreatureController} без загрузки контекста Spring:
 * методы контроллера вызываются напрямую с моком сервиса.
 */
class BookCreatureControllerTest {

    private final BookCreatureService service = mock(BookCreatureService.class);
    private final MagicCityService magicCityService = mock(MagicCityService.class);
    private final RingService ringService = mock(RingService.class);
    private final BookCreatureController controller =
            new BookCreatureController(service, magicCityService, ringService);

    @Test
    void listShouldReturnListViewWithPaginatedModel() {
        String view = controller.list(1, 10, "id", "asc", null, null, null, null, null,
                null, null, new ExtendedModelMap());
        assertEquals("bookcreatures/list", view);
    }

    @Test
    void detailShouldReturnDetailView() {
        String view = controller.detail(1L, new ExtendedModelMap());
        assertEquals("bookcreatures/detail", view);
    }

    @Test
    void createFormShouldReturnFormView() {
        String view = controller.createForm(new ExtendedModelMap());
        assertEquals("bookcreatures/form", view);
    }

    @Test
    void createWithErrorsShouldReturnFormView() {
        BookCreatureDto dto = new BookCreatureDto();
        BindingResult errors = new BeanPropertyBindingResult(dto, "creature");
        errors.rejectValue("name", "NotEmpty", "Имя обязательно");

        String view = controller.create(dto, errors, new CoordinatesDto(),
                new RedirectAttributesModelMap(), new ExtendedModelMap(),
                1, 10, null, null, null, null, null, null, null, null, null);

        assertEquals("bookcreatures/form", view);
        verify(service, never()).create(any());
    }

    @Test
    void createSuccessShouldRedirectToList() {
        BookCreatureDto dto = new BookCreatureDto();
        dto.setName("Bilbo");
        BindingResult errors = new BeanPropertyBindingResult(dto, "creature");

        String view = controller.create(dto, errors, new CoordinatesDto(),
                new RedirectAttributesModelMap(), new ExtendedModelMap(),
                1, 10, null, null, null, null, null, null, null, null, null);

        assertTrue(view.startsWith("redirect:/bookcreatures"));
        verify(service).create(dto);
    }

    @Test
    void editFormShouldReturnFormView() {
        when(service.findById(1L)).thenReturn(new BookCreatureListDto());
        String view = controller.editForm(1L, new ExtendedModelMap());
        assertEquals("bookcreatures/form", view);
    }

    @Test
    void updateWithErrorsShouldReturnFormView() {
        BookCreatureDto dto = new BookCreatureDto();
        BindingResult errors = new BeanPropertyBindingResult(dto, "creature");
        errors.rejectValue("age", "Min", "Возраст должен быть больше 0");

        String view = controller.update(1L, dto, errors,
                new RedirectAttributesModelMap(), new ExtendedModelMap(),
                1, 10, null, null, null, null, null, null, null, null, null);

        assertEquals("bookcreatures/form", view);
        verify(service, never()).update(anyLong(), any());
    }

    @Test
    void updateSuccessShouldRedirectToDetail() {
        BookCreatureDto dto = new BookCreatureDto();
        dto.setName("Bilbo");
        BindingResult errors = new BeanPropertyBindingResult(dto, "creature");

        String view = controller.update(1L, dto, errors,
                new RedirectAttributesModelMap(), new ExtendedModelMap(),
                1, 10, null, null, null, null, null, null, null, null, null);

        assertTrue(view.startsWith("redirect:/bookcreatures"));
        verify(service).update(1L, dto);
    }

    @Test
    void deleteShouldRedirectToList() {
        String view = controller.delete(1L, 1, 10, null, null, null, null, null, null, null,
                null, null, new RedirectAttributesModelMap());
        assertTrue(view.startsWith("redirect:/bookcreatures"));
        verify(service).delete(1L);
    }
}
