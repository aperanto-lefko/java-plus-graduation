# ExploreWithMe

**ExploreWithMe** — микросервисное приложение-афиша для совместного планирования и участия в мероприятиях.

## 📚 Описание

Приложение позволяет:
- Делиться событиями (выставки, концерты, походы и пр.)
- Искать мероприятия по категориям, дате и популярности
- Участвовать и подтверждать участие в событиях
- Комментировать события
- Управлять пользователями и модерацией

## 🧩 Архитектура

Проект реализован на основе микросервисной архитектуры:

- **Gateway** — единая точка входа в приложение
- **Eureka** — сервис-регистрация и обнаружение
- **Config Server** — централизованная конфигурация
- **Event-service** — управление событиями
- **User Service** — управление пользователями
- **Request Service** — управление заявками на участие
- **Comment Service** — добавление и модерация комментариев
- **Statistics Service** — сбор и анализ статистики обращений

### Рекомендательная система
- **Collector** — сбор действий пользователей через gRPC и отправка в Kafka
- **Aggregator** — анализ данных и расчет сходства мероприятий
- **Analyzer** — генерация рекомендаций и gRPC API для их получения
- **Apache Kafka** — обработка потоков данных в реальном времени

## 🔐 Доступ

API разделено на уровни доступа:
- **Публичный** — поиск и просмотр событий, категорий и подборок
- **Авторизованный** — создание/редактирование событий, участие, комментарии
- **Административный** — управление категориями, модерация, пользователи

## 🌟 Рекомендательная система

### Основные возможности:
1. **Сбор действий пользователей**:
    - Просмотры событий
    - Участие в мероприятиях
    - Лайки событий

2. **Анализ данных**:
    - Расчет косинусного сходства мероприятий
    - Обновление рекомендаций в реальном времени

3. **Персонализированные рекомендации**:
    - Подбор похожих событий
    - Рекомендации на основе истории действий
    - Рейтинговая система мероприятий

## 💡 Дополнительная функциональность

Реализована поддержка **комментариев**:
- Пользователи могут оставлять и просматривать комментарии к событиям
- Администратор может модерировать комментарии

### Технологии обработки данных:
- **Apache Kafka** для потоковой обработки
- **gRPC** для высокопроизводительной коммуникации
- **Алгоритмы ML** (коллаборативная фильтрация, косинусное сходство)

## ⚙️ Технологии

- Java 21, Spring Boot, Spring Cloud
- Eureka Discovery, Spring Cloud Gateway
- Spring Cloud Config
- PostgreSQL, Hibernate
- Swagger/OpenAPI
- Apache Kafka
- gRPC, Protocol Buffers
- RestTemplate/Feign