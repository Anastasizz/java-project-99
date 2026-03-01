package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;



    public TaskDTO getTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));
        return taskMapper.map(task);
    }

    public List<TaskDTO> getAllTasks() {
        var tasks = taskRepository.findAll();
        return tasks.stream()
                .map(task -> taskMapper.map(task))
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO taskData) {
        var assigneeId = taskData.getAssigneeId();
        var slug = taskData.getStatus();

        var user = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: "
                        + assigneeId + " does not exist!"));
        var status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with slug: "
                        + slug + " does not exist!"));

        var task = taskMapper.map(taskData);
        task.setAssignee(user);
        task.setTaskStatus(status);

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));

        if (taskData.getAssigneeId() != null) {
            var assigneeId = taskData.getAssigneeId().get();
            var user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id: "
                            + assigneeId + " does not exist!"));
            task.setAssignee(user);
        }


        if (taskData.getStatus() != null) {
            var slug = taskData.getStatus().get();
            var status = taskStatusRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status with slug: "
                            + slug + " does not exist!"));
            task.setTaskStatus(status);
        }

        taskMapper.update(taskData, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void deleteTask(Long id) {
//        var task = taskRepository.findById(id)
//                        .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));

        taskRepository.deleteById(id);
    }
}
