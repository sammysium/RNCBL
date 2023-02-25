import {
  validateFieldName,
  validateDBName,
  validateDocument,
  isDate,
} from '../src/Validators';
import MutableDocument from '../src/MutableDocument';
import {ValidationResponse} from '../src/Interfaces';

describe('Validators', () => {
  it('should validate Field Name', () => {
    expect(validateFieldName('abc')).toStrictEqual(true);
    expect(validateFieldName('123')).toStrictEqual(true);
    expect(validateFieldName('abc123')).toStrictEqual(true);
    expect(validateFieldName('_')).toStrictEqual(true);
    expect(validateFieldName('_Ab_32c')).toStrictEqual(true);
    expect(validateFieldName('abc_123_a')).toStrictEqual(true);
    expect(validateFieldName('abc-')).toStrictEqual(false);
    expect(validateFieldName('')).toStrictEqual(false);
    expect(validateFieldName('&firstName')).toStrictEqual(false);
    expect(validateFieldName('abcdefghijklmnopqrstioppp')).toStrictEqual(false);
  });

  it('should validate DB Name', () => {
    expect(validateDBName('abc')).toStrictEqual(true);
    expect(validateDBName('123')).toStrictEqual(true);
    expect(validateDBName('abc123')).toStrictEqual(true);
    expect(validateDBName('_A')).toStrictEqual(true);
    expect(validateDBName('_Ab_32c-E')).toStrictEqual(true);
    expect(validateDBName('abc_123_a')).toStrictEqual(true);
    expect(validateDBName('abc-&&')).toStrictEqual(false);
    expect(validateDBName('')).toStrictEqual(false);
    expect(validateFieldName('&dbName')).toStrictEqual(false);
    expect(validateDBName('abcdefghijklmnopqrstioppp')).toStrictEqual(false);
  });


  it('should check if date', () => {
    const validResponse: ValidationResponse = {
      ok: false,
      message: 'invalidDate',
    };
    expect(isDate(new Date()).ok).toStrictEqual(true);
    expect(isDate('2017-09-09')).toStrictEqual(validResponse);
    expect(isDate('')).toStrictEqual(validResponse);
  });

});
