/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.codes = {

  /**
   * This default language is used whenever a code registers its texts in scout.texts.
   */
  defaultLanguage: 'en',

  registry: {},

  bootstrap: function(url) {
    var promise = url ? $.ajaxJson(url) : $.resolvedPromise({});
    return promise.then(this._preInit.bind(this, url));
  },

  _preInit: function(url, data) {
    if (data && data.error) {
      // The result may contain a json error (e.g. session timeout) -> abort processing
      throw {
        error: data.error,
        url: url
      };
    }
    this.init(data);
  },

  init: function(data) {
    data = data || {};
    Object.keys(data).forEach(function(codeTypeId) {
      this.add(data[codeTypeId]);
    }, this);
  },

  /**
   * @param codes one or more codeTypes, maybe an object or an array
   */
  add: function(codeTypes) {
    codeTypes = scout.arrays.ensure(codeTypes);
    codeTypes.forEach(function(codeType) {
      codeType = scout.CodeType.ensure(codeType);
      this.registry[codeType.id] = codeType;
    }, this);
  },

  /**
   * @param codeTypes code types or code type ids to remove
   */
  remove: function(codeTypes) {
    codeTypes = scout.arrays.ensure(codeTypes);
    codeTypes.forEach(function(codeType) {
      var id;
      if (typeof codeType === 'string') {
        id = codeType;
      } else {
        id =  codeType.id;
      }
      delete this.registry[id];
      // TODO [7.0] awe: also clean up texts?
    }, this);
  },

  /**
   * Returns a code for the given codeId. When you work with hard-coded codes
   * you should always use this function and not <code>optGet</code>.
   *
   * The codeId is a string in the following format:
   *
   * "[CodeType.id] [Code.id]"
   *
   * Examples:
   * "71074 104860"
   * "MessageChannel Phone"
   *
   * CodeType.id and Code.id are separated by a space.
   * The Code.id alone is not unique, that's why the CodeType.id must be always provided.
   *
   * You can also call this function with two arguments. In that case the first argument
   * is the codeTypeId and the second is the codeId.
   *
   * @param {string} vararg either only "[CodeType.id]" or "[CodeType.id] [Code.id]"
   * @param {string} [codeId]
   * @returns {Code} a code for the given codeId
   * @throw {Error} if code does not exist
   */
  get: function(vararg, codeId) {
    return this._get('get', scout.objects.argumentsToArray(arguments));
  },

  /**
   * Same as <code>get</code>, but does not throw an error if the code does not exist.
   * You should always use this function when you work with codes coming from a dynamic data source.
   *
   * @param vararg
   * @param codeId
   * @returns {Code} code for the given codeId or undefined if code does not exist
   */
  optGet: function(vararg, codeId) {
    return this._get('optGet', scout.objects.argumentsToArray(arguments));
  },

  _get: function(funcName, funcArgs) {
    var codeTypeId, codeId;
    if (funcArgs.length === 2) {
      codeTypeId = funcArgs[0];
      codeId = funcArgs[1];
    } else {
      var tmp = funcArgs[0].split(' ');
      if (tmp.length !== 2) {
        throw new Error('Invalid string. Must have format "[CodeType.id] [Code.id]"');
      }
      codeTypeId = tmp[0];
      codeId = tmp[1];
    }
    scout.assertParameter('codeTypeId', codeTypeId);
    scout.assertParameter('codeId', codeId);
    return this.codeType(codeTypeId)[funcName](codeId);
  },

  codeType: function(codeTypeId, optional) {
    var codeType = this.registry[codeTypeId];
    if (!optional && !codeType) {
      throw new Error('No CodeType found for id=' + codeTypeId);
    }
    return codeType;
  },

  generateTextKey: function(code) {
    // Use __ as prefix to reduce the possibility of overriding 'real' keys
    return '__code.' + code.id;
  },

  /**
   * Registers texts for a code. It uses the method generateTextKey to generate the text key.
   * The texts for the default locale specified by scout.codes.defaultLanguage are used as default texts.
   *
   * @param code the code to register the text for
   * @param texts an object with the languageTag as key and the translated text as value
   * @return the generated text key
   */
  registerTexts: function(code, texts) {
    var key = scout.codes.generateTextKey(code);

    // In case of changed defaultLanguage clear the 'default' entry
    scout.texts.get('default').remove(key);

    for (var languageTag in texts) { // NOSONAR
      var text = texts[languageTag];
      // Use defaultLanguage as default, if specified (may be changed or set to null by the app).
      if (languageTag && languageTag === this.defaultLanguage) {
        languageTag = 'default';
      }
      scout.texts.get(languageTag).add(key, text);
    }
    return key;
  }

};