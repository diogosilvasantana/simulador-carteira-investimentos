// Convenção de commits do projeto (ver CONTRIBUTING.md e ADR-008).
// Formato: tipo(escopo): descrição no imperativo, em português, sem ponto final.
export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'perf', 'test', 'docs', 'style', 'build', 'ci', 'chore', 'revert'],
    ],
    'scope-enum': [
      2,
      'always',
      [
        // aplicações
        'backend',
        'frontend',
        'bridge',
        'infra',
        // módulos do backend
        'identity',
        'marketdata',
        'trading',
        'portfolio',
        'corporateactions',
        'analytics',
        'shared',
        'simulator',
        // transversais
        'docs',
        'adr',
        'ci',
        'deps',
        'release',
      ],
    ],
    'subject-case': [0],
    'header-max-length': [2, 'always', 100],
    'body-max-line-length': [1, 'always', 100],
  },
};
