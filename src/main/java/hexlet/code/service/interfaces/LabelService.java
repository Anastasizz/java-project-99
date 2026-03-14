package hexlet.code.service.interfaces;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import java.util.List;

public interface LabelService {

    LabelDTO getLabelById(Long id);
    List<LabelDTO> getAllLabels();
    LabelDTO createLabel(LabelCreateDTO labelData);
    LabelDTO updateLabel(LabelUpdateDTO labelData, Long id);
    void deleteLabel(Long id);

}
