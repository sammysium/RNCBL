import {Sum, Count, Average, Maximum, Minimum} from '../src/functions';

describe('Functions', () => {
  it('should return prefixed aggregates', () => {
    const sumSalary = Sum('salary');
    const countId = Count('id');
    const attendance = Average('attendance');
    const maxAge = Maximum('age');
    const minAge = Minimum('age');

    expect(sumSalary).toStrictEqual('sum___salary');
    expect(countId).toStrictEqual('count___id');
    expect(attendance).toStrictEqual('avg___attendance');
    expect(maxAge).toStrictEqual('max___age');
    expect(minAge).toStrictEqual('min___age');
  });
});
