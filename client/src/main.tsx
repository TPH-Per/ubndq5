import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import appConfig from '../app-config.json';
import App from './App.tsx';
import './index.css';

if (!window.APP_CONFIG) {
  window.APP_CONFIG = appConfig;
}

createRoot(document.getElementById('app')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
