package com.pablo.restApi.data;

import com.pablo.restApi.models.Task;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TasksRepository extends CrudRepository<Task, Long> {

    Optional<Task> findByName(String name);

    @Modifying
    @Query("UPDATE Task t SET t.name = ?2, t.content = ?3 WHERE t.id = ?1")
    int updateTask(Long id, String name, String content);
}
