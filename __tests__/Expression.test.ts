import {ComparisonOperators, Relationship} from '../src/Constants';
import Expression, {ExpressionDefinition} from '../src/Expression';

describe('Expression', () => {
  it('should construct simple expression', () => {
    const json: ExpressionDefinition = {
      fieldName: 'gender',
      value: 'M',
      comparison: ComparisonOperators.equalsTo,
    };
    const exp = new Expression([json]);
    let query = exp.expression;
    expect(query).toHaveLength(1);
    expect(query).toStrictEqual([
      {fieldName: 'gender', value: 'M', comparison: 0},
    ]);
  });

  it('should construct complex expression', () => {
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'm',
        comparison: ComparisonOperators.equalsTo,
        connectWithNextVia: Relationship.or,
      },
      {
        compoundComparison: [
          {
            fieldName: 'age',
            value: 10,
            comparison: ComparisonOperators.equalsTo,
          },
          {
            fieldName: 'age',
            value: 12,
            comparison: ComparisonOperators.equalsTo,
          },
        ],
      },
      {
        compoundComparison: [
          {
            fieldName: 'job',
            value: 'IT',
            comparison: ComparisonOperators.equalsTo,
          },
          {
            fieldName: 'year',
            value: 2019,
            comparison: ComparisonOperators.equalsTo,
            compoundComparison: [
              {
                fieldName: 'name',
                value: 'winnie',
                comparison: ComparisonOperators.equalsTo,
              },
              {
                fieldName: 'country',
                value: 'Kenya',
                comparison: ComparisonOperators.equalsTo,
                compoundComparison: [
                  {
                    fieldName: 'friend',
                    value: 'Emile',
                    comparison: ComparisonOperators.equalsTo,
                  },
                  {
                    fieldName: 'friend',
                    value: 'Frank',
                    comparison: ComparisonOperators.equalsTo,
                  },
                ],
              },
            ],
          },
        ],
      },
    ];
    const exp = new Expression(json);
    let query = exp.expression;
    expect(query).toHaveLength(3);
  });

  it('should construct aliased expression', () => {
    const json: ExpressionDefinition = {
      fieldName: 'gender',
      value: 'M',
      comparison: ComparisonOperators.equalsTo,
      fromTable: 'employee',
    };
    const exp = new Expression([json]);
    let query = exp.expression;
    expect(query).toHaveLength(1);
    expect(query).toStrictEqual([
      {fieldName: 'gender', value: 'M', comparison: 0, fromTable: 'employee'},
    ]);
  });
});
