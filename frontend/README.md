# Frontend

Angular 22 com componentes standalone, sem zone.js e estado em signals.

## Comandos

```bash
npm ci
npm start              # http://localhost:4200, com proxy de /api e /ws para o backend em :8080
npm test -- --watch=false
npm run lint
npm run format:check
npm run build
```

## Estrutura

- `src/app/core/`: serviços únicos (auth, STOMP, erros)
- `src/app/shared/`: componentes de apresentação reutilizáveis
- `src/app/features/<funcionalidade>/`: telas carregadas sob demanda
- `src/app/api/` (fase 1): cliente gerado do OpenAPI do backend, nunca editado à mão

Regras de lint que quebram a CI: `any` explícito, componente sem `OnPush` e propriedades que
deveriam ser signals.
