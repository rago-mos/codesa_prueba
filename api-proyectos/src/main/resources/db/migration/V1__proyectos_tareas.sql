CREATE TABLE IF NOT EXISTS proyectos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    owner VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS tareas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    proyecto_id BIGINT NOT NULL,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id)
);

INSERT INTO proyectos (nombre, descripcion, owner, estado) VALUES
    ('Proyecto Demo Admin', 'Un proyecto de demostración', 'admin', 'ACTIVE'),
    ('Proyecto User 1', 'Proyecto del usuario user', 'user', 'ACTIVE');