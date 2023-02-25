import Join from '../src/Join';
import {JoinObjectDefn} from '../src/Interfaces';
import {ComparisonOperators} from '../src/Constants';

describe('Join', () => {
  it('should return join object', () => {
    const joinInfo: JoinObjectDefn = Join.tables(
      'table1',
      'age',
      ComparisonOperators.equalsTo,
      'table2',
      'age',
    );
    expect(joinInfo).toStrictEqual({
      firstTable: 'table1',
      firstTableField: 'age',
      comparison: 0,
      secondTable: 'table2',
      secondTableField: 'age',
    });
  });
});
