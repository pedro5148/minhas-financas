import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CartaoCreditoService } from '../../services/cartao-credito.service';
import { CartaoCredito } from '../../models/types';
import { CartaoCardComponent } from './components/cartao-card/cartao-card.component';
import { CartaoModalComponent } from './components/cartao-modal/cartao-modal.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-cartoes',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatDialogModule, CartaoCardComponent],
  templateUrl: './cartoes.component.html',
  styleUrl: './cartoes.component.scss'
})
export class CartoesComponent implements OnInit {
  private cartaoService = inject(CartaoCreditoService);
  private dialog = inject(MatDialog);

  cartoes: CartaoCredito[] = [];

  ngOnInit(): void {
    this.carregarCartoes();
  }

  carregarCartoes() {
    this.cartaoService.listarTodos().subscribe(data => {
      this.cartoes = data;
    });
  }

  adicionarCartao() {
    const dialogRef = this.dialog.open(CartaoModalComponent, {
      width: '600px',
      maxWidth: '95vw'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.carregarCartoes();
      }
    });
  }
}
