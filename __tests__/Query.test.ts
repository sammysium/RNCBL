import {ComparisonOperators, OrderByOptions} from '../src/Constants';
import Query, {SelectResult} from '../src/Query';
import Join from '../src/Join';
import {Sum, Count, Maximum, Minimum, Average} from '../src/functions';
import Expression, {ExpressionDefinition} from '../src/Expression';
import {Dictionary} from '../src/Interfaces';

describe('Query', () => {
  it('should build classic select->where->limit->orderby', () => {
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);

    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .where(exp)
      .limit({limit: 10, offset: 89})
      .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toStrictEqual(exp.expression);
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0},
    ]);
  });

  it('should build classic select->where->limit->orderby with alias', () => {
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);

    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .where(exp)
      .limit({limit: 10, offset: 89})
      .orderBy([
        {
          fieldName: 'gender',
          orderBy: OrderByOptions.asc,
          fromTable: 'employees',
        },
      ]);

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toStrictEqual(exp.expression);
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0, fromTable: 'employees'},
    ]);
  });

  it('should build select->limit->orderby', () => {
    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .limit({limit: 10, offset: 89})
      .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0},
    ]);
  });

  it('should build select->orderby', () => {
    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0},
    ]);
  });

  it('should build select->limit', () => {
    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .limit({limit: 10, offset: 89});

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build classic select->where->limit', () => {
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);

    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .where(exp)
      .limit({limit: 10, offset: 89});

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toStrictEqual(exp.expression);
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build classic select->where->orderby', () => {
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    const query = new Query();
    query
      .select(
        SelectResult.selectField('firstName'),
        SelectResult.selectField('gender'),
      )
      .where(exp)
      .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'firstName', fromTable: ''},
      {fieldName: 'gender', fromTable: ''},
    ]);
    expect(queryResults.where).toStrictEqual(exp.expression);
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0},
    ]);
  });

  it('should build select all', () => {
    const query = new Query();
    query.select();
    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build Aggregates', () => {
    const query = new Query();
    query.select(
      SelectResult.selectField('gender'),
      SelectResult.selectField(Count('id')),
      SelectResult.selectField(Sum('salary')),
      SelectResult.selectField(Average('salary')),
      SelectResult.selectField(Maximum('salary')),
      SelectResult.selectField(Minimum('salary')),
    );

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'gender', fromTable: ''},
      {fieldName: 'count___id', fromTable: ''},
      {fieldName: 'sum___salary', fromTable: ''},
      {fieldName: 'avg___salary', fromTable: ''},
      {fieldName: 'max___salary', fromTable: ''},
      {fieldName: 'min___salary', fromTable: ''},
    ]);
  });

  it('should build select->groupby', () => {
    const query = new Query();
    query.select().groupBy([{fieldName: 'gender'}]);
    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.groupBy).toStrictEqual([{fieldName: 'gender'}]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.having).toBeUndefined();
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build select->groupby->having', () => {
    const query = new Query();
    query
      .select()
      .groupBy(
        [{fieldName: 'gender'}],
        [
          {
            fieldName: 'gender',
            value: 'M',
            comparison: ComparisonOperators.equalsTo,
          },
        ],
      );
    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.groupBy).toStrictEqual([{fieldName: 'gender'}]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.having).toStrictEqual([
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ]);
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toBeUndefined();
  });


  it('should build select->where->groupby', () => {
    const query = new Query();
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    query
      .select()
      .where(exp)
      .groupBy([{fieldName: 'gender', fromTable: 'employee'}]);
    const queryResults = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.groupBy).toStrictEqual([
      {fieldName: 'gender', fromTable: 'employee'},
    ]);
    expect(queryResults.where).toStrictEqual(exp.expression);
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build select->groupby->limit', () => {
    const query = new Query();
    query
      .select()
      .groupBy([{fieldName: 'gender'}])
      .limit({limit: 10, offset: 89});
    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.groupBy).toStrictEqual([{fieldName: 'gender'}]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toStrictEqual({limit: 10, offset: 89});
    expect(queryResults.orderBy).toBeUndefined();
  });

  it('should build select->groupby->order by', () => {
    const query = new Query();
    query
      .select()
      .groupBy([{fieldName: 'gender'}])
      .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);
    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([]);
    expect(queryResults.groupBy).toStrictEqual([{fieldName: 'gender'}]);
    expect(queryResults.where).toBeUndefined();
    expect(queryResults.limit).toBeUndefined();
    expect(queryResults.orderBy).toStrictEqual([
      {fieldName: 'gender', orderBy: 0},
    ]);
  });

  it('should  select with from alias', () => {
    const query = new Query();
    query.select(
      SelectResult.selectField('gender', 'employee'),
      SelectResult.selectField(Sum('salary'), 'group'),
    );

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'gender', fromTable: 'employee'},
      {fieldName: 'sum___salary', fromTable: 'group'},
    ]);
  });

  it('should  select with join', () => {
    const query = new Query();
    query
      .select(
        SelectResult.selectField('gender', 'employee'),
        SelectResult.selectField(Sum('salary'), 'group'),
      )
      .join(
        Join.tables(
          'primaTable',
          'age',
          ComparisonOperators.equalsTo,
          'secondaryTable',
          'age',
        ),
      );

    const queryResults: Dictionary = query.query;
    expect(queryResults.select).toStrictEqual([
      {fieldName: 'gender', fromTable: 'employee'},
      {fieldName: 'sum___salary', fromTable: 'group'},
    ]);

    expect(queryResults.join).toStrictEqual([
      {
        firstTable: 'primaTable',
        firstTableField: 'age',
        comparison: ComparisonOperators.equalsTo,
        secondTable: 'secondaryTable',
        secondTableField: 'age',
      }
    ]);
  });
});
