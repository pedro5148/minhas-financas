import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CsvImportService {
  
  parseCsv(file: File): Promise<{ headers: string[], data: Record<string, string>[] }> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = (e) => {
        try {
          const content = e.target?.result as string;
          resolve(this.processCsvContent(content));
        } catch (error) {
          reject(error);
        }
      };
      
      reader.onerror = (err) => reject(err);
      reader.readAsText(file, 'UTF-8');
    });
  }

  private processCsvContent(content: string): { headers: string[], data: Record<string, string>[] } {
    // Separa por linhas lidando com CR e LF
    const lines = content.split(/\r?\n/).filter(line => line.trim() !== '');
    if (lines.length === 0) {
      return { headers: [], data: [] };
    }

    const headers = this.parseLine(lines[0]).map(h => h.trim());
    const data: Record<string, string>[] = [];

    for (let i = 1; i < lines.length; i++) {
      const values = this.parseLine(lines[i]);
      const row: Record<string, string> = {};
      
      headers.forEach((header, index) => {
        // Garantir que as chaves fiquem atreladas mesmo que falte dados no final da linha
        row[header] = values[index] !== undefined ? values[index].trim() : '';
      });
      data.push(row);
    }

    return { headers, data };
  }

  private parseLine(line: string): string[] {
    // Regex para fazer match de campos delimitados por vírgula ou ponto e vírgula,
    // ignorando os delimitadores que estão dentro de aspas duplas.
    const re = /,(?=(?:(?:[^"]*"){2})*[^"]*$)|;(?=(?:(?:[^"]*"){2})*[^"]*$)/;
    
    return line.split(re).map(value => {
      let val = value.trim();
      // Remove as aspas delimitadoras se existirem e escapa aspas duplas
      if (val.startsWith('"') && val.endsWith('"')) {
        val = val.substring(1, val.length - 1).replace(/""/g, '"');
      }
      return val;
    });
  }
}
