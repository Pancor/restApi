package com.pablo.restApi;

import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    @Profile("test")
    public CommandLineRunner setupDatabase(TasksRepository tasksRepository) {
        return args -> {
            tasksRepository.save(new Task("Task 1", "Content 1"));
            tasksRepository.save(new Task("Task 2", "Content 2"));
            tasksRepository.save(new Task("Task 3", "Content 3"));
        };
    }
}
