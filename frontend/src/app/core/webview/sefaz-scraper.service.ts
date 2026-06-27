import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Platform } from '@angular/cdk/platform';
import { InAppBrowser, InAppBrowserObject } from '@awesome-cordova-plugins/in-app-browser/ngx';
import { CapacitorHttp } from '@capacitor/core';
import { Capacitor } from '@capacitor/core';

@Injectable({
  providedIn: 'root'
})
export class SefazScraperService {
  
  constructor(
    private platform: Platform,
    private iab: InAppBrowser,
    private ngZone: NgZone
  ) {}

  /**
   * Abre a URL da SEFAZ e tenta extrair o HTML após a resolução do CAPTCHA.
   */
  public extractHtmlFromSefaz(url: string): Observable<string> {
    const htmlSubject = new Subject<string>();

    if (!Capacitor.isNativePlatform()) {
      // Fallback para Web (apenas demonstração, CORS bloqueará iframe real)
      console.warn("Ambiente Web detectado. Usando mock HTML.");
      
      setTimeout(() => {
        const mockHtml = `
          <html>
            <body>
              <div id="u20" class="txtTopo">SUPERMERCADO MOCK LTDA</div>
              <div id="conteudo">
                <div class="txtCenter"><div class="text">CNPJ: 12.345.678/0001-90</div></div>
              </div>
              <span class="chave">43230812345678901234550010001234561123456789</span>
              <div id="infos">
                <ul><li>Emissão:  01/08/2023 12:00:00 </li></ul>
              </div>
              <table id="tabResult">
                <tr id="Item1">
                  <td>
                    <span class="txtTit">ARROZ BRANCO 5KG</span>
                    <span class="RCod">1234</span>
                    <span class="Rqtd">Qtde.: 2</span>
                    <span class="RUN">UN: UN</span>
                    <span class="RvlUnit">Vl. Unit.: 25,00</span>
                  </td>
                  <td class="txtTit noWrap"><span class="valor">50,00</span></td>
                </tr>
                <tr id="Item2">
                  <td>
                    <span class="txtTit">FEIJAO PRETO 1KG</span>
                    <span class="RCod">5678</span>
                    <span class="Rqtd">Qtde.: 3</span>
                    <span class="RUN">UN: KG</span>
                    <span class="RvlUnit">Vl. Unit.: 8,00</span>
                  </td>
                  <td class="txtTit noWrap"><span class="valor">24,00</span></td>
                </tr>
              </table>
              <div id="totalNota">
                <div id="linhaTotal"><label>Valor a pagar R$:</label><span class="totalNumb">74,00</span></div>
                <div id="linhaTotal"><label>Descontos R$:</label><span class="totalNumb">0,00</span></div>
              </div>
            </body>
          </html>
        `;
        htmlSubject.next(mockHtml);
        htmlSubject.complete();
      }, 1500);
      
    } else {
      let lastValidHtml: string | null = null;
      let anyHtml: string | null = null;

      const browser: InAppBrowserObject = this.iab.create(url, '_blank', 'location=no,zoom=no');

      // A nova estratégia: "Empurrar" (Push) em vez de "Puxar" (Pull).
      // Injetamos um robô dentro da página que envia o HTML continuamente de dentro para fora,
      // usando a ponte de mensagens segura nativa (postMessage).
      // Isso evita o bug de truncamento do Android (as famosas 19 linhas).
      const injectScraper = () => {
        const code = `
          if (!window.hasScraper) {
            window.hasScraper = true;
            setInterval(function() {
              try {
                var html = document.documentElement.outerHTML;
                webkit.messageHandlers.cordova_iab.postMessage(JSON.stringify({ html: html }));
              } catch(e) {}
            }, 1000);
          }
        `;
        browser.executeScript({ code: code }).catch(e => {});
      };

      // Injeta imediatamente para garantir na primeira página (CAPTCHA)
      let initialInterval = setInterval(() => {
         injectScraper();
      }, 1000);

      // Injeta novamente sempre que a página mudar (como o redirect para a Nota Fiscal)
      const loadStopSub = browser.on('loadstop').subscribe(() => {
         injectScraper();
      });

      // Escuta as mensagens enviadas de dentro do InAppBrowser
      browser.on('message').subscribe((event: any) => {
        try {
          const html = event.data?.html;
          if (html && html.length > 500) {
            const htmlLower = html.toLowerCase();
            if (!htmlLower.includes('cf-turnstile')) {
               anyHtml = html;
            }
            if (htmlLower.includes('valor') || htmlLower.includes('total') || htmlLower.includes('emissão')) {
               lastValidHtml = html;
            }
          }
        } catch(err) {
           console.error("Erro ao processar mensagem do IAB", err);
        }
      });

      browser.on('exit').subscribe(() => {
        if (initialInterval) clearInterval(initialInterval);
        if (loadStopSub) loadStopSub.unsubscribe();
        
        if (!htmlSubject.closed) {
          const finalHtml = lastValidHtml || anyHtml;
          
          this.ngZone.run(() => {
            if (finalHtml) {
              htmlSubject.next(finalHtml);
              htmlSubject.complete();
            } else {
              htmlSubject.error(new Error('A janela foi fechada, mas não foi possível capturar os dados da nota.'));
            }
          });
        }
      });
    }

    return htmlSubject.asObservable();
  }
}
