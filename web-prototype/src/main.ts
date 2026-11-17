import './styles/main.css';
import { registerSW } from 'virtual:pwa-register';
import { PrincessPuzzleApp } from './core/app';

registerSW({ immediate: true });

const canvas = document.getElementById('game-canvas');
if (!(canvas instanceof HTMLCanvasElement)) {
  throw new Error('Missing #game-canvas element');
}

const app = new PrincessPuzzleApp(canvas);
app.start();
