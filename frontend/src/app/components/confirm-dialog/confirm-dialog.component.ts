import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="confirm-dialog-container">
      <div class="confirm-icon-wrapper">
        <mat-icon class="confirm-icon">delete_forever</mat-icon>
      </div>
      <h2 mat-dialog-title class="confirm-title">{{ data.titulo }}</h2>
      <mat-dialog-content>
        <p class="confirm-message">{{ data.mensagem }}</p>
      </mat-dialog-content>
      <mat-dialog-actions align="center" class="actions-container">
        <button mat-button (click)="cancelar()" class="btn-cancelar">Cancelar</button>
        <button mat-raised-button color="warn" (click)="confirmar()" class="btn-confirmar">Excluir</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog-container {
      padding: 24px 16px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .confirm-icon-wrapper {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      background-color: rgba(239, 68, 68, 0.1); /* bg-red-100/50 fallback */
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 20px;
    }
    .confirm-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      color: #ef4444; /* text-red-500 */
    }
    .confirm-title {
      font-size: 1.35rem;
      font-weight: 700;
      color: var(--text-primary, #1f2937);
      margin: 0 0 12px 0;
    }
    .confirm-message {
      color: var(--text-secondary, #4b5563);
      font-size: 1rem;
      line-height: 1.5;
      margin: 0;
      padding: 0 16px;
    }
    .actions-container {
      margin-top: 8px;
      gap: 16px;
      display: flex;
      justify-content: center;
      width: 100%;
    }
    .btn-cancelar {
      border-radius: 8px;
      padding: 0 24px;
      font-weight: 500;
      color: var(--text-secondary, #4b5563);
    }
    .btn-confirmar {
      border-radius: 8px;
      padding: 0 32px;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
    mat-dialog-content {
      margin-bottom: 24px;
      max-height: none !important;
    }
  `]
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { titulo: string; mensagem: string }
  ) {}

  cancelar(): void {
    this.dialogRef.close(false);
  }

  confirmar(): void {
    this.dialogRef.close(true);
  }
}
