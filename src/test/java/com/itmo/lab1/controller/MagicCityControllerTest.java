package com.itmo.lab1.controller;

import com.itmo.lab1.dto.MagicCityDto;
import com.itmo.lab1.service.MagicCityService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Тесты {@link MagicCityController} без реальной БД (сервис замокан).
 * Проверяет, что форма создания города принимает ISO-дату и редиректит на список.
 */
class MagicCityControllerTest {

    private final MagicCityService service = mock(MagicCityService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new MagicCityController(service)).build();

    private MagicCityDto newDto() {
        MagicCityDto dto = new MagicCityDto();
        dto.setId(1L);
        return dto;
    }

    @Test
    void createShouldRedirectToListAfterValidSubmit() throws Exception {
        when(service.create(any(MagicCityDto.class))).thenReturn(newDto());

        mvc.perform(post("/magiccities/new")
                        .param("name", "питер")
                        .param("area", "34.4")
                        .param("population", "4")
                        .param("establishmentDate", "2026-10-21")
                        .param("governor.code", "ELF")
                        .param("capital", "false")
                        .param("populationDensity", "43.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/magiccities"));

        verify(service, times(1)).create(any(MagicCityDto.class));
    }

    @Test
    void createShouldReturnFormWithErrorsOnInvalidDate() throws Exception {
        mvc.perform(post("/magiccities/new")
                        .param("name", "питер")
                        .param("area", "34.4")
                        .param("population", "4")
                        .param("establishmentDate", "not-a-date")
                        .param("governor.code", "ELF")
                        .param("capital", "false")
                        .param("populationDensity", "43.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("magiccities/form"))
                .andExpect(model().attributeHasFieldErrors("city", "establishmentDate"));

        verify(service, times(0)).create(any(MagicCityDto.class));
    }
}
