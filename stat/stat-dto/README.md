# DTO Module

Этот модуль содержит классы DTO (Data Transfer Objects) с возможностью валидации для удобства и контроля данных.

## Особенности

- **Валидация**: Использованы аннотации для проверки валидности полей.
- **Ломбок**: Используется Lombok для генерации getter-ов, setter-ов и других методов (`@Getter`, `@Setter`, `@Builder`,
  `@NoArgsConstructor`, `@AllArgsConstructor`).

### Добавление зависимости

```xml
<dependency>
    <groupId>ru.practicum</groupId>
    <artifactId>dto</artifactId>
    <version>1.0.0</version>
</dependency>