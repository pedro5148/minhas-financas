import { Component, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CartaoCreditoService } from '../../services/cartao-credito.service';
import { CartaoCredito } from '../../models/cartao-credito.model';
import { CartaoCardComponent } from './components/cartao-card/cartao-card.component';
import { CartaoModalComponent } from './components/cartao-modal/cartao-modal.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SeletorMesComponent, MesAno } from '../../components/seletor-mes/seletor-mes.component';

@Component({
  selector: 'app-cartoes',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatDialogModule, CartaoCardComponent, SeletorMesComponent],
  templateUrl: './cartoes.component.html',
  styleUrl: './cartoes.component.scss'
})
export class CartoesComponent implements OnInit {
  private cartaoService = inject(CartaoCreditoService);
  private dialog = inject(MatDialog);
  private destroyRef = inject(DestroyRef);

  cartoes: CartaoCredito[] = [];
  mesAnoSelecionado: {mes: number, ano: number} | null = null;
  private reloadTrigger = new BehaviorSubject<void>(undefined);
  
  mesAtual: Date = new Date();
  private mesAnoSubject = new BehaviorSubject<{mes: number, ano: number}>({ 
    mes: new Date().getMonth() + 1, 
    ano: new Date().getFullYear() 
  });

  ngOnInit(): void {
    combineLatest([this.reloadTrigger, this.mesAnoSubject]).pipe(
      takeUntilDestroyed(this.destroyRef),
      switchMap(([_, mesAno]) => {
        this.mesAnoSelecionado = mesAno;
        return this.cartaoService.listarTodos(mesAno.mes, mesAno.ano);
      })
    ).subscribe(data => {
      this.cartoes = data;
    });
  }

  onMesAlterado(evento: MesAno) {
    if (evento && evento.mes !== null) {
      this.mesAtual = new Date(evento.ano, evento.mes - 1, 1);
      this.mesAnoSubject.next({ mes: evento.mes, ano: evento.ano });
    }
  }

  carregarCartoes() {
    this.reloadTrigger.next();
  }

  adicionarCartao() {
    const dialogRef = this.dialog.open(CartaoModalComponent, {
      width: '600px',
      maxWidth: '95vw'
    });

    dialogRef.afterClosed().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
      if (result) {
        this.carregarCartoes();
      }
    });
  }
}
