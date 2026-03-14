package hexlet.code.service.interfaces;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import java.util.List;



public interface TaskService {

    TaskDTO getTaskById(Long id);
    List<TaskDTO> getAllTasks(TaskParamsDTO params);
    TaskDTO createTask(TaskCreateDTO taskData);
    TaskDTO updateTask(TaskUpdateDTO taskData, Long id);
    void deleteTask(Long id);

}
