package codesa.com.co.mapper;

import codesa.com.co.domain.Proyecto;
import codesa.com.co.domain.Tarea;
import codesa.com.co.dto.TareaRequest;
import org.springframework.stereotype.Component;

@Component
public class TareaMapper {

    public Tarea toEntity(TareaRequest request, Proyecto proyecto) {
        return Tarea.builder()
                .titulo(request.titulo())
                .descripcion(request.descripcion())
                .estado("TODO")
                .proyecto(proyecto)
                .build();
    }
}
