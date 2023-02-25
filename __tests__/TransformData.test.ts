import {numberType} from '../src/TransformData';

describe('transform data', () => {
  it('should return type of number', () => {
    expect(numberType('')).toStrictEqual('');
    expect(numberType(1)).toStrictEqual('int');
    expect(numberType(123456789)).toStrictEqual('int');
    expect(numberType(1.23)).toStrictEqual('float');
    expect(numberType(123456789012346789008765)).toStrictEqual('double');
  });
});
