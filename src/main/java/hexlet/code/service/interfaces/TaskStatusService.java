package hexlet.code.service.interfaces;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import java.util.List;


public interface TaskStatusService {

    TaskStatusDTO getTaskStatusById(Long id);
    List<TaskStatusDTO> getAllTaskStatuses();
    TaskStatusDTO createTaskStatus(TaskStatusCreateDTO taskStatusData);
    TaskStatusDTO updateTaskStatus(TaskStatusUpdateDTO data, Long id);
    void deleteTaskStatus(Long id);

}
