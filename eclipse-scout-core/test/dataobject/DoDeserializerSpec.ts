/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, dataobjects, dates, DoDeserializer, DoRegistry, ObjectFactory, typeName} from '../../src/index';

describe('DoDeserializer', () => {

  beforeAll(() => {
    ObjectFactory.get().registerNamespace('scout', {
      Fixture01Do, Fixture02Do, Fixture03Do
    }, {allowedReplacements: ['scout.Fixture01Do', 'scout.Fixture02Do', 'scout.Fixture03Do']});

    const doRegistry = DoRegistry.get();
    doRegistry.add(Fixture01Do);
    doRegistry.add(Fixture02Do);
    doRegistry.add(Fixture03Do);
  });

  afterAll(() => {
    const doRegistry = DoRegistry.get();
    doRegistry.removeByClass(Fixture01Do);
    doRegistry.removeByClass(Fixture02Do);
    doRegistry.removeByClass(Fixture03Do);
  });

  it('can deserialize based on _type', () => {
    const json = `{
      "_type": "scout.Fixture01",
      "propBool": true,
      "propNum": 1234.5678,
      "propStr": "testString",
      "propDate": "2024-07-15 13:51:39.708Z",
      "propNull": null,
      "propArr": [
        {
          "nestedDate": "2024-07-14 13:51:39.708Z",
          "nestedObj": {
            "nestedNestedDate": "2024-07-13 13:51:39.708Z"
          },
          "nestedIfc": {
            "_type": "scout.Fixture03",
            "nestedNestedDate": "2024-07-12 13:51:39.708Z"
          }
        },
        {
          "nestedDate": "2024-07-11 13:51:39.708Z",
          "nestedObj": {
            "nestedNestedDate": "2024-07-10 13:51:39.708Z"
          },
          "nestedIfc": {
            "_type": "not.existing.but.should.survive",
            "nestedNestedDate": "2024-07-09 13:51:39.708Z"
          }
        }
      ],
      "propArr2": [[[["2024-07-02 13:51:39.708Z", "2024-07-02 12:51:39.708Z"]], [["2024-07-02 11:51:39.708Z", "2024-07-02 10:51:39.708Z"]]]],
      "propObj": {
        "nestedNestedDate": "2024-07-08 13:51:39.708Z"
      }
    }
    `;
    const dataobject = dataobjects.parse(json) as Fixture01Do; // do not pass type so that the detection is tested as well
    expect(dataobject).toBeInstanceOf(Fixture01Do);
    expect(dataobject.propBool).toBeTrue();
    expect(dataobject.propNum).toBe(1234.5678);
    expect(dataobject.propStr).toBe('testString');
    expect(dataobject.propDate).toBeInstanceOf(Date);
    expect(dataobject.propDate).toEqual(dates.parseJsonDate('2024-07-15 13:51:39.708Z'));
    expect(Array.isArray(dataobject.propArr)).toBeTrue();
    expect(dataobject.propArr.length).toBe(2);
    expect(dataobject._type).toBe('scout.Fixture01'); // does not come from deserialize but from the instance creation
    expect(dataobject.propNull).toBeNull();

    const propArr0 = dataobject.propArr[0];
    expect(propArr0).toBeInstanceOf(Fixture02Do);
    expect(propArr0.nestedDate).toEqual(dates.parseJsonDate('2024-07-14 13:51:39.708Z'));
    expect(propArr0.nestedObj).toBeInstanceOf(Fixture03Do);
    expect(propArr0.nestedObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-13 13:51:39.708Z'));
    expect(propArr0.nestedIfc).toBeInstanceOf(Fixture03Do);
    expect(propArr0.nestedIfc.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-12 13:51:39.708Z'));

    const propArr1 = dataobject.propArr[1];
    expect(propArr1).toBeInstanceOf(Fixture02Do);
    expect(propArr1.nestedDate).toEqual(dates.parseJsonDate('2024-07-11 13:51:39.708Z'));
    expect(propArr1.nestedObj).toBeInstanceOf(Fixture03Do);
    expect(propArr1.nestedObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-10 13:51:39.708Z'));
    expect(propArr1.nestedIfc).toBeInstanceOf(BaseDoEntity); // because _type is missing and the interface is not helpful
    const nested = propArr1.nestedIfc as any;
    expect(nested.nestedNestedDate).toBe('2024-07-09 13:51:39.708Z');
    expect(nested._type).toBe('not.existing.but.should.survive');

    let expectedDateArray = [[[[dates.parseJsonDate('2024-07-02 13:51:39.708Z'), dates.parseJsonDate('2024-07-02 12:51:39.708Z')]],
      [[dates.parseJsonDate('2024-07-02 11:51:39.708Z'), dates.parseJsonDate('2024-07-02 10:51:39.708Z')]]]];
    expect(dataobject.propArr2).toEqual(expectedDateArray); // is detected as array of Date with dimension 4
    expect(dataobject.propObj).toBeInstanceOf(Fixture03Do);
    expect(dataobject.propObj.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-08 13:51:39.708Z'));
  });

  it('can deserialize based on objectType', () => {
    const withStringObjectType = `{
      "objectType": "scout.Fixture03Do",
      "nestedNestedDate": "2024-07-25 09:41:10.708Z"
    }
    `;
    const resultFromStringObjectType = new DoDeserializer().parse(withStringObjectType) as Fixture03Do;
    expect(resultFromStringObjectType).toBeInstanceOf(Fixture03Do);
    expect(resultFromStringObjectType.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 09:41:10.708Z'));

    const withConstructorObjectType = {
      objectType: Fixture03Do,
      nestedNestedDate: '2024-07-25 08:41:10.708Z'
    };
    const resultFromConstructorObjectType = new DoDeserializer().reviveObject(withConstructorObjectType) as Fixture03Do;
    expect(resultFromConstructorObjectType).toBeInstanceOf(Fixture03Do);
    expect(resultFromConstructorObjectType.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 08:41:10.708Z'));
  });

  it('Uses BaseDoEntity if no type information is available', () => {
    const result = new DoDeserializer().parse('{"num":1234}') as any;
    expect(result).toBeInstanceOf(BaseDoEntity);
    expect(result.num).toBe(1234);
  });

  it('uses objectType given for instance creation', () => {
    const json = `{
      "_type": "scout.Fixture01",
      "nestedNestedDate": "2024-07-25 07:41:10.708Z"
    }
    `;
    const fromConstructor = dataobjects.parse(json, Fixture03Do);
    expect(fromConstructor).toBeInstanceOf(Fixture03Do);// _type from string is ignored and given type should be used.
    expect(fromConstructor.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 07:41:10.708Z'));

    const fromString = dataobjects.parse(json, 'scout.Fixture03Do') as Fixture03Do;
    expect(fromString).toBeInstanceOf(Fixture03Do);// _type from string is ignored and given type should be used.
    expect(fromString.nestedNestedDate).toEqual(dates.parseJsonDate('2024-07-25 07:41:10.708Z'));
  });
});

@typeName('scout.Fixture01')
export class Fixture01Do extends BaseDoEntity {
  propBool: boolean;
  propNum: number;
  propStr: string;
  propNull: string;
  propDate: Date;
  propArr: Fixture02Do[];
  propArr2: Array<Array<Date[]>[]>;
  propObj: Fixture03Do;
}

@typeName('scout.Fixture02')
export class Fixture02Do extends BaseDoEntity {
  nestedDate: Date;
  nestedObj: Fixture03Do;
  nestedIfc: FixtureDoIfc;
}

export interface FixtureDoIfc {
  nestedNestedDate: Date;
}

@typeName('scout.Fixture03')
export class Fixture03Do extends BaseDoEntity implements FixtureDoIfc {
  nestedNestedDate: Date;
}
