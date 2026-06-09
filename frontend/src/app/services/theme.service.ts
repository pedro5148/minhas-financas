import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  isDarkMode = signal<boolean>(false);

  constructor() {
    this.initTheme();
  }

  private initTheme() {
    const savedTheme = localStorage.getItem('minhas-financas-theme');
    if (savedTheme === 'dark') {
      this.setDarkTheme();
    } else if (savedTheme === 'light') {
      this.setLightTheme();
    } else {
      // Check system preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      if (prefersDark) {
        this.setDarkTheme();
      }
    }
  }

  toggleTheme() {
    if (this.isDarkMode()) {
      this.setLightTheme();
    } else {
      this.setDarkTheme();
    }
  }

  private setDarkTheme() {
    this.isDarkMode.set(true);
    document.body.classList.add('dark-theme');
    localStorage.setItem('minhas-financas-theme', 'dark');
  }

  private setLightTheme() {
    this.isDarkMode.set(false);
    document.body.classList.remove('dark-theme');
    localStorage.setItem('minhas-financas-theme', 'light');
  }
}
