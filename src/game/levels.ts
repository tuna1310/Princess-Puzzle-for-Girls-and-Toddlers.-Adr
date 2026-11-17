import type { MessageKey } from '../i18n';

export type SceneThemeId =
  | 'tiara'
  | 'bunny'
  | 'unicorn'
  | 'castle'
  | 'dragon'
  | 'carriage'
  | 'willow'
  | 'mermaid'
  | 'teaparty'
  | 'treehouse'
  | 'harp'
  | 'swan'
  | 'pegasus'
  | 'lantern'
  | 'carousel'
  | 'ball';

export interface LevelDefinition {
  id: number;
  titleKey: MessageKey;
  pieceCount: number;
  free: boolean;
  implementedInStep2: boolean;
  theme: SceneThemeId;
  accentColor: string;
  frameColor: string;
}

export const LEVELS: LevelDefinition[] = [
  {
    id: 1,
    titleKey: 'level.1.title',
    pieceCount: 2,
    free: true,
    implementedInStep2: true,
    theme: 'tiara',
    accentColor: '#FF9EC7',
    frameColor: '#FFF3FA'
  },
  {
    id: 2,
    titleKey: 'level.2.title',
    pieceCount: 3,
    free: true,
    implementedInStep2: true,
    theme: 'bunny',
    accentColor: '#C59BFF',
    frameColor: '#F7F2FF'
  },
  {
    id: 3,
    titleKey: 'level.3.title',
    pieceCount: 4,
    free: true,
    implementedInStep2: true,
    theme: 'unicorn',
    accentColor: '#8FD8FF',
    frameColor: '#F1FAFF'
  },
  {
    id: 4,
    titleKey: 'level.4.title',
    pieceCount: 4,
    free: true,
    implementedInStep2: true,
    theme: 'castle',
    accentColor: '#FFB8D2',
    frameColor: '#FFF5F8'
  },
  {
    id: 5,
    titleKey: 'level.5.title',
    pieceCount: 5,
    free: false,
    implementedInStep2: false,
    theme: 'dragon',
    accentColor: '#A8E6A3',
    frameColor: '#F2FCF1'
  },
  {
    id: 6,
    titleKey: 'level.6.title',
    pieceCount: 6,
    free: false,
    implementedInStep2: false,
    theme: 'carriage',
    accentColor: '#F9C4E4',
    frameColor: '#FDF3FA'
  },
  {
    id: 7,
    titleKey: 'level.7.title',
    pieceCount: 6,
    free: false,
    implementedInStep2: false,
    theme: 'willow',
    accentColor: '#9EDBFF',
    frameColor: '#F2FAFF'
  },
  {
    id: 8,
    titleKey: 'level.8.title',
    pieceCount: 8,
    free: false,
    implementedInStep2: false,
    theme: 'mermaid',
    accentColor: '#98EAE0',
    frameColor: '#F0FCFA'
  },
  {
    id: 9,
    titleKey: 'level.9.title',
    pieceCount: 8,
    free: false,
    implementedInStep2: false,
    theme: 'teaparty',
    accentColor: '#FFC3D8',
    frameColor: '#FFF4F8'
  },
  {
    id: 10,
    titleKey: 'level.10.title',
    pieceCount: 9,
    free: false,
    implementedInStep2: false,
    theme: 'treehouse',
    accentColor: '#BBE69F',
    frameColor: '#F5FCEF'
  },
  {
    id: 11,
    titleKey: 'level.11.title',
    pieceCount: 9,
    free: false,
    implementedInStep2: false,
    theme: 'harp',
    accentColor: '#D6B8FF',
    frameColor: '#F8F4FF'
  },
  {
    id: 12,
    titleKey: 'level.12.title',
    pieceCount: 10,
    free: false,
    implementedInStep2: false,
    theme: 'swan',
    accentColor: '#AEE4FF',
    frameColor: '#F3FBFF'
  },
  {
    id: 13,
    titleKey: 'level.13.title',
    pieceCount: 10,
    free: false,
    implementedInStep2: false,
    theme: 'pegasus',
    accentColor: '#FFD1B8',
    frameColor: '#FFF7F2'
  },
  {
    id: 14,
    titleKey: 'level.14.title',
    pieceCount: 12,
    free: false,
    implementedInStep2: false,
    theme: 'lantern',
    accentColor: '#FFE194',
    frameColor: '#FFFBEF'
  },
  {
    id: 15,
    titleKey: 'level.15.title',
    pieceCount: 12,
    free: false,
    implementedInStep2: false,
    theme: 'carousel',
    accentColor: '#FFAAD5',
    frameColor: '#FFF3F9'
  },
  {
    id: 16,
    titleKey: 'level.16.title',
    pieceCount: 12,
    free: false,
    implementedInStep2: false,
    theme: 'ball',
    accentColor: '#F3B6FF',
    frameColor: '#FCF4FF'
  }
];

export function getLevelById(id: number): LevelDefinition {
  return LEVELS.find((lvl) => lvl.id === id) ?? LEVELS[0]!;
}

export function isLevelUnlocked(level: LevelDefinition, unlockedAll: boolean): boolean {
  return level.free || unlockedAll;
}
