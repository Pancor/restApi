package com.pablo.restApi.data;

import com.pablo.restApi.models.Task;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TasksRepository extends CrudRepository<Task, Long> {

    Optional<Task> findByName(String name);

    @Modifying
    @Query("UPDATE Task t SET t.name = :name, t.content = :content WHERE t.id = :id")
    int updateTask(@Param("id") Long id, @Param("name") String name, @Param("content") String content);

    @Modifying
    @Query("UPDATE Task t SET t.content = :content WHERE t.id = :id")
    int updateTask(@Param("id") Long id, @Param("content") String content);
}
