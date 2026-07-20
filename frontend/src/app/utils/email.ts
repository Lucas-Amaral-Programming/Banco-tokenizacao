export function emailEhValido(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((email ?? '').trim());
}
