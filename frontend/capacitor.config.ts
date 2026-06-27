import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'br.minhasfinancas.app',
  appName: 'Minhas Financas',
  webDir: 'dist/frontend/browser',
  server: {
    url: 'https://sibling-unraveled-showpiece.ngrok-free.dev',
    cleartext: true
  }
};

export default config;
