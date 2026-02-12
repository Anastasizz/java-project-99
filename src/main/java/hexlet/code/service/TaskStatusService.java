package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {
    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    TaskStatusMapper taskStatusMapper;

    public TaskStatusDTO getTaskStatusById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " Not found"));
        return taskStatusMapper.map(taskStatus);
    }

    public List<TaskStatusDTO> getAllTaskStatuses() {
        var taskStatuses = taskStatusRepository.findAll();

        return taskStatuses.stream()
                .map(status -> taskStatusMapper.map(status))
                .toList();
    }

    public TaskStatusDTO createTaskStatus(TaskStatusCreateDTO taskStatusData) {
        var taskStatus = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO updateTaskStatus(TaskStatusUpdateDTO data, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + "Not found"));

        taskStatusMapper.update(data, taskStatus);

        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    public void deleteTaskStatus(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
