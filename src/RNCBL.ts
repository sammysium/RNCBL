import {NativeModules} from 'react-native';
import CBLInterface from './CBLInterface';
import MutableDocument from './MutableDocument';
import {
  Dictionary,
  MultipleProcessedDocumentsStat,
  OptionsCopyDocumentsBetweenDatabases,
  SyncingInitiationStatusInterface,
  SyncingParametersInterface,
  ValidationResponse,
} from './Interfaces';
import Expression from './Expression';
import Query, {LiveQueryOptions} from './Query';

export default class RNCBL {
  private cblNative: CBLInterface;
  private dbName: string = '';
  private syncingAttempt: SyncingInitiationStatusInterface = {
    syncingInitiated: false,
  };

  private syncingParameters?: SyncingParametersInterface;

  constructor(dbName: string) {
    this.dbName = dbName;
    this.cblNative = NativeModules.RNCBL;
  }

  async initialize(indexes: string[]) {
    try {
      //grrrrrrrrrr. not sure why await is required here!
      return await this.cblNative.initialize(this.dbName, indexes);
    } catch (error) {
      throw new Error(error);
    }
  }

  async closeDB() {
    try {
      return await this.cblNative.closeDB(this.dbName);
    } catch (error) {
      throw new Error(error);
    }
  }

  async deleteDB(recreateDB: boolean = true) {
    try {
      return await this.cblNative.deleteDB(this.dbName, recreateDB);
    } catch (error) {
      throw new Error(error);
    }
  }

  async insertMutableDocument(document: MutableDocument) {
    //validate document first. returns document id of the inserted Document
    try {
      const isValidDocument: ValidationResponse = document.isValid();
      if (isValidDocument.ok) {
        return await this.cblNative.insertDocument(
          this.dbName,
          document.getContent(),
        );
      }
      throw new Error(isValidDocument.message);
    } catch (error) {
      throw new Error(error);
    }
  }

  async insertJSONDocument(data: Dictionary): Promise<string> {
    /*
      add one document. data is a JSON
    */
    try {
      return await this.cblNative.insertDocument(this.dbName, data);
    } catch (error) {
      throw new Error(error);
    }
  }

  async insertJSONDocuments(
    data: Dictionary[],
  ): Promise<MultipleProcessedDocumentsStat> {
    //inserts JSON documents
    try {
      return await this.cblNative.insertDocuments(this.dbName, data);
    } catch (error) {
      throw new Error(error);
    }
  }

  async deleteDocument(documentId: string): Promise<boolean> {
    try {
      return await this.cblNative.deleteDocument(this.dbName, documentId);
    } catch (error) {
      throw new Error(error);
    }
  }

  async updateDocument(
    documentId: string,
    updatedInfo: Dictionary,
  ): Promise<boolean> {
    try {
      return await this.cblNative.updateDocument(
        this.dbName,
        documentId,
        updatedInfo,
      );
    } catch (error) {
      throw new Error(error);
    }
  }

  async updateDocuments(
    whereExp: Expression,
    updatedInfo: Dictionary,
  ): Promise<MultipleProcessedDocumentsStat> {
    try {
      return await this.cblNative.updateDocuments(
        this.dbName,
        whereExp.expression,
        updatedInfo,
      );
    } catch (error) {
      throw new Error(error);
    }
  }

  async deleteDocuments(
    whereExp: Expression,
  ): Promise<MultipleProcessedDocumentsStat> {
    try {
      return await this.cblNative.deleteDocuments(
        this.dbName,
        whereExp.expression,
      );
    } catch (error) {
      throw new Error(error);
    }
  }

  async insertMutableDocuments(
    documents: MutableDocument[],
  ): Promise<MultipleProcessedDocumentsStat> {
    /*
      inserts multiple documents to the database
    */
    try {
      const data: any[] = [];
      // validate documents
      const total = documents.length;
      for (let i = 0; i < total; i++) {
        const document: MutableDocument = documents[i];
        // validate it now
        const isValidDocument: ValidationResponse = document.isValid();
        if (isValidDocument.ok === false) {
          throw isValidDocument;
        }
        data.push(document.getContent());
      }
      return await this.cblNative.insertDocuments(this.dbName, data);
    } catch (error) {
      throw new Error(error);
    }
  }

  async syncDatabase(
    syncParameters: SyncingParametersInterface,
  ): Promise<SyncingInitiationStatusInterface> {
    // confirm auth is well set first.
    if (
      (syncParameters.authOption.username &&
        syncParameters.authOption.password) ||
      syncParameters.authOption.sessionId
    ) {
      // auth given.
      this.syncingParameters = syncParameters;
      this.syncingAttempt = await this.cblNative.syncDatabase(
        this.dbName,
        syncParameters,
      );
      return this.syncingAttempt;
    }
    throw new Error('syncOptionNotGiven');
  }

  syncingState(): SyncingInitiationStatusInterface {
    return this.syncingAttempt;
  }

  async stopSyncingDatabase(): Promise<boolean> {
    try {
      await this.cblNative.stopSyncing(this.dbName);
      this.syncingAttempt.syncingInitiated = false;
      return true;
    } catch (error) {
      throw new Error(error);
    }
  }

  async query(
    query: Query,
    liveQueryOptions: LiveQueryOptions,
  ): Promise<Dictionary[]> {
    /*
      returns an array of objects like [{...}]
    */
    try {
      if (!query.query.select) {
        throw new Error('mustCallSelectFirst');
      }
      if (query.query.join && query.query.groupBy && !query.query.where) {
        throw new Error('joinWithGroupByMustHaveWhereClause');
      }
      if (liveQueryOptions.liveQuery && !liveQueryOptions.liveQueryName) {
        throw new Error('liveQueryMustHaveUniqueName');
      }
      return await this.cblNative.query(
        this.dbName,
        query.query,
        liveQueryOptions,
      );
    } catch (error) {
      throw new Error(error);
    }
  }

  async getDocument(documentId: string): Promise<Dictionary> {
    try {
      return await this.cblNative.getDocument(this.dbName, documentId);
    } catch (error) {
      throw new Error(error);
    }
  }

  async stopLiveQuery(liveQueryName: string): Promise<boolean> {
    try {
      return await this.cblNative.stopLiveQuery(this.dbName, liveQueryName);
    } catch (error) {
      throw new Error(error);
    }
  }

  async deleteIndexes(indexNames: string[]): Promise<boolean> {
    return this.cblNative.deleteIndexes(this.dbName, indexNames);
  }

  async copyDocumentsToDatabase(
    toDatabase: RNCBL,
    copyOptions?: OptionsCopyDocumentsBetweenDatabases
  ): Promise<number> {
    return this.cblNative.copyDocumentsToDatabase(
      this.dbName,
      toDatabase.dbName,
      copyOptions,
    );
  }
}
