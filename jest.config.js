module.exports = {
  preset: 'react-native',
  coverageProvider: 'babel',
  coverageReporters: ['text', 'cobertura', 'lcov', 'json'],
  moduleDirectories: ['node_modules'],
  setupFiles: ['./__tests__/__mocks__/RNCBL.ts'],
  testTimeout: 8000,
  testMatch: ['**/*.test.ts'],
  coverageThreshold: {
    global: {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
  },
  collectCoverageFrom: ['src/**/*.{ts,tsx}'],
  coveragePathIgnorePatterns: ['node_modules', '__tests__', '__mocks__'],
};
