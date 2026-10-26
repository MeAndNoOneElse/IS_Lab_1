package com.itmo.lab1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Конфигурация Spring MVC (веб-слой приложения).
 * <p>
 * Настраивает два основных things:
 * <ul>
 *   <li>резолвер представлений Thymeleaf — преобразует имена представлений (например,
 *       {@code bookcreatures/list}) в HTML-шаблоны из папки {@code templates/};</li>
 *   <li>статические ресурсы — CSS, JS и картинки из папки {@code static/}, доступные
 *       по пути {@code /static/**}.</li>
 * </ul>
 * Настроено вручную через {@link WebMvcConfigurer}, потому что {@code @EnableWebMvc}
 * отключил бы автоконфигурацию Spring Boot, а нам нужен только кастомизированный
 * резолвер, а не полное отключение автонастройки.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Резолвер шаблонов Thymeleaf.
     * <ul>
     *   <li>{@code prefix=templates/} — корневая папка с HTML-шаблонами;</li>
     *   <li>{@code suffix=.html} — расширение файлов шаблонов;</li>
     *   <li>{@code cacheable=false} — кэш выключен, чтобы при правке шаблона
     *       изменения подхватывались без перезапуска сервера;</li>
     *   <li>{@code characterEncoding=UTF-8} — корректный отбор кириллицы.</li>
     * </ul>
     */
    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode("HTML");
        resolver.setCacheable(false);
        return resolver;
    }

    /**
     * Движок Thymeleaf. Связан с резолвером шаблонов — именно он рендерит HTML
     * из шаблонов с подстановкой данных модели.
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        return engine;
    }

    /**
     * Резолвер представлений Spring MVC. Связывает имена вьюх, которые возвращают
     * контроллеры, с шаблонами Thymeleaf через {@link #templateEngine()}.
     * Регистрируется как @Bean, поэтому в {@link #configureViewResolvers}
     * дополнительная регистрация не нужна.
     */
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // Резолвер уже зарегистрирован через @Bean viewResolver() выше, здесь ничего делать не надо.
    }

    /**
     * Обработчик статических ресурсов.
     * Запросы вида {@code /static/style.css} отдают файлы из {@code classpath:/static/}.
     * {@code setCachePeriod(0)} — кэширование в браузере выключено, чтобы при правке CSS/JS
     * изменения сразу видились без очистки кэша.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }
}
