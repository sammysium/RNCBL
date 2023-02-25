import {Dictionary, ValidationResponse} from './Interfaces';
import {numberType} from './TransformData';
import {isDate, validateFieldName} from './Validators';
export default class MutableDocument {


  private content: Dictionary = {};

  getContent(): Dictionary {
    return this.content;
  }

  getFieldValue(fieldName: string) {
    return this.content[fieldName];
  }
  private setData(fieldName: string, value: any) {
    if (validateFieldName(fieldName)) {
      this.content[fieldName] = value;
    } else {
      throw new Error('invalidFieldName');
    }
  }

  private validateDocumentID(id: string): ValidationResponse {
    /*
      if given, validate the document id is a valid string
    */
    const reply: ValidationResponse = {
      ok: true,
      message: '',
    };
    if (id.length === 0) {
      reply.message = 'IDEmpty';
      reply.ok = false;
    }
    return reply;
  }

  isValid(): ValidationResponse {
    const validationResult: ValidationResponse = {
      ok: false,
    };
    const fieldNames: string[] = Object.keys(this.content);
    const totalFields = fieldNames.length;
    if (totalFields === 0) {
      validationResult.message = 'ErrorEmptyDocument';
      return validationResult;
    }

    if (fieldNames.indexOf('type') === -1) {
      validationResult.message = 'ErrorDocumentTypeMisssing';
      return validationResult;
    }
    validationResult.ok = true;
    return validationResult;
  }

  putBoolean(fieldName: string, value: boolean) {
    this.setData(fieldName, value);
  }

  putType(value: string) {
    this.setData('type', value);
  }

  putId(value: string) {
    const v = value.trim();
    const validation: ValidationResponse = this.validateDocumentID(v);
    if (validation.ok) {
      this.setData('id', v);
    } else {
      throw new Error(validation.message);
    }
  }

  putString(fieldName: string, value: string) {
    this.setData(fieldName, value.trim());
  }

  putNumber(fieldName: string, value?: number) {
    this.setData(fieldName, value);
  }

  putFloat(fieldName: string, value: number) {
    if (numberType(value) === 'float') {
      this.setData(fieldName, value);
    } else {
      throw new Error('IncorrectNumberType');
    }
  }

  putInteger(fieldName: string, value: number) {
    if (numberType(value) === 'int') {
      this.setData(fieldName, value);
    } else {
      throw new Error('IncorrectNumberType');
    }
  }

  putDouble(fieldName: string, value: number) {
    if (numberType(value) === 'double') {
      this.setData(fieldName, value);
    } else {
      throw new Error('IncorrectNumberType');
    }
  }

  putDictionary(fieldName: string, value: Dictionary) {
    this.setData(fieldName, value);
  }

  putArray(fieldName: string, value: Object) {
    if (Array.isArray(value)) {
      this.setData(fieldName, value);
    } else {
      throw new Error('IncorrectArrayType');
    }
  }

  putDate(fieldName: string, value: Date) {
    const validation: ValidationResponse = isDate(value);
    if (validation.ok) {
      this.setData(fieldName, value);
    } else {
      throw new Error(validation.message);
    }
  }
}
