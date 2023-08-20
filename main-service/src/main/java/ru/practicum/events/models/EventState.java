package ru.practicum.events.models;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED,
    REJECTED    // отклонено администратором на этапе модерации
}
