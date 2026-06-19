import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

export interface MesAno {
  mes: number | null;
  ano: number;
}

@Component({
  selector: 'app-seletor-mes',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './seletor-mes.component.html',
  styleUrl: './seletor-mes.component.scss'
})
export class SeletorMesComponent implements OnInit {
  @Input() dataInicial: Date = new Date();
  @Input() permitirVisaoAnual = false;
  
  @Output() mesAlterado = new EventEmitter<MesAno>();

  isMesEspecifico = true;
  dataAtual: Date = new Date();

  ngOnInit() {
    this.dataAtual = new Date(this.dataInicial.getFullYear(), this.dataInicial.getMonth(), 1);
  }

  mudarMes(delta: number) {
    if (!this.isMesEspecifico) return;
    this.dataAtual = new Date(this.dataAtual.getFullYear(), this.dataAtual.getMonth() + delta, 1);
    this.emitirEvento();
  }

  getNomeMesAno(): string {
    if (!this.isMesEspecifico) return `Visão Anual ${this.dataAtual.getFullYear()}`;
    const meses = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    return `${meses[this.dataAtual.getMonth()]} ${this.dataAtual.getFullYear()}`;
  }

  toggleVisaoAnual() {
    this.isMesEspecifico = !this.isMesEspecifico;
    if (this.isMesEspecifico) {
      this.dataAtual = new Date();
    }
    this.emitirEvento();
  }

  private emitirEvento() {
    if (!this.isMesEspecifico) {
      this.mesAlterado.emit({
        mes: null,
        ano: this.dataAtual.getFullYear()
      });
    } else {
      this.mesAlterado.emit({
        mes: this.dataAtual.getMonth() + 1,
        ano: this.dataAtual.getFullYear()
      });
    }
  }
}
