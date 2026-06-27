import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ZXingScannerModule } from '@zxing/ngx-scanner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-scanner',
  standalone: true,
  imports: [CommonModule, ZXingScannerModule, MatButtonModule, MatIconModule],
  template: `
    <div class="scanner-container">
      <div class="scanner-header">
        <h3>Aponte a câmera para o QR Code da Nota Fiscal</h3>
        <button mat-icon-button (click)="close.emit()">
          <mat-icon>close</mat-icon>
        </button>
      </div>
      <zxing-scanner 
        [enable]="scannerEnabled"
        (scanSuccess)="onScanSuccess($event)"
        (camerasFound)="camerasFoundHandler($event)"
        (camerasNotFound)="camerasNotFoundHandler($event)"
        (scanError)="scanErrorHandler($event)">
      </zxing-scanner>
      <div *ngIf="!hasCameras" class="camera-error">
        Nenhuma câmera encontrada ou permissão negada. (Para acesso local, use http://localhost:4200 ou ative HTTPS).
      </div>
      <div class="mock-action">
        <button mat-raised-button color="primary" (click)="simularScan()">
          Simular Leitura (Ambiente Dev)
        </button>
      </div>
    </div>
  `,
  styles: [`
    .scanner-container {
      position: fixed;
      top: 0; left: 0; width: 100vw; height: 100vh;
      background: #000;
      z-index: 9999;
      display: flex;
      flex-direction: column;
    }
    .scanner-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      color: white;
      background: rgba(0,0,0,0.5);
    }
    zxing-scanner {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .camera-error {
      color: red;
      text-align: center;
      padding: 20px;
      background: white;
    }
    .mock-action {
      position: absolute;
      bottom: 40px;
      left: 50%;
      transform: translateX(-50%);
      background: white;
      padding: 10px 20px;
      border-radius: 8px;
    }
  `]
})
export class ScannerComponent {
  @Output() scanResult = new EventEmitter<string>();
  @Output() close = new EventEmitter<void>();

  scannerEnabled = true;
  hasCameras = true;

  onScanSuccess(result: string) {
    // Para evitar múltiplos scans rápidos
    if (this.scannerEnabled) {
      this.scannerEnabled = false;
      this.scanResult.emit(result);
    }
  }

  camerasFoundHandler(cameras: MediaDeviceInfo[]) {
    this.hasCameras = cameras && cameras.length > 0;
  }

  camerasNotFoundHandler(event: any) {
    this.hasCameras = false;
  }

  scanErrorHandler(error: any) {
    console.error('Scan erro', error);
  }

  simularScan() {
    // Simula a leitura de um QR Code válido da Sefaz
    const mockUrl = 'https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?chNFe=43230812345678901234550010001234561123456789&nVersao=100&tpAmb=1&dhEmi=323032332d30382d30315431323a30303a30302d30333a3030&vNF=150.50&vICMS=0.00&digVal=717171717171717171717171717171717171717171717171717171717171&cIdToken=000001&cHashQRCode=1234567890123456789012345678901234567890';
    this.onScanSuccess(mockUrl);
  }
}
