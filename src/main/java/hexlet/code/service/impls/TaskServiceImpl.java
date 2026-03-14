package hexlet.code.service.impls;

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
import hexlet.code.service.interfaces.TaskService;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskSpecification specBuilder;

    public TaskDTO getTaskById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));
        return taskMapper.map(task);
    }

    public List<TaskDTO> getAllTasks(TaskParamsDTO params) {
        var spec = specBuilder.build(params);
        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO createTask(TaskCreateDTO taskData) {
        var assigneeId = taskData.getAssigneeId();
        var slug = taskData.getStatus();
        var labelIds = taskData.getTaskLabelIds();

        var task = taskMapper.map(taskData);

        if (assigneeId != null) {
            var user = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id: "
                            + assigneeId + " does not exist!"));
            task.setAssignee(user);
        }

        var status = taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with slug: "
                        + slug + " does not exist!"));
        task.setTaskStatus(status);

        task.setLabels(getLabelsByIds(labelIds));

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO updateTask(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + id + " does not exist!"));

        if (taskData.getAssigneeId() != null) {
            Long assigneeId = taskData.getAssigneeId().orElse(null);
            if (assigneeId != null) {
                var user = userRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "User with id: " + assigneeId + " does not exist!"));
                task.setAssignee(user);
            } else if (taskData.getAssigneeId().isPresent()) {
                task.setAssignee(null);
            }
        }

        if (taskData.getStatus() != null) {
            String statusSlug = taskData.getStatus().orElse(null);
            if (statusSlug != null) {
                var status = taskStatusRepository.findBySlug(statusSlug)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Task status with slug: " + statusSlug + " does not exist!"));
                task.setTaskStatus(status);
            }
        }

        if (taskData.getTaskLabelIds() != null) {
            var labelIds = taskData.getTaskLabelIds().orElse(null);
            if (labelIds != null) {
                task.setLabels(getLabelsByIds(labelIds));
            } else if (taskData.getTaskLabelIds().isPresent()) {
                task.setLabels(null);
            }
        }

        taskMapper.update(taskData, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private List<Label> getLabelsByIds(List<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return Collections.emptyList();
        }
        Iterable<Label> labelsIterable = labelRepository.findAllById(labelIds);
        List<Label> labels = new ArrayList<>();
        labelsIterable.forEach(labels::add);
        return labels;
    }
}
