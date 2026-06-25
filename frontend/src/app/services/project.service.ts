import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listProjects() {
    return this.http.get<any[]>(`${this.apiUrl}/api/projects`);
  }

  getProject(id: number) {
    return this.http.get<any>(`${this.apiUrl}/api/projects/${id}`);
  }

  createProject(nombre: string, descripcion: string) {
    return this.http.post<any>(`${this.apiUrl}/api/projects`, { nombre, descripcion });
  }

  updateProject(id: number, nombre: string, descripcion: string) {
    return this.http.put<any>(`${this.apiUrl}/api/projects/${id}`, { nombre, descripcion });
  }

  archiveProject(id: number) {
    return this.http.delete(`${this.apiUrl}/api/projects/${id}/archive`);
  }
}
