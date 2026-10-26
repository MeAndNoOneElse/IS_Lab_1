package com.itmo.lab1.dto;

/**
 * DTO перечисления типа существа (для выпадающих списков в формах).
 */
public class BookCreatureTypeDto {

    private String code;
    private String label;

    public BookCreatureTypeDto() {
    }

    public BookCreatureTypeDto(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
