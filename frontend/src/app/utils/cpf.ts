export function cpfEhValido(cpf: string): boolean {
  const digitos = (cpf ?? '').replace(/\D/g, '');
  if (digitos.length !== 11 || /^(\d)\1{10}$/.test(digitos)) {
    return false;
  }
  return verificarDigito(digitos, 9, 10) && verificarDigito(digitos, 10, 11);
}

function verificarDigito(digitos: string, posicao: number, pesoInicial: number): boolean {
  let soma = 0;
  for (let i = 0; i < posicao; i++) {
    soma += Number(digitos[i]) * (pesoInicial - i);
  }
  const resto = 11 - (soma % 11);
  const digitoCalculado = resto >= 10 ? 0 : resto;
  return digitoCalculado === Number(digitos[posicao]);
}
