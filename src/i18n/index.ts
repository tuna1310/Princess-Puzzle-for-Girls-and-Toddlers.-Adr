import enMessages from './en.json';

export type MessageKey = keyof typeof enMessages;
export type MessageParams = Record<string, string | number>;

const dictionary: Record<string, string> = enMessages;

/**
 * Resolve a localized English UI string by key and interpolate `{param}` placeholders.
 */
export function t(key: MessageKey, params?: MessageParams): string {
  const template = dictionary[key];
  if (template === undefined) {
    return key;
  }

  if (!params) {
    return template;
  }

  return template.replace(/\{(\w+)\}/g, (match, token: string) => {
    const value = params[token];
    return value !== undefined ? String(value) : match;
  });
}

export function getAllMessageKeys(): MessageKey[] {
  return Object.keys(enMessages) as MessageKey[];
}
