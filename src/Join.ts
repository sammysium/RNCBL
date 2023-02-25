import {ComparisonOperators} from './Constants';
import {JoinObjectDefn} from './Interfaces';

export default class Join {
  static tables(
    firstTable: string,
    firstTableField: string,
    comparison: ComparisonOperators,
    secondTable: string,
    secondTableField: string,
  ): JoinObjectDefn {
    return {
      firstTable,
      firstTableField,
      comparison,
      secondTable,
      secondTableField,
    };
  }
}
