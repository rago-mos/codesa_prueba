import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listTasks(projectId: number) {
    return this.http.get<any[]>(`${this.apiUrl}/api/projects/${projectId}/tasks`);
  }

  createTask(projectId: number, titulo: string, descripcion: string) {
    return this.http.post<any>(`${this.apiUrl}/api/projects/${projectId}/tasks`, { titulo, descripcion });
  }

  updateTask(projectId: number, taskId: number, titulo: string, descripcion: string) {
    return this.http.put<any>(`${this.apiUrl}/api/projects/${projectId}/tasks/${taskId}`, { titulo, descripcion });
  }

  deleteTask(projectId: number, taskId: number) {
    return this.http.delete(`${this.apiUrl}/api/projects/${projectId}/tasks/${taskId}`);
  }
}
