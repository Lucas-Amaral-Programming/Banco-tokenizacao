export function nomeEhCompleto(nome: string): boolean {
  const partes = (nome ?? '').trim().split(/\s+/).filter(Boolean);
  if (partes.length < 2) {
    return false;
  }
  return partes.every((parte) => /^\p{L}{2,}$/u.test(parte));
}
