import {
  Dictionary,
  MultipleProcessedDocumentsStat,
  OptionsCopyDocumentsBetweenDatabases,
  SyncingInitiationStatusInterface,
  SyncingParametersInterface,
} from './Interfaces';
import {LiveQueryOptions} from './Query';

export default interface CBLInterface {
  initialize(dbName: string, indexes: string[]): Promise<boolean>;
  deleteDB(dbName: string, recreateDB: boolean): Promise<boolean>;
  closeDB(dbName: string): Promise<boolean>;
  insertDocument(dbName: string, doc: Dictionary): Promise<string>;
  insertDocuments(
    dbName: string,
    docs: Dictionary[],
  ): Promise<MultipleProcessedDocumentsStat>;
  deleteDocument(dbName: string, documentId: string): Promise<boolean>;
  updateDocument(
    dbName: string,
    documentId: string,
    data: Dictionary,
  ): Promise<boolean>;
  updateDocuments(
    dbName: string,
    whereExp: Array<Dictionary>,
    data: Dictionary,
  ): Promise<MultipleProcessedDocumentsStat>;
  deleteDocuments(
    dbName: string,
    whereExp: Array<Dictionary>,
  ): Promise<MultipleProcessedDocumentsStat>;
  syncDatabase(
    dbName: string,
    syncParameters: SyncingParametersInterface,
  ): Promise<SyncingInitiationStatusInterface>;
  stopSyncing(dbName: string): Promise<boolean>;
  query(
    dbName: string,
    query: Dictionary,
    liveQueryOptions?: LiveQueryOptions,
  ): Promise<Dictionary[]>;
  getDocument(dbName: string, docId: string): Promise<Dictionary>;
  stopLiveQuery(dbName: string, liveQueryName: string): Promise<boolean>;
  deleteIndexes(dbName: string, indexNames: string[]): Promise<boolean>;
  copyDocumentsToDatabase(
    fromDbName: string,
    toDbName: string,
    copyOptions?: OptionsCopyDocumentsBetweenDatabases,
  ): Promise<number>;
};
