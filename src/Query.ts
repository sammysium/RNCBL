import Expression from './Expression';
import {
  Dictionary,
  JoinObjectDefn,
  QueryLimit,
  QueryResultSetOrderBy,
  SelectField,
  SelectHavingField,
} from './Interfaces';

export interface LiveQueryOptions {
  liveQuery: boolean;
  liveQueryName?: string;
}

interface QueryContent {
  select?: SelectResult[];
  join?: JoinObjectDefn[];
  where?: Dictionary;
  limit?: QueryLimit;
  orderBy?: QueryResultSetOrderBy[];
  groupBy?: SelectField[];
  having?: SelectHavingField[];
}

class GroupBy {
  private queryContent: QueryContent = {};
  constructor(
    queryContent: QueryContent,
    d: SelectField[],
    having: SelectHavingField[],
  ) {
    queryContent.groupBy = d;
    if (having.length > 0){
      queryContent.having = having;
    }

    this.queryContent = queryContent;
  }
  limit(limitOptions?: QueryLimit) {
    return new Limit(this.queryContent, limitOptions);
  }

  orderBy(o: QueryResultSetOrderBy[]) {
    return new OrderBy(this.queryContent, o);
  }
}

class OrderBy {
  constructor(queryContent: QueryContent, d: QueryResultSetOrderBy[]) {
    queryContent.orderBy = d;
  }
}

class Limit {
  private queryContent: QueryContent = {};
  constructor(queryContent: QueryContent, limitOptions?: QueryLimit) {
    queryContent.limit = limitOptions;
    this.queryContent = queryContent;
  }

  orderBy(o: QueryResultSetOrderBy[]) {
    return new OrderBy(this.queryContent, o);
  }
}

class Where {
  private queryContent: QueryContent = {};

  constructor(queryContent: QueryContent, expression: Expression) {
    this.queryContent = queryContent;
    queryContent.where = expression.expression;
  }

  limit(limitOptions?: QueryLimit) {
    return new Limit(this.queryContent, limitOptions);
  }

  orderBy(o: QueryResultSetOrderBy[]) {
    return new OrderBy(this.queryContent, o);
  }

  groupBy(o: SelectField[], having: SelectHavingField[] = []) {
    return new GroupBy(this.queryContent, o, having);
  }
}

export class SelectResult {
  static selectField(fieldName: string, fromTable: string = ''){
    return {fieldName, fromTable};
  }
}

class Select {
  private queryContent: QueryContent = {};
  constructor(fields: SelectResult[], queryContent: QueryContent) {
    this.queryContent = queryContent;
    queryContent.select = fields;
  }

  join(...args: JoinObjectDefn[]) {
    this.queryContent.join = args;
    return this;
  }

  where(expression: Expression) {
    return new Where(this.queryContent, expression);
  }

  limit(limitOptions?: QueryLimit) {
    return new Limit(this.queryContent, limitOptions);
  }

  orderBy(o: QueryResultSetOrderBy[]) {
    return new OrderBy(this.queryContent, o);
  }

  groupBy(o: SelectField[], having: SelectHavingField[] = []) {
    return new GroupBy(this.queryContent, o, having);
  }
}

class Query {
  private queryContent: QueryContent = {};

  select(...fields: SelectField[]) {
    return new Select(fields, this.queryContent);
  }

  get query(): QueryContent {
    return this.queryContent;
  }
}

export default Query;
