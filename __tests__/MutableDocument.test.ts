import {ValidationResponse} from '../src/Interfaces';
import MutableDocument from '../src/MutableDocument';
import * as validators from '../src/Validators';
import * as transformData from '../src/TransformData';
describe('Document', () => {
  let spyValidateFieldName: any;
  let spyIsDate: any;
  let spyNumberType: any;

  beforeEach(() => {
    spyValidateFieldName = jest.spyOn(validators, 'validateFieldName');
    spyIsDate = jest.spyOn(validators, 'isDate');
    spyNumberType = jest.spyOn(transformData, 'numberType');
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });
  it('should create a document', () => {
    spyValidateFieldName.mockReturnValue(true);
    const validResponse: ValidationResponse = {
      ok: true,
    };
    spyIsDate.mockReturnValue(validResponse);
    const d = new Date();
    const doc = new MutableDocument();
    doc.putString('name', 'semira');
    doc.putNumber('age', 16);
    doc.putDate('registeredOn', d);
    doc.putDictionary('matches', {a: 1});
    doc.putArray('validids', [1, 2, 4]);
    doc.putBoolean('isNew', false);
    doc.putDouble('value', 12345678901993939393);
    doc.putInteger('length', 4);
    doc.putFloat('salary', 300.3);
    doc.putId('1234');
    doc.putType('employee');
    expect(spyValidateFieldName).toHaveBeenCalledTimes(11);
    expect(spyIsDate).toHaveBeenCalledTimes(1);
    expect(spyNumberType).toHaveBeenCalledTimes(3);

    const data = doc.getContent();
    expect(data).toStrictEqual({
      name: 'semira',
      age: 16,
      registeredOn: d,
      matches: {a: 1},
      validids: [1, 2, 4],
      isNew: false,
      type: 'employee',
      value: 12345678901993939393,
      length: 4,
      salary: 300.3,
      id: '1234',
    });
    expect(doc.getFieldValue('length')).toStrictEqual(4);
  });

  it('should reject invalid field names', () => {
    spyValidateFieldName.mockReturnValue(false);
    const doc = new MutableDocument();
    try {
      doc.putString('&&$y', 'data');
      expect(doc.getFieldValue('&&$y')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('invalidFieldName');
    }
  });

  it('should reject invalid document id', () => {
    const doc = new MutableDocument();
    try {
      doc.putId('');
      expect(doc.getFieldValue('id')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('IDEmpty');
    }
  });

  it('should reject invalid float number type', () => {
    spyNumberType.mockReturnValue('int');
    const doc = new MutableDocument();
    try {
      doc.putFloat('salary', 123);
      expect(doc.getFieldValue('salary')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('IncorrectNumberType');
    }
  });

  it('should reject invalid double number type', () => {
    spyNumberType.mockReturnValue('int');
    const doc = new MutableDocument();
    try {
      doc.putDouble('salary', 123);
      expect(doc.getFieldValue('salary')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('IncorrectNumberType');
    }
  });

  it('should reject invalid int number type', () => {
    spyNumberType.mockReturnValue('float');
    const doc = new MutableDocument();
    try {
      doc.putInteger('salary', 123.3);
      expect(doc.getFieldValue('salary')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('IncorrectNumberType');
    }
  });

  it('should reject invalid date type', () => {
    const validResponse: ValidationResponse = {
      ok: false,
      message: 'errorMessage',
    };
    spyIsDate.mockReturnValue(validResponse);
    const doc = new MutableDocument();
    try {
      doc.putDate('dob', new Date());
      expect(doc.getFieldValue('dob')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual(validResponse.message);
    }
  });

  it('should reject invalid array type', () => {
    spyIsDate.mockReturnValue(false);
    const doc = new MutableDocument();
    try {
      doc.putArray('lets', {a: 'b'});
      expect(doc.getFieldValue('lets')).toBeUndefined();
    } catch (error: any) {
      expect(error.message).toStrictEqual('IncorrectArrayType');
    }
  });

  it('should validate document', () => {
    const doc = new MutableDocument();
    const validResponse: ValidationResponse = {
      ok: false,
      message: 'ErrorEmptyDocument',
    };
    expect(doc.isValid()).toStrictEqual(validResponse);

    validResponse.message = 'ErrorDocumentTypeMisssing';

    doc.putString('firstName', 'adaEhi');
    expect(doc.isValid()).toStrictEqual(validResponse);
    doc.putType('employee');
    expect(doc.isValid().ok).toStrictEqual(true);
  });
});
