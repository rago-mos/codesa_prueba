import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgClass, TitleCasePipe } from '@angular/common';
import { ProjectService } from '../../services/project.service';
import { TaskService } from '../../services/task.service';
import { NotificationService } from '../../core/notification.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { Project } from '../../models/project.model';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [ReactiveFormsModule, NavbarComponent, NgClass, TitleCasePipe],
  templateUrl: './project-detail.component.html',
  styleUrl: './project-detail.component.css'
})
export class ProjectDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private projectService = inject(ProjectService);
  private taskService = inject(TaskService);
  private notification = inject(NotificationService);

  project: Project | null = null;
  tasks: Task[] = [];
  loadingProject = true;
  loadingTasks = true;
  saving = false;

  isEditingProject = false;
  showTaskModal = false;
  editingTask: Task | null = null;

  editProjectForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: ['', [Validators.required, Validators.minLength(5)]]
  });

  taskForm = this.fb.group({
    titulo: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: ['', [Validators.required, Validators.minLength(5)]]
  });

  get projectId(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  get pendingCount() { return this.tasks.filter(t => t.estado === 'PENDIENTE').length; }
  get completedCount() { return this.tasks.filter(t => t.estado === 'COMPLETADA').length; }

  ngOnInit() {
    this.loadProject();
  }

  loadProject() {
    this.loadingProject = true;
    this.projectService.getProject(this.projectId).subscribe({
      next: (p) => {
        this.project = p;
        this.loadingProject = false;
        this.loadTasks();
      },
      error: (err) => {
        if (err.status === 403) {
          this.notification.error('No tienes permiso para ver este proyecto.');
        } else {
          this.notification.error('No se pudo cargar el proyecto.');
        }
        this.router.navigate(['/projects']);
      }
    });
  }

  loadTasks() {
    this.loadingTasks = true;
    this.taskService.listTasks(this.projectId).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.loadingTasks = false;
      },
      error: () => {
        this.notification.error('No se pudieron cargar las tareas.');
        this.loadingTasks = false;
      }
    });
  }

  startEditProject() {
    if (!this.project) return;
    this.editProjectForm.patchValue({
      nombre: this.project.nombre,
      descripcion: this.project.descripcion
    });
    this.isEditingProject = true;
  }

  cancelEditProject() {
    this.isEditingProject = false;
    this.editProjectForm.reset();
  }

  saveProject() {
    if (this.editProjectForm.invalid || !this.project) return;
    this.saving = true;
    const { nombre, descripcion } = this.editProjectForm.value;
    this.projectService.updateProject(this.project.id, nombre!, descripcion!).subscribe({
      next: (updated) => {
        this.project = updated;
        this.isEditingProject = false;
        this.notification.success('Proyecto actualizado correctamente.');
        this.saving = false;
      },
      error: () => {
        this.notification.error('Error al actualizar el proyecto.');
        this.saving = false;
      }
    });
  }

  archiveProject() {
    if (!this.project) return;
    if (!confirm(`¿Archivar el proyecto "${this.project.nombre}"?`)) return;
    this.projectService.archiveProject(this.project.id).subscribe({
      next: () => {
        this.notification.success('Proyecto archivado.');
        this.router.navigate(['/projects']);
      },
      error: () => {
        this.notification.error('Error al archivar el proyecto.');
      }
    });
  }

  openTaskModal(task?: Task) {
    this.editingTask = task ?? null;
    if (task) {
      this.taskForm.patchValue({ titulo: task.titulo, descripcion: task.descripcion });
    } else {
      this.taskForm.reset();
    }
    this.showTaskModal = true;
  }

  closeTaskModal() {
    this.showTaskModal = false;
    this.editingTask = null;
    this.taskForm.reset();
  }

  saveTask() {
    if (this.taskForm.invalid) return;
    this.saving = true;
    const { titulo, descripcion } = this.taskForm.value;

    const op$ = this.editingTask
      ? this.taskService.updateTask(this.projectId, this.editingTask.id, titulo!, descripcion!)
      : this.taskService.createTask(this.projectId, titulo!, descripcion!);

    op$.subscribe({
      next: () => {
        this.notification.success(this.editingTask ? 'Tarea actualizada.' : 'Tarea creada exitosamente.');
        this.closeTaskModal();
        this.loadTasks();
        this.saving = false;
      },
      error: (err) => {
        if (err.status === 409) {
          this.notification.error('No se pueden crear tareas en un proyecto archivado.');
        } else {
          this.notification.error('Error al guardar la tarea.');
        }
        this.saving = false;
      }
    });
  }

  deleteTask(task: Task) {
    if (!confirm(`¿Eliminar la tarea "${task.titulo}"?`)) return;
    this.taskService.deleteTask(this.projectId, task.id).subscribe({
      next: () => {
        this.notification.success('Tarea eliminada.');
        this.loadTasks();
      },
      error: () => {
        this.notification.error('Error al eliminar la tarea.');
      }
    });
  }

  goBack() {
    this.router.navigate(['/projects']);
  }
}