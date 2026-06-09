import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-alert-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="alert-dialog-container">
      <div class="alert-icon-wrapper">
        <mat-icon class="alert-icon">warning</mat-icon>
      </div>
      <h2 mat-dialog-title class="alert-title">{{ data.titulo }}</h2>
      <mat-dialog-content>
        <p class="alert-message">{{ data.mensagem }}</p>
      </mat-dialog-content>
      <mat-dialog-actions align="center">
        <button mat-raised-button color="primary" (click)="fechar()" class="btn-entendi">Entendi</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .alert-dialog-container {
      padding: 16px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .alert-icon-wrapper {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background-color: rgba(245, 158, 11, 0.1);
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 16px;
    }
    .alert-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #f59e0b; /* Amber */
    }
    .alert-title {
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary, #333);
      margin: 0 0 8px 0;
    }
    .alert-message {
      color: var(--text-secondary, #666);
      font-size: 0.95rem;
      line-height: 1.5;
      margin: 0;
      padding: 0;
    }
    .btn-entendi {
      border-radius: 8px;
      padding: 0 32px;
      font-weight: 500;
      letter-spacing: 0.5px;
    }
    mat-dialog-content {
      margin-bottom: 24px;
      max-height: none !important;
    }
  `]
})
export class AlertDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<AlertDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { titulo: string; mensagem: string }
  ) {}

  fechar(): void {
    this.dialogRef.close();
  }
}
