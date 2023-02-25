import {ComparisonOperators, Relationship} from './Constants';
import {Dictionary} from './Interfaces';

export interface ExpressionDefinition {
  fieldName?: string;
  value?: string | number | Date | Array<any> | Dictionary;
  value2?: number | Date;
  comparison?: ComparisonOperators;
  connectWithNextVia?: Relationship;
  compoundComparison?: CompoundComparison[] | null;
  fromTable?: string;
}

interface CompoundComparison {
  fieldName: string;
  value:
    | string
    | number
    | Date
    | Array<string | number | Date | Array<any>>
    | Dictionary;
  value2?: number | Date;
  comparison: ComparisonOperators;
  connectWithNextVia?: Relationship;
  compoundComparison?: CompoundComparison[] | null;
  fromTable?: string;
}

class Expression {
  private data: ExpressionDefinition[] = [];

  constructor(exp: ExpressionDefinition[]) {
    this.data = exp;
  }

  get expression(): ExpressionDefinition[] {
    return this.data;
  }
}

export default Expression;
