"""Gera o APP_PASSWORD_HASH do login sem que a senha saia do seu computador.

Uso (no PC, uma vez):
    python -m pip install bcrypt
    python scripts/gerar-hash-senha.py

Cole o resultado na variável APP_PASSWORD_HASH do Coolify, marcada como segredo. O resultado vem
em Base64 porque o Docker Compose trataria os cifrões do hash BCrypt como variáveis.
"""

import base64
import getpass
import sys

import bcrypt

MIN_LENGTH = 12


def main() -> None:
    password = getpass.getpass("Senha (não aparece enquanto digita): ")
    if len(password) < MIN_LENGTH:
        sys.exit(f"Use pelo menos {MIN_LENGTH} caracteres; uma frase longa é melhor ainda.")
    if getpass.getpass("Repita a senha: ") != password:
        sys.exit("As senhas não conferem.")
    hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(rounds=12))
    print()
    print("APP_PASSWORD_HASH=" + base64.b64encode(hashed).decode("ascii"))


if __name__ == "__main__":
    main()
