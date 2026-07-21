export function telefoneEhValido(telefone: string): boolean {
  const digitos = (telefone ?? '').replace(/\D/g, '');
  return digitos.length === 11 && digitos[2] === '9';
}
