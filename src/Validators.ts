import {ValidationResponse} from './Interfaces';

export function validateFieldName(fieldName: string): boolean {
  const pattern = /^([A-Za-z]|[0-9]|_){1,20}$/g;
  return pattern.test(fieldName);
}

export function validateDBName(dbName: string): boolean {
  const pattern = /^([A-Za-z]|[0-9]|_|-){2,20}$/g;
  return pattern.test(dbName);
}

export function isDate(date: any): ValidationResponse {
  const reply: ValidationResponse = {
    ok: Object.prototype.toString.call(date) === '[object Date]',
    message: 'invalidDate',
  };
  return reply;
}
