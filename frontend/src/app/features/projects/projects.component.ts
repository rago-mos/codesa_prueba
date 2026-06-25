import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProjectService } from '../../services/project.service';
import { NotificationService } from '../../core/notification.service';
import { NavbarComponent } from '../../shared/navbar/navbar.component';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [ReactiveFormsModule, NavbarComponent],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.css'
})
export class ProjectsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private projectService = inject(ProjectService);
  private notification = inject(NotificationService);
  private router = inject(Router);

  projects: Project[] = [];
  loading = true;
  saving = false;

  showCreateModal = false;
  showEditModal = false;
  editingProject: Project | null = null;

  createForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: ['', [Validators.required, Validators.minLength(5)]]
  });

  editForm = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: ['', [Validators.required, Validators.minLength(5)]]
  });

  get activeCount() { return this.projects.filter(p => p.estado === 'ACTIVE').length; }
  get archivedCount() { return this.projects.filter(p => p.estado === 'ARCHIVED').length; }

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.loading = true;
    this.projectService.listProjects().subscribe({
      next: (data) => {
        this.projects = data;
        this.loading = false;
      },
      error: () => {
        this.notification.error('No se pudieron cargar los proyectos.');
        this.loading = false;
      }
    });
  }

  openCreateModal() {
    this.createForm.reset();
    this.showCreateModal = true;
  }

  closeCreateModal() {
    this.showCreateModal = false;
    this.createForm.reset();
  }

  createProject() {
    if (this.createForm.invalid) return;
    this.saving = true;
    const { nombre, descripcion } = this.createForm.value;
    this.projectService.createProject(nombre!, descripcion!).subscribe({
      next: () => {
        this.notification.success('Proyecto creado exitosamente.');
        this.closeCreateModal();
        this.loadProjects();
        this.saving = false;
      },
      error: () => {
        this.notification.error('Error al crear el proyecto. Inténtalo nuevamente.');
        this.saving = false;
      }
    });
  }

  openEditModal(project: Project) {
    this.editingProject = project;
    this.editForm.patchValue({ nombre: project.nombre, descripcion: project.descripcion });
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
    this.editingProject = null;
    this.editForm.reset();
  }

  updateProject() {
    if (this.editForm.invalid || !this.editingProject) return;
    this.saving = true;
    const { nombre, descripcion } = this.editForm.value;
    this.projectService.updateProject(this.editingProject.id, nombre!, descripcion!).subscribe({
      next: () => {
        this.notification.success('Proyecto actualizado correctamente.');
        this.closeEditModal();
        this.loadProjects();
        this.saving = false;
      },
      error: () => {
        this.notification.error('Error al actualizar el proyecto.');
        this.saving = false;
      }
    });
  }

  viewProject(id: number) {
    this.router.navigate(['/projects', id]);
  }

  archiveProject(project: Project) {
    if (!confirm(`¿Archivar el proyecto "${project.nombre}"? Esta acción no se puede deshacer.`)) return;
    this.projectService.archiveProject(project.id).subscribe({
      next: () => {
        this.notification.success('Proyecto archivado.');
        this.loadProjects();
      },
      error: () => {
        this.notification.error('Error al archivar el proyecto.');
      }
    });
  }
}
