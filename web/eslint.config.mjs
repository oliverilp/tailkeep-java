import { defineConfig, globalIgnores } from 'eslint/config';
import nextVitals from 'eslint-config-next/core-web-vitals';
import nextTypescript from 'eslint-config-next/typescript';

/*
 * The previous .eslintrc.js extended @vercel/style-guide and eslint-config-turbo,
 * which were never installed, so linting could not run. This keeps its custom
 * rules on top of eslint-config-next.
 */
export default defineConfig([
  ...nextVitals,
  ...nextTypescript,
  {
    languageOptions: {
      parserOptions: {
        projectService: true
      }
    },
    rules: {
      'import/no-default-export': 'off',
      semi: ['error', 'always'],
      'no-duplicate-imports': ['error', { includeExports: true }],
      complexity: ['error', 10],
      '@typescript-eslint/no-unnecessary-condition': 'error',
      '@typescript-eslint/no-floating-promises': 'error',
      'no-shadow': 'off',
      '@typescript-eslint/no-shadow': 'error',
      'no-void': ['error', { allowAsStatement: true }],
      'require-await': 'off',
      '@typescript-eslint/require-await': ['error'],
      'no-nested-ternary': 'error',
      'react/jsx-props-no-spreading': 'off',
      'import/prefer-default-export': 'off',
      'react-hooks/exhaustive-deps': 'off',
      'no-restricted-syntax': [
        'error',
        'ForInStatement',
        'LabeledStatement',
        'WithStatement'
      ],
      'import/extensions': ['warn', 'never'],
      'react/jsx-sort-props': 'off',
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/no-misused-promises': [
        'error',
        {
          checksVoidReturn: {
            attributes: false
          }
        }
      ]
    }
  },
  globalIgnores(['node_modules/', 'dist/', '.next/', 'next-env.d.ts'])
]);
