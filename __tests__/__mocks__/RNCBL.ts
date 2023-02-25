/* eslint-disable @typescript-eslint/no-unused-vars */
import {NativeModules} from 'react-native';
import {
  Dictionary,
  MultipleProcessedDocumentsStat,
  SyncingInitiationStatusInterface,
  SyncingParametersInterface,
} from '../../src/Interfaces';

import {LiveQueryOptions} from '../../src/Query';

NativeModules.RNCBL = {
  initialize: (dbName: string, indexes: string[]) => {
    return Promise.resolve(true);
  },
  deleteDB: (dbName: string, recreateDB: boolean) => {
    return Promise.resolve(true);
  },
  closeDB: (dbName: string) => {
    return Promise.resolve(true);
  },
  insertDocument: (dbName: string, doc: Dictionary) => {
    return Promise.resolve('123');
  },
  insertDocuments: (dbName: string, docs: Dictionary[]) => {
    const reply: MultipleProcessedDocumentsStat = {
      totalRejectedDocs: 1,
      totalAffectedDocs: 2,
    };
    return Promise.resolve(reply);
  },
  deleteDocument: (dbName: string, documentId: string) => {
    return Promise.resolve(true);
  },
  updateDocument: (dbName: string, documentId: string, data: Dictionary) => {
    return Promise.resolve(true);
  },
  updateDocuments: (
    dbName: string,
    docsLimit: Array<Dictionary>,
    data: Dictionary,
  ) => {
    const reply: MultipleProcessedDocumentsStat = {
      totalRejectedDocs: 1,
      totalAffectedDocs: 2,
    };
    return Promise.resolve(reply);
  },
  deleteDocuments: (dbName: string, docsLimit: Array<Dictionary>) => {
    const reply: MultipleProcessedDocumentsStat = {
      totalRejectedDocs: 0,
      totalAffectedDocs: 2,
    };
    return Promise.resolve(reply);
  },
  syncDatabase: (
    dbName: string,
    syncParameters: SyncingParametersInterface,
  ) => {
    const reply: SyncingInitiationStatusInterface = {
      syncingInitiated: true,
      syncingInitiatedStatus: '',
    };
    return Promise.resolve(reply);
  },
  stopSyncing: (dbName: string) => {
    return Promise.resolve(true);
  },
  query: (
    dbName: string,
    query: Dictionary,
    liveQueryOptions?: LiveQueryOptions,
  ) => {
    const results: Dictionary[] = [];
    return Promise.resolve(results);
  },
  getDocument: (dbName: string, docId: string) => {
    const result: Dictionary = {name: 'Daniel'};
    return Promise.resolve(result);
  },
  stopLiveQuery: (dbName: string, liveQueryName: string) => {
    return Promise.resolve(true);
  },
  deleteIndexes(dbName: string, indexNames: string[]): Promise<boolean> {
    return Promise.resolve(true);
  },
  copyDocumentsToDatabase(
    fromDbName: string,
    toDbName: string,
    copyDocsOfOnly: Dictionary,
    omitFields: string[],
    overWriteExisting: boolean,
    keepMetaID: boolean,
  ): Promise<number> {
    return Promise.resolve(1);
  },
};
