import { describe, expect, it } from 'vitest';
import enMessages from '../i18n/en.json';
import { getAllMessageKeys, t } from '../i18n';
import { LEVELS } from '../game/levels';

describe('i18n English localization', () => {
  it('resolves and interpolates keys cleanly', () => {
    expect(t('app.title')).toBe('Princess Storybook Puzzle');
    expect(t('hub.piecesCount', { count: 4 })).toBe('4 Pieces');
    expect(t('hub.starsTotal', { earned: 6, total: 48 })).toBe('6 / 48');
  });

  it('contains strictly English text with no Vietnamese characters in en.json', () => {
    const vietnameseDiacriticRegex =
      /[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđĐ]/;

    for (const key of getAllMessageKeys()) {
      const value = enMessages[key];
      expect(typeof value).toBe('string');
      expect(value.trim().length).toBeGreaterThan(0);
      expect(vietnameseDiacriticRegex.test(value)).toBe(false);
    }
  });

  it('has valid title keys for all 16 levels', () => {
    expect(LEVELS).toHaveLength(16);
    for (const level of LEVELS) {
      const title = t(level.titleKey);
      expect(title).not.toBe(level.titleKey);
      expect(title.length).toBeGreaterThan(2);
    }
  });
});
