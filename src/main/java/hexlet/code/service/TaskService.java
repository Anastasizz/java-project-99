package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSpecification specBuilder;

    public TaskDTO getTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));
        return taskMapper.map(task);
    }

    public List<TaskDTO> getAllTasks(TaskParamsDTO params) {
        var spec = specBuilder.build(params);
        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(task -> taskMapper.map(task))
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO taskData) {
        var assigneeId = taskData.getAssigneeId();
        var slug = taskData.getStatus();
        var labelIds = taskData.getLabelIds();

        var task = taskMapper.map(taskData);

        if(assigneeId != null) {
            var user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id: "
                            + assigneeId + " does not exist!"));
            task.setAssignee(user);
        }

        var status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with slug: "
                        + slug + " does not exist!"));
        task.setTaskStatus(status);

        if(labelIds != null) {
            var labels = labelIds.stream()
                    .map(id -> labelRepository.findById(id).orElseThrow(
                            () -> new ResourceNotFoundException("Label with id: "
                                    + id + " does not exist!")
                    ))
                    .collect(Collectors.toList());
            task.setLabels(labels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));

        if (taskData.getAssigneeId() != null && taskData.getAssigneeId().isPresent()) {
            var assigneeId = taskData.getAssigneeId().get();
            if(assigneeId == null) {
                task.setAssignee(null);
            } else {
                var user = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User with id: "
                                + assigneeId + " does not exist!"));
                task.setAssignee(user);
            }
        }

        if (taskData.getStatus() != null && taskData.getStatus().isPresent()) {
            var slug = taskData.getStatus().get();
            var status = taskStatusRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status with slug: "
                            + slug + " does not exist!"));
            task.setTaskStatus(status);
        }

        if (taskData.getLabelIds() != null && taskData.getLabelIds().isPresent()) {
            var labelIds = taskData.getLabelIds().get();

            var labels = labelIds.stream()
                            .map(labelId -> labelRepository.findById(labelId).orElseThrow(
                                    () -> new ResourceNotFoundException("Label with id: "
                                            + labelId + " does not exist!")
                            ))
                            .collect(Collectors.toList());
            task.setLabels(labels);
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
