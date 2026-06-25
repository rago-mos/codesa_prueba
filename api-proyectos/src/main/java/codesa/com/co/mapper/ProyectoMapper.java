package codesa.com.co.mapper;

import codesa.com.co.domain.Estado;
import codesa.com.co.domain.Proyecto;
import codesa.com.co.dto.ProyectoRequest;
import org.springframework.stereotype.Component;

@Component
public class ProyectoMapper {

    public Proyecto toEntity(ProyectoRequest request, String owner) {
        return Proyecto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .owner(owner)
                .estado(Estado.ACTIVE)
                .build();
    }
}
