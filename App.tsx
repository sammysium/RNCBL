/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React, {useEffect} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  Button,
  useColorScheme,
  View,
  DeviceEventEmitter
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import {
  ComparisonOperators,
  OrderByOptions,
  Relationship,
  SyncingDirection,
} from './src/Constants';
import Expression, {ExpressionDefinition} from './src/Expression';
import Query from './src/Query';
import { Sum, Count}from './src/functions';
import {
  Dictionary,
  SyncAuthOption,
  SyncingInitiationStatusInterface,
  SyncingOperationStatus,
  SyncingParametersInterface,
  PullPushFilter
} from './src/Interfaces';
import MutableDocument from './src/MutableDocument';
import RNCBL from './src/RNCBL';


const App = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const syncAuthOption: SyncAuthOption = {
    username: 'semira',
    password: 'semira'
  };
  const pullFilter: PullPushFilter[] = [
    {
      value: 'test-doc',
      fieldName: 'type', 
      comparison: ComparisonOperators.equalsTo,
      connectWithNextVia: Relationship.or,
    },
    {
      value: 'third-app',
      fieldName: 'type', 
      comparison: ComparisonOperators.equalsTo,
    },
  ];
  const syncParameters: SyncingParametersInterface = {
    syncURL: 'ws://10.0.2.2:4984/offline/',
    continuous: true,
    enableTrackingSyncStatus: true,
    syncDirection: SyncingDirection.pull,
    eventName: 'syncingStatus',
    authOption: syncAuthOption,
    filterPull: pullFilter,
  };

  const rncbl = new RNCBL('tupac');

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const deleteDB = async () => {
    try {
      await rncbl.deleteDB(false);
      console.log(72, 'deleted');
    } catch (error) {
      console.log(74, error, ' deleting db');
    }
  };

  const deleteDBRecreate = async () => {
    try {
      await rncbl.deleteDB(true);
      console.log(88, 'deleted');
    } catch (error) {
      console.log(90, error, ' deleting db');
    }
  };

  const insertMutableDocument = async () => {
    const doc = new MutableDocument();
    doc.putString('firstName', 'Daniel');
    doc.putInteger('age', 34);
    doc.putType('employee');
    try {
      const result = await rncbl.insertMutableDocument(doc);
      console.log(94, result);
    } catch (error) {
      console.log(96, error);
    }
  };

  const insertJSONDocument = async () => {
    const doc: Dictionary = {
      type: 'employee',
      name: 'elim',
    };
    try {
      const result = await rncbl.insertJSONDocument(doc);
      console.log(114, result);
    } catch (error) {
      console.log(116, error);
    }
  };

  const getDocument = async () => {
    const doc: Dictionary = {
      type: 'employee',
      age: 20,
      salary: 1903.93,
      ranks: [12, 3, 4, 6],
      active: true,
      obj: {a: 'A', b:'B'},
      objAr: [{a: 'A', b:'B'}],
      nl: null
    };
    try {
      const result = await rncbl.insertJSONDocument(doc);
      const document: Dictionary = await rncbl.getDocument(result);
      console.log(140, document);
    } catch (error) {
      console.log(116, error);
    }
  };

  const updateDocument = async ()=>{
     const docId = '5ff0c982-0bb0-45fb-8cb9-3901379bd074';
     const newData : Dictionary = {
       name: 'ELim Paulous'
     };
     try {
      const result: boolean = await rncbl.updateDocument(docId, newData);
      console.log(125, result)
     }catch(error){
     console.log(127, error);
     }

  }

  const deleteDocument = async ()=>{
    const docId = '5ff0c982-0bb0-45fb-8cb9-3901379bd074';

    try {
     const result: boolean = await rncbl.deleteDocument(docId);
     console.log(137, result)
    }catch(error){
    console.log(129, error);
    }

 }

const insertJSONDocuments = async () =>{
  const docs : Dictionary [] = [];

  docs.push(
    {
    type: 'employee',
    'name': 'wINNIE',
    gender: 'F',
    age: 28
   },
   {
    type: 'employee',
    'name': 'Tupac',
    gender: 'M',
    age: 45
   },
       {
    type: 'employee',
    'name': 'Mercy',
    gender: 'F',
    age: 31
   },
   {
    type: 'employee',
    'name': 'Jordan',
    gender: 'F',
    age: 22
   },
   {
    type: 'employee',
    'name': 'emineam',
    gender: 'M',
    age: 31
   }
   // {
  //   type: 'employee',
  //   'name': 'Daniel',
  //   gender: 'M',
  //   age: 28
  // },{
  //   type: 'employee',
  //   'name': 'punk',
  //   gender: 'M',
  //   age: 18
  // }
  
  );

   try {
      const result: MultipleProcessedDocumentsStat =
        await rncbl.insertJSONDocuments(docs);
      console.log(167, result);
   } catch (error) {
     console.log(169, error);
   }
}


const insertMutableDocuments = async () =>{
  const docs: MutableDocument[] = [];
  const doc1: MutableDocument = new MutableDocument();
  doc1.putType('employee');
  doc1.putString('name', 'Semira');
  doc1.putInteger('age', 30);
  const doc2: MutableDocument = new MutableDocument();
  doc2.putType('employee');
  doc2.putString('name', 'Samuel');
  doc2.putInteger('age', 36);

  docs.push(doc1);
  docs.push(doc2);
  try {
    const results: MultipleProcessedDocumentsStat = await rncbl.insertMutableDocuments(docs);
    console.log(189, results)
  } catch (error) {
    console.log(191, error);
  }

}

const updateEmployees = async () => {
  // add info to them
  /*
  const exp = new Expression();
  exp.single({fieldName: 'type', value: 'employee'});
  const newData: Dictionary = {
    status: 'new'
  };
  try {
const results: MultipleProcessedDocumentsStat = await rncbl.updateDocuments(exp, newData);
console.log(205, results);
  } catch (error) {
    console.log(207, error)
  }
  */
}

const deleteEmployees = async () => {
  // add info to them
  const exp = new Expression();
  exp.single({fieldName: 'type', value: 'employee'}).single({fieldName: 'age', value: 34})
  try {
const results: MultipleProcessedDocumentsStat = await rncbl.deleteDocuments(exp);
console.log(217, results);
  } catch (error) {
    console.log(219, error)
  }
}


  const updateDocuments = async () => {
    const b: GroupExpressionionInfo = {
      data: [
        {
          fields: [
            {
              fieldName: 'job',
              value: 'IT',
              comparison: ComparisonOperators.equalsTo,
            },
            {
              fieldName: 'status',
              value: 'new',
              comparison: ComparisonOperators.equalsTo,
            },
          ],
          connectWithNextVia: 'or',
        },
        {
          fields: [
            {
              fieldName: 'job',
              value: 'admin',
              comparison: ComparisonOperators.equalsTo,
            },
            {
              fieldName: 'status',
              value: 'old',
              comparison: ComparisonOperators.equalsTo,
            },
          ],
          connectWithNextVia: 'or',
        },
      ],
      connectWithNextVia: 'and',
    };

    const c: GroupExpressionionInfo = {
      data: [
        {
          fields: [
            {
              fieldName: 'age',
              value: 19,
              comparison: ComparisonOperators.equalsTo,
              connectWithNextVia: 'or',
            },
            {
              fieldName: 'age',
              value: 20,
              comparison: ComparisonOperators.equalsTo,
            },
          ],
        },
      ],
      connectWithNextVia: 'or',
    };

    const exp = new Expression();
    exp.single({
      fieldName: 'gender',
      value: 'M',
      comparison: ComparisonOperators.equalsTo,
    });
    exp.grouped(b).grouped(c).single({
      fieldName: 'faith',
      value: 'jesus',
      comparison: ComparisonOperators.equalsTo,
    });

    try {
      const updateData: Dictionary = {
        keepActive: true,
      };
      const result = await rncbl.updateDocuments(exp, updateData);
      console.log(110, result);
    } catch (error) {
      console.log(174, error);
    }
  };
  const onSyncingStatus = (event: any) => {
    /*
    event contains status key with a json striginfied object
    */
    const currentStatus: SyncingOperationStatus = JSON.parse(event.status);
    console.log(324, currentStatus);
  };
  const syncDatabase = async () =>{
    const attempt: SyncingInitiationStatusInterface = await rncbl.syncDatabase(
      syncParameters,
    );
    if (attempt.syncingInitiated){
      // we can try to listen to changes if we want too. not syncParamters must have enable tracking enabled
          // at what stage do we want to remove it?
      DeviceEventEmitter.addListener(syncParameters.eventName, onSyncingStatus);
    }
  };

  const stopSyncingDatabase = () =>{
    rncbl.stopSyncingDatabase();
  }

  const query = async ()=>{
    const q = new Query();
    const json: ExpressionDefinition[] = [
      {
        fieldName: 'age',
        value: 22, //wINNIE
        value2: 28,
        comparison: ComparisonOperators.between,
        connectWithNextVia: Relationship.and,
      }
    ];

    const exp = new Expression(json);

    q.select(['type','endpoint'])
     // .where(exp);
      // .groupBy(['type'])
      // .limit({limit: 10, offset: 89})
      // .orderBy([{fieldName: 'gender', orderBy: OrderByOptions.asc}]);
    try {
      const results = await rncbl.query(q, {liveQuery: false});
      console.log(344, results.length,  results)
    } catch (error) {
      console.log(346, error);
    }
  }

  const syncDatabaseC = (start = true) =>{
    const rncbl2 = new RNCBL("donkey");
    if (start){
      rncbl2.initialize(["type", "id"]).then(done=>{
        rncbl2.syncDatabase(syncParameters);
      }).catch(err=>{
        console.log(314, err)
      })
    }else{
      rncbl2.stopSyncingDatabase();
    }

  }


  useEffect(() => {
    rncbl
      .initialize(['siteId'])
      .then(result => {
        console.log('db created', result);
      })
      .catch(error => {
        console.log(error);
      });
  });

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <Button onPress={deleteDB} title="Delete Database" />
          <Button
            onPress={deleteDBRecreate}
            title="Delete && Recreate Database"
          />
          <Button onPress={insertJSONDocument} title="Insert JSON Document" />
          <Button
            onPress={insertMutableDocument}
            title="Insert Mutable Document"
          />

<Button onPress={updateDocument} title="Update Document" />

<Button onPress={deleteDocument} title="Delete Document" />

<Button onPress={getDocument} title="Get Document" />
          <Button onPress={insertJSONDocuments} title="Insert JSON Documents" />
          <Button
            onPress={insertMutableDocuments}
            title="Insert Mutable Documents"
          />

          <Button onPress={updateEmployees} title="Update Employees" />
          <Button onPress={deleteEmployees} title="Delete Employees" />

          <Button onPress={syncDatabase} title="Sync Documents" />
          <Button onPress={query} title="Query" />
          <Button onPress={stopSyncingDatabase} title="Stop Syncing" />

          <Button onPress={()=>syncDatabaseC()} title="Create and Sync Donkey DB" />
          <Button onPress={()=>syncDatabaseC(false)} title="Stop Syncing Donkey Db" />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
