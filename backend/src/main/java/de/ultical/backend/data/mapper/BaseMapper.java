package de.ultical.backend.data.mapper;

import java.util.List;

public interface BaseMapper<T> {
    T get(int id);

    Integer update(T entity);

    Integer insert(T entity);

    void delete(T entity);

    void delete(int id);

    List<T> getAll();
}
