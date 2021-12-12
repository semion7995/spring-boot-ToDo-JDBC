package com.apress.todo.repository;

import java.util.Collection;

public interface CommonRepository<ToDo> {
     ToDo save (final ToDo domain);
     Iterable<ToDo> save(Collection<ToDo> domains);
     void delete(final ToDo domain);
     ToDo findById(String id);
     Iterable<ToDo> findAll();

}
