package ru.langauge.coursework.core.mapper;

import ru.langauge.coursework.core.entity.ErrorEntity;
import ru.langauge.coursework.core.service.FileService;
import ru.langauge.coursework.view_logic.ErrorModel;

import java.util.List;

public class ErrorMapper {
    public List<ErrorModel> map(List<ErrorEntity> list, FileService fileService) {
        return list.stream().map(
                errorEntity -> new ErrorModel(
                        fileService.getCurrentFileName(),
                        errorEntity.error(),
                        errorEntity.line(),
                        errorEntity.column()
                )
        ).toList();
    }

}
