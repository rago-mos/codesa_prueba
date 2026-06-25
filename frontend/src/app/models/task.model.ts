export interface Task {
  id: number;
  titulo: string;
  descripcion: string;
  estado: string;
  proyectoId: number;
}

export interface TaskRequest {
  titulo: string;
  descripcion: string;
}