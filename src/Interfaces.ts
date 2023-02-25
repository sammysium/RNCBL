import {
  ComparisonOperators,
  OrderByOptions,
  Relationship,
  SyncingDirection,
} from './Constants';
import { ExpressionDefinition } from './Expression';

// for batch couchbase lite operations
export interface MultipleProcessedDocumentsStat {
  totalAffectedDocs: number;
  totalRejectedDocs: number;
}

//all our json operations to make sure key is string.
export interface Dictionary {
  [key: string]: any;
}

export interface StringDictionary {
  [key: string]: string;
}

export interface SelectField {
  fieldName: string;
  fromTable?: string;
}

export interface SelectHavingField {
  fieldName: string;
  value: string | number | boolean | null;
  value2?: string | number | boolean | null;
  connectWithNextVia?: Relationship;
  fromTable?: string;
  comparison: ComparisonOperators;
}

//Note: expand value times.
export interface PullPushFilter {
  value: string | number | boolean | null;
  fieldName: string; // in the document
  comparison: ComparisonOperators;
  valueIsDateString?: boolean;
  connectWithNextVia?: Relationship;
}

//internal validation services interface
export interface ValidationResponse {
  ok: boolean;
  message?: string;
  details?: any;
}

//holds Couchbase lite error messages. noe errorDetail might be null.
export interface CBLOperationErrorResponseInterface {
  errorType: string;
  action: string;
  errorDetail: Object;
}

//this is not for status of an going syncing. just if we were able to initiate action
// syncing. if syncingInitiated is true, the operation has started and you can start
// listening to syncing changes.
export interface SyncingInitiationStatusInterface {
  syncingInitiated: boolean;
  syncingInitiatedStatus?: string; //if error initiating, why?
}

// how should we auth ourselves with SyncGateway.
// if basicAuth, provide username and password, which is defaultly checked.
// at least one combo should be given
export interface SyncAuthOption {
  username?: string;
  password?: string;
  sessionId?: string;
}

//inorder to sync, we need to pass the following parameters
export interface SyncingParametersInterface {
  syncURL: string;
  continuous: boolean; // should we continue to sync non-stop?
  syncDirection: SyncingDirection;
  enableTrackingSyncStatus: boolean; // should we care about http status of the syncing operation?
  eventName: string; // if enableTrackingSyncStatus is true, the status will be reported with eventName of the emit
  authOption: SyncAuthOption;
  syncOption?: Dictionary; // not yet implemented fully but will give further options for syncing such as heart beat.
  headers?: StringDictionary[];
  resetCheckPoint?: boolean; //should sync pointer be reset?
  pullFromChannelsOnly?: string[]; //if pull or pullpush, should sync from this channels only,
  filterPull?: PullPushFilter[]; //pull documents of this filter only
  filterPush?: PullPushFilter[]; //push documents of this filter only
  emitPulledDocuments?: boolean; //get an array of the documents taht just got synced down.
  emitPushedDocuments?: boolean; //get an array of the documents that just got synceed up.
}

// what is the status of syncing such as BUSY, ONGOING and affected documents
export interface SyncingOperationStatus {
  status: string;
  affectedDocs: number;
}

//to limit number of results returned by a query
export interface QueryLimit {
  offset?: number;
  limit: number;
}

//to order resultset of a select query
export interface QueryResultSetOrderBy {
  fieldName: string;
  orderBy?: OrderByOptions;
  fromTable?: string;
}

export interface JoinObjectDefn {
  firstTable: string;
  firstTableField: string;
  comparison: ComparisonOperators;
  secondTable: string;
  secondTableField: string;
};

export interface OptionsCopyDocumentsBetweenDatabases {
  omitFields?: string[];
  overWriteExisting?: boolean;
  keepMetaID?: boolean;
  copyDocsOfOnly?: ExpressionDefinition[];
};