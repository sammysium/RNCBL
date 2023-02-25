import {NativeModules} from 'react-native';
import MutableDocument from '../src/MutableDocument';
import RNCBL from '../src/RNCBL';
import {
  Dictionary,
  MultipleProcessedDocumentsStat,
  ValidationResponse,
  CBLOperationErrorResponseInterface,
  SyncingInitiationStatusInterface,
  SyncAuthOption,
  SyncingParametersInterface,
  OptionsCopyDocumentsBetweenDatabases,
} from '../src/Interfaces';

import Expression, {ExpressionDefinition} from '../src/Expression';
import {ComparisonOperators, SyncingDirection} from '../src/Constants';
import Query, {SelectResult} from '../src/Query';
import Join from '../src/Join';

describe('RNCBL', () => {
  let spyInitDB: any;
  let spyDeleteDB: any;
  let spyCloseDB: any;
  let spyinsertDocument: any;
  let spyInsertDocuments: any;
  let spyDeleteDocument: any;
  let spyUpdateDocument: any;
  let spyUpdateDocuments: any;
  let spyDeleteDocuments: any;
  let spySyncDatabase: any;
  let spyStopSyncing: any;
  let spyQuery: any;
  let spyGetDocument: any;
  let spyStopLiveQuery: any;
  let spyDeleteIndexes: any;
  let spyCopyDocumentsToDatabase: any;

  const testDBName = 'RNCBL_TEST_DB';

  beforeEach(() => {
    spyInitDB = jest.spyOn(NativeModules.RNCBL, 'initialize');
    spyDeleteDB = jest.spyOn(NativeModules.RNCBL, 'deleteDB');
    spyCloseDB = jest.spyOn(NativeModules.RNCBL, 'closeDB');
    spyinsertDocument = jest.spyOn(NativeModules.RNCBL, 'insertDocument');
    spyInsertDocuments = jest.spyOn(NativeModules.RNCBL, 'insertDocuments');
    spyDeleteDocument = jest.spyOn(NativeModules.RNCBL, 'deleteDocument');
    spyUpdateDocument = jest.spyOn(NativeModules.RNCBL, 'updateDocument');
    spyUpdateDocuments = jest.spyOn(NativeModules.RNCBL, 'updateDocuments');
    spyDeleteDocuments = jest.spyOn(NativeModules.RNCBL, 'deleteDocuments');
    spySyncDatabase = jest.spyOn(NativeModules.RNCBL, 'syncDatabase');
    spyStopSyncing = jest.spyOn(NativeModules.RNCBL, 'stopSyncing');
    spyQuery = jest.spyOn(NativeModules.RNCBL, 'query');
    spyGetDocument = jest.spyOn(NativeModules.RNCBL, 'getDocument');
    spyStopLiveQuery = jest.spyOn(NativeModules.RNCBL, 'stopLiveQuery');
    spyDeleteIndexes = jest.spyOn(NativeModules.RNCBL, 'deleteIndexes');
    spyCopyDocumentsToDatabase = jest.spyOn(
      NativeModules.RNCBL,
      'copyDocumentsToDatabase',
    );
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  it('should initialize database', async () => {
    spyInitDB.mockResolvedValue(true);
    const rncbl = new RNCBL(testDBName);
    try {
      const result = await rncbl.initialize(['employeeId']);
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
  });

  it('should initialize database - handle error', async () => {
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'initDB',
      errorDetail: 'Error creating db',
    };
    spyInitDB.mockRejectedValue(error);
    const rncbl = new RNCBL(testDBName);

    await expect(() => rncbl.initialize(['employeeId'])).rejects.toThrow(
      new Error(error),
    );
    expect(spyInitDB).toHaveBeenCalledWith(testDBName, ['employeeId']);
  });

  it('should delete the database - no recreating db', async () => {
    spyDeleteDB.mockResolvedValue(true);
    const rncbl = new RNCBL(testDBName);

    try {
      const result = await rncbl.deleteDB(false);
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyDeleteDB).toHaveBeenCalledWith(testDBName, false);
  });

  it('should delete the database - recreating db', async () => {
    spyDeleteDB.mockResolvedValue(true);
    const rncbl = new RNCBL(testDBName);

    try {
      const result = await rncbl.deleteDB();
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyDeleteDB).toHaveBeenCalledWith(testDBName, true);
  });

  it('should delete the database - handle error', async () => {
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'deleteDB',
      errorDetail: 'Error deleting db',
    };
    spyDeleteDB.mockRejectedValue(error);
    const rncbl = new RNCBL(testDBName);

    await expect(() => rncbl.deleteDB()).rejects.toThrow(new Error(error));
    expect(spyDeleteDB).toHaveBeenCalledWith(testDBName, true);
  });

  it('should close the database', async () => {
    spyCloseDB.mockResolvedValue(true);
    const rncbl = new RNCBL(testDBName);

    try {
      const result = await rncbl.closeDB();
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyCloseDB).toHaveBeenCalledWith(testDBName);
  });

  it('should close the database - handle error', async () => {
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'closeDB',
      errorDetail: 'Error closing db',
    };
    spyCloseDB.mockRejectedValue(error);
    const rncbl = new RNCBL(testDBName);
    await expect(() => rncbl.closeDB()).rejects.toThrow(new Error(error));
    expect(spyCloseDB).toHaveBeenCalledWith(testDBName);
  });

  it('should add one JSON document to the database', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    const doc: Dictionary = {
      id: docId,
      name: 'Semira',
    };
    spyinsertDocument.mockResolvedValue(docId);

    try {
      const result = await rncbl.insertJSONDocument(doc);
      expect(result).toStrictEqual(docId);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyinsertDocument).toHaveBeenCalledWith(testDBName, doc);
  });

  it('should save single Json document the database - handle error', async () => {
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'insertDocumentument',
      errorDetail: 'Error saving document',
    };
    const doc: Dictionary = {
      id: '124',
      name: 'elim',
    };
    spyinsertDocument.mockRejectedValue(error);
    const rncbl = new RNCBL(testDBName);
    await expect(() => rncbl.insertJSONDocument(doc)).rejects.toThrow(
      new Error(error),
    );
    expect(spyinsertDocument).toHaveBeenCalledWith(testDBName, doc);
  });

  it('should add one mutable document to the database - valid document', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    spyinsertDocument.mockResolvedValue(docId);
    const doc = new MutableDocument();
    doc.putType('employee');
    doc.putString('name', 'emi');

    try {
      const result = await rncbl.insertMutableDocument(doc);
      expect(result).toStrictEqual(docId);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyinsertDocument).toHaveBeenCalledWith(
      testDBName,
      doc.getContent(),
    );
  });

  it('should save single mutable document the database - handle error', async () => {
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'insertDocumentument',
      errorDetail: 'Error saving document',
    };
    const doc = new MutableDocument();
    doc.putType('employee');
    doc.putString('name', 'emi');
    spyinsertDocument.mockRejectedValue(error);
    const rncbl = new RNCBL(testDBName);
    await expect(() => rncbl.insertMutableDocument(doc)).rejects.toThrow(
      new Error(error),
    );
    expect(spyinsertDocument).toHaveBeenCalledWith(
      testDBName,
      doc.getContent(),
    );
  });

  it('should save single mutable document the database - reject invalid document', async () => {
    const error: ValidationResponse = {
      message: 'ErrorEmptyDocument',
      ok: false,
    };
    const doc = new MutableDocument();
    spyinsertDocument.mockResolvedValue('true');
    const rncbl = new RNCBL(testDBName);
    await expect(() => rncbl.insertMutableDocument(doc)).rejects.toThrow(
      new Error(`Error: ${error.message}`),
    );
    expect(spyinsertDocument).not.toHaveBeenCalled();
  });

  it('should add JSON multiple documents to the database', async () => {
    const rncbl = new RNCBL(testDBName);
    const docResult: MultipleProcessedDocumentsStat = {
      totalRejectedDocs: 1,
      totalAffectedDocs: 5,
    };
    const documents: Dictionary[] = [];
    const doc1: Dictionary = {
      name: 'semira',
      isNew: false,
      salary: 300.3,
      type: 'employee',
      id: '1234',
    };

    const doc2: Dictionary = {
      name: 'napoleon',
      isNew: false,
      salary: 100,
      type: 'employee',
      id: '12345',
    };
    documents.push(doc1);
    documents.push(doc2);
    spyInsertDocuments.mockResolvedValue(docResult);

    try {
      const result = await rncbl.insertJSONDocuments(documents);
      expect(result).toStrictEqual(docResult);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyInsertDocuments).toHaveBeenCalledWith(testDBName, documents);
  });

  it('should add multiple JSON documents to the database  - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const error: CBLOperationErrorResponseInterface = {
      errorType: 'logic',
      action: 'insertJSONDocuments',
      errorDetail: 'Error saving multiple documents',
    };
    const documents: Dictionary[] = [];
    const doc1: Dictionary = {
      name: 'semira',
      isNew: false,
      salary: 300.3,
      type: 'employee',
      id: '1234',
    };

    const doc2: Dictionary = {
      name: 'napoleon',
      isNew: false,
      salary: 100,
      type: 'employee',
      id: '12345',
    };

    documents.push(doc1);
    documents.push(doc2);
    spyInsertDocuments.mockRejectedValue(error);

    await expect(() => rncbl.insertJSONDocuments(documents)).rejects.toThrow(
      new Error(error),
    );
    expect(spyInsertDocuments).toHaveBeenCalledWith(testDBName, documents);
  });

  it('should delete document', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    spyDeleteDocument.mockResolvedValue(true);

    try {
      const result = await rncbl.deleteDocument(docId);
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyDeleteDocument).toHaveBeenCalledWith(testDBName, docId);
  });

  it('should delete document - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    spyDeleteDocument.mockRejectedValue('error');

    await expect(() => rncbl.deleteDocument(docId)).rejects.toThrow(
      new Error('error'),
    );
    expect(spyDeleteDocument).toHaveBeenCalledWith(testDBName, docId);
  });

  it('should update document', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    const newData: Dictionary = {
      name: 'emi',
    };

    spyUpdateDocument.mockResolvedValue(true);

    try {
      const result = await rncbl.updateDocument(docId, newData);
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyUpdateDocument).toHaveBeenCalledWith(testDBName, docId, newData);
  });

  it('should update document - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    const newData: Dictionary = {
      name: 'emi',
    };
    spyUpdateDocument.mockRejectedValue('error');

    await expect(() => rncbl.updateDocument(docId, newData)).rejects.toThrow(
      new Error('error'),
    );
    expect(spyUpdateDocument).toHaveBeenCalledWith(testDBName, docId, newData);
  });

  it('should update documents', async () => {
    const reply: Dictionary = {
      totalAffectedDocs: 1,
      totalRejectedDocs: 1,
    };
    const rncbl = new RNCBL(testDBName);
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    const newData: Dictionary = {
      name: 'emi',
    };
    spyUpdateDocuments.mockResolvedValue(reply);

    try {
      const result = await rncbl.updateDocuments(exp, newData);
      expect(result).toStrictEqual(reply);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyUpdateDocuments).toHaveBeenCalledWith(
      testDBName,
      exp.expression,
      newData,
    );
  });

  it('should update documents - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    const newData: Dictionary = {
      name: 'emi',
    };
    spyUpdateDocuments.mockRejectedValue('error');

    await expect(() => rncbl.updateDocuments(exp, newData)).rejects.toThrow(
      new Error('error'),
    );

    expect(spyUpdateDocuments).toHaveBeenCalledWith(
      testDBName,
      exp.expression,
      newData,
    );
  });

  it('should delete documents', async () => {
    const reply: Dictionary = {
      totalAffectedDocs: 1,
      totalRejectedDocs: 1,
    };
    const rncbl = new RNCBL(testDBName);
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    spyDeleteDocuments.mockResolvedValue(reply);

    try {
      const result = await rncbl.deleteDocuments(exp);
      expect(result).toStrictEqual(reply);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyDeleteDocuments).toHaveBeenCalledWith(testDBName, exp.expression);
  });

  it('should delete documents - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    const exp = new Expression(json);
    spyDeleteDocuments.mockRejectedValue('error');

    await expect(() => rncbl.deleteDocuments(exp)).rejects.toThrow(
      new Error('error'),
    );

    expect(spyDeleteDocuments).toHaveBeenCalledWith(testDBName, exp.expression);
  });

  it('should insert multiple mutable documents - valid documents', async () => {
    const documents: MutableDocument[] = [];
    const doc1: MutableDocument = new MutableDocument();
    doc1.putString('name', 'Semira');
    doc1.putString('nationality', 'Nigerian');
    doc1.putInteger('age', 30);
    doc1.putType('people');
    const doc2: MutableDocument = new MutableDocument();
    doc2.putString('name', 'Feez');
    doc2.putString('nationality', 'Nigerian');
    doc2.putInteger('age', 4);
    doc2.putType('people');
    documents.push(doc1);
    documents.push(doc2);
    const rncbl = new RNCBL(testDBName);
    const result: MultipleProcessedDocumentsStat = {
      totalAffectedDocs: 2,
      totalRejectedDocs: 0,
    };
    spyInsertDocuments.mockResolvedValue(result);
    try {
      const docsAffected: MultipleProcessedDocumentsStat =
        await rncbl.insertMutableDocuments(documents);
      expect(docsAffected).toStrictEqual(result);
    } catch (error) {
      expect(error).toBeNull();
    }
    expect(spyInsertDocuments).toHaveBeenCalledWith(testDBName, [
      doc1.getContent(),
      doc2.getContent(),
    ]);
  });

  it('should insert multiple mutable documents - handle error', async () => {
    const documents: MutableDocument[] = [];
    const doc1: MutableDocument = new MutableDocument();
    doc1.putString('name', 'Semira');
    doc1.putString('nationality', 'Nigerian');
    doc1.putInteger('age', 30);
    doc1.putType('employee');
    documents.push(doc1);

    const rncbl = new RNCBL(testDBName);
    spyInsertDocuments.mockRejectedValue('error');

    await expect(() => rncbl.insertMutableDocuments(documents)).rejects.toThrow(
      new Error('error'),
    );

    expect(spyInsertDocuments).toHaveBeenCalledWith(testDBName, [
      doc1.getContent(),
    ]);
  });

  it('should not insert multiple mutable documents - invalid documents', async () => {
    const validationResponse: ValidationResponse = {
      ok: false,
    };

    const documents: MutableDocument[] = [];
    const doc1: MutableDocument = new MutableDocument();
    doc1.putString('name', 'Semira');
    doc1.putString('nationality', 'Nigerian');
    doc1.putInteger('age', 30);
    documents.push(doc1);
    const doc2: MutableDocument = new MutableDocument();
    doc2.putType('employee');
    doc2.putString('name', 'jon');
    documents.push(doc2);
    const rncbl = new RNCBL(testDBName);
    await expect(() => rncbl.insertMutableDocuments(documents)).rejects.toThrow(
      new Error(validationResponse),
    );
    expect(spyInsertDocuments).not.toHaveBeenCalled();
  });

  it('should start syncing database', async () => {
    const rncbl = new RNCBL(testDBName);
    expect(rncbl.syncingState().syncingInitiated).toStrictEqual(false);
    expect(rncbl.syncingState().syncingInitiatedStatus).toBeUndefined();
    const reply: SyncingInitiationStatusInterface = {
      syncingInitiated: true,
      syncingInitiatedStatus: '',
    };
    const syncAuthOption: SyncAuthOption = {
      username: 'username',
      password: 'password',
    };
    const syncParameters: SyncingParametersInterface = {
      syncURL: 'wss://abc.net',
      continuous: true,
      enableTrackingSyncStatus: true,
      syncDirection: SyncingDirection.pull,
      eventName: 'syncingStatus',
      authOption: syncAuthOption,
    };
    spySyncDatabase.mockResolvedValue(reply);
    const attempt: SyncingInitiationStatusInterface = await rncbl.syncDatabase(
      syncParameters,
    );
    expect(attempt).toStrictEqual(reply);
    expect(rncbl.syncingState().syncingInitiated).toStrictEqual(true);
    expect(rncbl.syncingState().syncingInitiatedStatus).toStrictEqual('');
    expect(spySyncDatabase).toHaveBeenCalledWith(testDBName, syncParameters);
  });

  it('should not start syncing database - no auth', async () => {
    const rncbl = new RNCBL(testDBName);
    const syncAuthOption: SyncAuthOption = {};
    const syncParameters: SyncingParametersInterface = {
      syncURL: 'wss://abc.net',
      continuous: true,
      enableTrackingSyncStatus: true,
      syncDirection: SyncingDirection.pull,
      eventName: 'syncingStatus',
      authOption: syncAuthOption,
    };

    await expect(() => rncbl.syncDatabase(syncParameters)).rejects.toThrow(
      new Error('syncOptionNotGiven'),
    );

    expect(rncbl.syncingState().syncingInitiated).toStrictEqual(false);
    expect(spySyncDatabase).not.toHaveBeenCalled();
  });

  it('should stop syncing database', async () => {
    const rncbl = new RNCBL(testDBName);

    spyStopSyncing.mockResolvedValue(true);
    const result: boolean = await rncbl.stopSyncingDatabase();
    expect(result).toStrictEqual(true);
    expect(rncbl.syncingState().syncingInitiated).toStrictEqual(false);
    expect(spyStopSyncing).toHaveBeenCalledWith(testDBName);
  });

  it('should stop syncing database - hanle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const reply: SyncingInitiationStatusInterface = {
      syncingInitiated: true,
      syncingInitiatedStatus: '',
    };
    const syncAuthOption: SyncAuthOption = {
      username: 'username',
      password: 'password',
    };
    const syncParameters: SyncingParametersInterface = {
      syncURL: 'wss://abc.net',
      continuous: true,
      enableTrackingSyncStatus: true,
      syncDirection: SyncingDirection.pull,
      eventName: 'syncingStatus',
      authOption: syncAuthOption,
    };
    spySyncDatabase.mockResolvedValue(reply);
    await rncbl.syncDatabase(syncParameters);
    spyStopSyncing.mockRejectedValue('error');
    await expect(() => rncbl.stopSyncingDatabase()).rejects.toThrow(
      new Error('error'),
    );
    expect(rncbl.syncingState().syncingInitiated).toStrictEqual(true);
    expect(spyStopSyncing).toHaveBeenCalledWith(testDBName);
  });

  it('should query a database', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    query.select(['group']);

    spyQuery.mockResolvedValue([]);

    try {
      const result = await rncbl.query(query, {liveQuery: false});
      expect(result).toStrictEqual([]);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyQuery).toHaveBeenCalledWith(testDBName, query.query, {
      liveQuery: false,
    });
  });

  it('should query database - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    query.select(['group']);
    spyQuery.mockRejectedValue('error');

    await expect(() => rncbl.query(query, {liveQuery: false})).rejects.toThrow(
      new Error('error'),
    );
    expect(spyQuery).toHaveBeenCalledWith(testDBName, query.query, {
      liveQuery: false,
    });
  });

  it('should query database - handle select not called error', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    spyQuery.mockRejectedValue('error');

    await expect(() => rncbl.query(query, {liveQuery: false})).rejects.toThrow(
      new Error('Error: mustCallSelectFirst'),
    );
    expect(spyQuery).not.toHaveBeenCalled();
  });

  it('should query database - throw incomplete join', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    query
      .select(SelectResult.selectField('gender'))
      .join(
        Join.tables(
          'primaTable',
          'age',
          ComparisonOperators.equalsTo,
          'secondaryTable',
          'age',
        ),
      )
      .groupBy([{fieldName: 'gender'}]);
    spyQuery.mockRejectedValue('error');

    await expect(() => rncbl.query(query, {liveQuery: false})).rejects.toThrow(
      new Error('Error: joinWithGroupByMustHaveWhereClause'),
    );
    expect(spyQuery).not.toHaveBeenCalled();
  });

  it('should live query a database', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    query.select(['group']);

    spyQuery.mockResolvedValue([]);

    try {
      const result = await rncbl.query(query, {
        liveQuery: true,
        liveQueryName: 'group',
      });
      expect(result).toStrictEqual([]);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyQuery).toHaveBeenCalledWith(testDBName, query.query, {
      liveQuery: true,
      liveQueryName: 'group',
    });
  });

  it('should handle unnamed live query', async () => {
    const rncbl = new RNCBL(testDBName);
    const query: Query = new Query();
    query.select(['group']);
    spyQuery.mockRejectedValue('error');

    await expect(() => rncbl.query(query, {liveQuery: true})).rejects.toThrow(
      new Error('Error: liveQueryMustHaveUniqueName'),
    );
    expect(spyQuery).not.toHaveBeenCalled();
  });

  it('should get a single document', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    const name = 'name';
    const doc: Dictionary = {name};
    spyGetDocument.mockResolvedValue(doc);

    try {
      const result = await rncbl.getDocument(docId);
      expect(result).toStrictEqual(doc);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyGetDocument).toHaveBeenCalledWith(testDBName, docId);
  });

  it('should get a single document - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    const docId = '1234';
    spyGetDocument.mockRejectedValue('error');

    await expect(() => rncbl.getDocument(docId)).rejects.toThrow(
      new Error('error'),
    );
    expect(spyGetDocument).toHaveBeenCalledWith(testDBName, docId);
  });

  it('should stop live query', async () => {
    const rncbl = new RNCBL(testDBName);

    spyStopLiveQuery.mockResolvedValue(true);

    try {
      const result = await rncbl.stopLiveQuery('queryName');
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyStopLiveQuery).toHaveBeenCalledWith(testDBName, 'queryName');
  });

  it('should stop live query - handle error', async () => {
    const rncbl = new RNCBL(testDBName);
    spyStopLiveQuery.mockRejectedValue('error');

    await expect(() => rncbl.stopLiveQuery('queryName')).rejects.toThrow(
      new Error('error'),
    );
    expect(spyStopLiveQuery).toHaveBeenCalledWith(testDBName, 'queryName');
  });

  it('should delete database indexes', async () => {
    spyDeleteIndexes.mockResolvedValue(true);
    const rncbl = new RNCBL(testDBName);
    const indexesToDelete: string[] = ['type', 'name'];

    try {
      const result = await rncbl.deleteIndexes(indexesToDelete);
      expect(result).toStrictEqual(true);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyDeleteIndexes).toHaveBeenCalledWith(testDBName, indexesToDelete);
  });

  it('should copy documents to another database - default options', async () => {
    const rncbl = new RNCBL(testDBName);
    const destDB = new RNCBL('destDB');
    spyCopyDocumentsToDatabase.mockResolvedValue(1);

    try {
      const result = await rncbl.copyDocumentsToDatabase(destDB);
      expect(result).toStrictEqual(1);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyCopyDocumentsToDatabase).toHaveBeenCalledWith(
      testDBName,
      'destDB',
      undefined,
    );
  });

  it('should copy documents to another database - multipe options', async () => {
    const rncbl = new RNCBL(testDBName);
    const destDB = new RNCBL('destDB');
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'gender',
        value: 'M',
        comparison: ComparisonOperators.equalsTo,
      },
    ];
    spyCopyDocumentsToDatabase.mockResolvedValue(1);
    const exp = new Expression(json);
    const copyDBOptions: OptionsCopyDocumentsBetweenDatabases = {
      omitFields: ['gender'],
      keepMetaID: false,
      overWriteExisting: true,
      copyDocsOfOnly: exp.expression,
    };
    try {
      const result = await rncbl.copyDocumentsToDatabase(destDB, copyDBOptions);
      expect(result).toStrictEqual(1);
    } catch (err) {
      expect(err).toBeNull();
    }
    expect(spyCopyDocumentsToDatabase).toHaveBeenCalledWith(
      testDBName,
      'destDB',
      {
        copyDocsOfOnly: [{comparison: 0, fieldName: 'gender', value: 'M'}],
        keepMetaID: false,
        omitFields: ['gender'],
        overWriteExisting: true,
      },
    );
  });
});
