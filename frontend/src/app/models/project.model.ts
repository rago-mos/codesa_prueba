export interface Project {
  id: number;
  nombre: string;
  descripcion: string;
  owner: string;
  estado: 'ACTIVE' | 'ARCHIVED';
}

export interface ProjectRequest {
  nombre: string;
  descripcion: string;
}