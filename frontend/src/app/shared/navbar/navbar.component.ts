import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);

  username = '';
  role = '';

  ngOnInit() {
    this.auth.profile().subscribe({
      next: (p) => {
        this.username = p.username;
        this.role = p.role;
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}