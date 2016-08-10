/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 The jabsorb team
 * Copyright (c) 2005 Michael Clark, Metaparadigm Pte Ltd
 * Copyright (c) 2003-2004 Jan-Klaas Kollhof
 *
 * This code is based on original code from the json-rpc-java library
 * which was originally based on Jan-Klaas' JavaScript o lait library
 * (jsolait).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* escape a character */

var escapeJSONChar=function ()
{
  var escapeChars = ["\b","\t","\n","\f","\r"];

  return function(c){
    // Need to do these first as their ascii values are > 32 (34 & 92)
    if(c == "\"" || c == "\\")
    {
      return "\\"+c;
    }
    //Otherwise it doesn't need escaping
    if(c.charCodeAt(0)>=32)
    {
      return c;
    }
    // Otherwise it is has a code < 32 and may need escaping.
    for(var i=0;i<escapeChars.length;i++)
    {
      if(c==escapeChars[i])
      {
        return "\\" + c;
      }
    }
    // it was a character from 0-31 that wasn't one of the escape chars
    return c;
  };
}();

/* encode a string into JSON format */

function escapeJSONString(s)
{
  /* The following should suffice but Safari's regex is b0rken
      (doesn't support callback substitutions)
      return "\"" + s.replace(/([^\u0020-\u007f]|[\\\"])/g,
      escapeJSONChar) + "\"";
   */

  /* Rather inefficient way to do it */
  var parts = s.split("");
  for (var i = 0; i < parts.length; i++)
  {
    parts[i] = escapeJSONChar(parts[i]);
  }
  return "\"" + parts.join("") + "\"";
}

/**
 * Marshall an object to JSON format.
 * Circular references can be handled if the client parameter
 * JSONRpcClient.fixupCircRefs is true.  If this parameter is false,
 * an exception will be thrown if a circular reference is detected.
 *
 * if the client parameter, JSONRpcClient.fixupDuplicates is true then
 * duplicate objects in the object graph are also combined except for Strings
 *
 * (todo: it wouldn't be too hard to optimize strings as well, but probably a threshold
 *  should be provided, so that only strings over a certain length would be optimized)
 * this would be worth doing on the upload (on the download it's not so important, because
 * gzip handles this)
 * if it's false, then duplicate objects are "re-serialized"
 *
 * @param o       the object being converted to json
 *
 * @return an object, { 'json': jsonString, 'fixups': fixupString } or
 *            just { 'json' : jsonString }  if there were no fixups found.
 */
function toJSON(o)
{
  // to detect circular references and duplicate objects, each object has a special marker
  // added to it as we go along.

  // therefore we know if the object is either a duplicate or circular ref if the object
  // already has this marker in it before we process it.

  // the marker object itself contains two pointers-- one to the last object processed
  // and another to the parent object
  // therefore we can rapidly detect if an object is a circular reference
  // by following the chain of parent pointer objects to see if we find the same object again.

  // if we don't find the same object again in the parent recursively, then we know that it's a
  // duplicate instead of a circular reference

  // the pointer to the last object processed is used to link all processed objects together
  // so that the marker objects can be removed when the operation is complete

  // once all objects are processed, we can traverse the linked list, removing all the markers

  // the special name for the marker object
  // try to pick a name that would never be used for any other purpose, so
  // it won't conflict with anything else
  var marker="$_$jabsorbed$813492";

  // the head of the marker object chain
  var markerHead;

  // fixups detected as we go along, both for circular references and duplicates
  var fixups = [];

  // unlink the whole chain of marker objects that were added to objects when processing
  function removeMarkers()
  {
    var next;
    while (markerHead)
    {
      next = markerHead[marker].prev;
      delete markerHead[marker];
      markerHead = next;
    }
  }

  // special object used to indicate that an object should be omitted
  // because it was found to be a circular reference or duplicate
  var omitCircRefOrDuplicate = {};

  // temp variable to hold json while processing
  var json;

  /**
   * Do the work of converting an individual "sub" object to JSON.
   * Each object that is processed has a special marker object attached to it, 
   * to quickly detect if it has already been processed and thus handle 
   * circular references and duplicates.
   *
   * @param o   object being converted into JSON.
   * 
   * @param p   the parent of the object being processed, it should be null or
   *            undefined if it's the root object.
   * 
   * @param ref the "reference" of the object in the parent that is being 
   *            converted such that p[ref] === o.
   * 
   * @return A string containing the JSON representation of the object o, but 
   *         with duplicates and circular references removed (according to the 
   *         option settings JSONRpcClient.fixupCircRefs and 
   *         JSONRpcClient.fixupDuplicates. 
   */
  function subObjToJSON(o,p,ref)
  {
    var v = [],
        // list of references to get to the fixup entry
        fixup,
        // list of reference to get to the original location
        original,
        parent,
        circRef,
        i;
        
    if (o === null || o === undefined)
    {
      return "null";  // it's null or undefined, so serialize it as null
    }
    else if (typeof o === 'string')
    {
      //todo: handle duplicate strings!  but only if they are over a certain threshold size...
      return escapeJSONString(o);
    }
    else if (typeof o === 'number')
    {
      return o.toString();
    }
    else if (typeof o === 'boolean')
    {
      return o.toString();
    }
    else
    {
      // must be an object type

      // look for an already existing marker which would mean this object has already been processed
      // at least once and therefore either a circular ref or dup has been found!!
      if (o[marker])
      {
        // determine if it's a circular reference
        fixup = [ref];
        parent = p;

        // walk up the parent chain till we find null
        while (parent)
        {
          // if a circular reference was found somewhere along the way,
          // calculate the path to it as we are going
          if (original)
          {
            original.unshift (parent[marker].ref);
          }

          // if we find ourself, then we found a circular reference!
          if (parent===o)
          {
            circRef=parent;
            original = [circRef[marker].ref];
          }

          fixup.unshift(parent[marker].ref);
          parent = parent[marker].parent;
        }

        // if we found ourselves in the parent chain then this is a circular reference
        if (circRef)
        {
          //either save off the circular reference or throw an exception, depending on the client setting
          if (JSONRpcClient.fixupCircRefs)
          {
            // remove last redundant unshifted reference
            fixup.shift();
            original.shift();

            //todo: (LATER) if multiple fixups go to the same original, this could be optimized somewhat
            fixups.push([fixup, original]);
            return omitCircRefOrDuplicate;
          }
          else
          {
            removeMarkers();
            throw new Error("circular reference detected!");
          }
        }
        else
        {
          // otherwise it's a dup!
          if (JSONRpcClient.fixupDuplicates)
          {
            // find the original path of the dup
            original = [o[marker].ref];
            parent = o[marker].parent;
            while (parent)
            {
              original.unshift(parent[marker].ref);
              parent = parent[marker].parent;
            }
            //todo: (LATER) if multiple fixups go to the same original, this could be optimized somewhat

            // remove last redundant unshifted reference
            fixup.shift();
            original.shift();

            fixups.push([fixup, original]);
            return omitCircRefOrDuplicate;
          }
        }
      }
      else
      {
        // mark this object as visited/processed and set up the parent link and prev link
        o[marker] = {parent:p, prev:markerHead, ref:ref};

        // adjust the "marker" head pointer so the prev pointer on the next object processed can be set
        markerHead = o;
      }

      if (o.constructor === Date)
      {
        return '{javaClass: "java.util.Date", time: ' + o.valueOf() + '}';
      }
      else if (o.constructor === Array)
      {
        for (i = 0; i < o.length; i++)
        {
          json = subObjToJSON(o[i], o, i);

          // if it's a dup/circ ref, put a slot where the object would have been
          // otherwise, put the json data here
          v.push(json===omitCircRefOrDuplicate?null:json);
        }
        return "[" + v.join(", ") + "]";
      }
      else
      {
        for (var attr in o)
        {
          if (attr === marker)
          {
             /* skip */
          }
          else if (o[attr] === null || o[attr] === undefined)
          {
            v.push("\"" + attr + "\": null");
          }
          else if (typeof o[attr] == "function")
          {
             /* skip */
          }
          else
          {
            json = subObjToJSON(o[attr], o, attr);
            if (json !== omitCircRefOrDuplicate)
            {
              v.push(escapeJSONString(attr) + ": " + json);
            }
          }
        }
        return "{" + v.join(", ") + "}";
      }
    }
  }

  json = subObjToJSON(o, null, "root");

  removeMarkers();

  // only return the fixups if one or more were found
  if (fixups.length)
  {
    return {json: json, fixups: fixups};
  }
  else
  {
    return {json: json};
  }
}

/* JSONRpcClient constructor */
function JSONRpcClient()
{
  var arg_shift = 0,
      req,
      _function,
      methods,
      self;

  //If a call back is being used grab it
  if (typeof arguments[0] == "function")
  {
    this.readyCB = arguments[0];
    arg_shift++;
  }
  //The next 3 args are passed to the http request
  this.serverURL = arguments[arg_shift];
  this.user = arguments[arg_shift + 1];
  this.pass = arguments[arg_shift + 2];
  //A unique identifier which the identity hashcode of the object on the server, if this is a reference type
  this.objectID = arguments[arg_shift + 3];
  //The full package+classname of the object
  this.javaClass = arguments[arg_shift + 4];
  //The reference type this is: Reference or CallableReference
  this.JSONRPCType = arguments[arg_shift + 5];
  //if we have already made one of these classes before
  if(JSONRpcClient.knownClasses[this.javaClass]&&(this.JSONRPCType=="CallableReference"))
  {
    //Then add all the cached methods to it.
    for (var name in JSONRpcClient.knownClasses[this.javaClass])
    {
      _function = JSONRpcClient.knownClasses[this.javaClass][name];
      //Change the this to the object that will be calling it
      //Note: bind is JSONRPC.bind
      this[name]=JSONRpcClient.bind(_function,this);
    }
  }
  else
  {
    //If we are here, it is either the first time an object of this type has
    //been created or the bridge
    // If it is an object list the methods for it
    if(this.objectID)
    {
      this._addMethods(["listMethods"],this.javaClass);
      req = this._makeRequest("listMethods", []);
    }
    //If it is the bridge get the bridge's methods
    else
    {
      this._addMethods(["system.listMethods"],this.javaClass);
      req = this._makeRequest("system.listMethods", []);
    }

    // If the constructor has an async callback we add a wrapper
    // callback to the request which is called when system.listMethods
    // completes which then adds the methods and calls the callback.
    if (this.readyCB)
    {
      self = this;
      req.cb = function (result, e)
      {
        if (!e)
        {
          self._addMethods(result);
        }
        self.readyCB(result, e);
      };
    }

    // Send the request
    methods = this._sendRequest(req);

    //Now add the methods to the object
    //Note: we don't do this if an async constructor callback has been used
    //as this is done in the request's wrapper callback
    if (!this.readyCB)
    {
      this._addMethods(methods,this.javaClass);
    }
  }
}

//This is a static variable that maps className to a map of functions names to
//calls, ie Map knownClasses<ClassName,Map<FunctionName,Function>>
JSONRpcClient.knownClasses = {};

/* JSONRpcCLient.Exception */
JSONRpcClient.Exception = function (code, message, javaStack)
{
  this.code = code;
  var name,m;
  if (javaStack)
  {
    this.javaStack = javaStack;
    m = javaStack.match(/^([^:]*)/);
    if (m)
    {
      name = m[0];
    }
  }
  if (name)
  {
    this.name = name;
  }
  else
  {
    this.name = "JSONRpcClientException";
  }
  this.message = message;
};

//Error codes that are the same as on the bridge
JSONRpcClient.Exception.CODE_REMOTE_EXCEPTION = 490;
JSONRpcClient.Exception.CODE_ERR_CLIENT = 550;
JSONRpcClient.Exception.CODE_ERR_PARSE = 590;
JSONRpcClient.Exception.CODE_ERR_NOMETHOD = 591;
JSONRpcClient.Exception.CODE_ERR_UNMARSHALL = 592;
JSONRpcClient.Exception.CODE_ERR_MARSHALL = 593;

JSONRpcClient.Exception.prototype = new Error();

JSONRpcClient.Exception.prototype.toString = function (code, msg)
{
  return this.name + ": " + this.message;
};


/* Default top level exception handler */

JSONRpcClient.default_ex_handler = function (e)
{
  alert(e);
};


/* Client settable variables */

JSONRpcClient.toplevel_ex_handler = JSONRpcClient.default_ex_handler;
JSONRpcClient.profile_async = false;
JSONRpcClient.max_req_active = 1;
JSONRpcClient.requestId = 1;

// if this is true, circular references in the object graph are fixed up
// if this is false, circular references cause an exception to be thrown
JSONRpcClient.fixupCircRefs = true;

// if this is true, duplicate objects in the object graph are optimized
// if it's false, then duplicate objects are "re-serialized"
JSONRpcClient.fixupDuplicates = true;

/**
 * Used to bind the this of the serverMethodCaller() (see below) which is to be
 * bound to the right object. This is needed as the serverMethodCaller is
 * called only once in createMethod and is then assigned to multiple
 * CallableReferences are created.
 */
JSONRpcClient.bind=function(functionName,context)
{
  return function() {
    return functionName.apply(context, arguments);
  };
};

/*
 * This creates a method that points to the serverMethodCaller and binds it
 * with the correct methodName.
 */
JSONRpcClient.prototype._createMethod = function (methodName)
{
  //This function is what the user calls.
  //This function uses a closure on methodName to ensure that the function
  //always has the same name, but can take different arguments each call.
  //Each time it is added to an object this should be set with bind()
  var serverMethodCaller= function()
  {
    var args = [],
      callback;
    for (var i = 0; i < arguments.length; i++)
    {
      args.push(arguments[i]);
    }
    if (typeof args[0] == "function")
    {
      callback = args.shift();
    }
    var req = this._makeRequest.call(this, methodName, args, callback);
    if (!callback)
    {
      return this._sendRequest.call(this, req);
    }
    else
    {
      //when there is a callback, add the req to the list 
      JSONRpcClient.async_requests.push(req);
      JSONRpcClient.kick_async();
      return req.requestId;
    }
  };

  return serverMethodCaller;
};

/**
 * This is used to add a list of methods to this.
 * @param methodNames a list containing the names of the methods to add
 * @param javaClass If here it signifies that the function is part of the class
 *   and should be cached.
 */
JSONRpcClient.prototype._addMethods = function (methodNames,javaClass)
{
  var name,
      obj,
      names,
      n,
      method;
  //Aha! It is a class, so create a entry for it.
  //This shouldn't get called twice on the same class so we can happily
  //overwrite it
  if(javaClass){
    JSONRpcClient.knownClasses[javaClass]={};
  }
  
  for (var i = 0; i < methodNames.length; i++)
  {
    obj = this;
    names = methodNames[i].split(".");
    //In the case of system.listMethods create a new object in this called
    //system and and the listMethod function to that object.
    for (n = 0; n < names.length - 1; n++)
    {
      name = names[n];
      if (obj[name])
      {
        obj = obj[name];
      }
      else
      {
        obj[name] = {};
        obj = obj[name];
      }
    }
    //The last part of the name is the actual functionName
    name = names[names.length - 1];
    //If it doesn't yet exist (why would it??)
    if (!obj[name])
    {
      //Then create the method
      method = this._createMethod(methodNames[i]);
      //Bind it to the current this
      obj[name]=JSONRpcClient.bind(method,this);
      //And if this is adding it to an object, then
      //add it to the cache
      if(javaClass&&(name!="listMethods"))
      {
        JSONRpcClient.knownClasses[javaClass][name]=method;
      }
    }
  }
};

JSONRpcClient._getCharsetFromHeaders = function (http)
{
  var contentType,
      parts,
      i;
  try
  {
    contentType = http.getResponseHeader("Content-type");
    parts = contentType.split(/\s*;\s*/);
    for (i = 0; i < parts.length; i++)
    {
      if (parts[i].substring(0, 8) == "charset=")
      {
        return parts[i].substring(8, parts[i].length);
      }
    }
  }
  catch (e)
  {
  }
  return "UTF-8"; // default
};

/* Async queue globals */
JSONRpcClient.async_requests = [];
JSONRpcClient.async_inflight = {};
JSONRpcClient.async_responses = [];
JSONRpcClient.async_timeout = null;
JSONRpcClient.num_req_active = 0;

JSONRpcClient._async_handler = function ()
{
  var res,
      req;
  JSONRpcClient.async_timeout = null;

  while (JSONRpcClient.async_responses.length > 0)
  {
    res = JSONRpcClient.async_responses.shift();
    if (res.canceled)
    {
      continue;
    }
    if (res.profile)
    {
      res.profile.dispatch = new Date();
    }
    try
    {
      res.cb(res.result, res.ex, res.profile);
    }
    catch(e)
    {
      JSONRpcClient.toplevel_ex_handler(e);
    }
  }

  while (JSONRpcClient.async_requests.length > 0 &&
         JSONRpcClient.num_req_active < JSONRpcClient.max_req_active)
  {
    req = JSONRpcClient.async_requests.shift();
    if (req.canceled)
    {
      continue;
    }
    req.client._sendRequest.call(req.client, req);
  }
};

JSONRpcClient.kick_async = function ()
{
  if (!JSONRpcClient.async_timeout)
  {
    JSONRpcClient.async_timeout = setTimeout(JSONRpcClient._async_handler, 0);
  }
};

JSONRpcClient.cancelRequest = function (requestId)
{
  /* If it is in flight then mark it as canceled in the inflight map
      and the XMLHttpRequest callback will discard the reply. */
  if (JSONRpcClient.async_inflight[requestId])
  {
    JSONRpcClient.async_inflight[requestId].canceled = true;
    return true;
  }
  var i;

  /* If its not in flight yet then we can just mark it as canceled in
      the the request queue and it will get discarded before being sent. */
  for (i in JSONRpcClient.async_requests)
  {
    if (JSONRpcClient.async_requests[i].requestId == requestId)
    {
      JSONRpcClient.async_requests[i].canceled = true;
      return true;
    }
  }

  /* It may have returned from the network and be waiting for its callback
      to be dispatched, so mark it as canceled in the response queue
      and the response will get discarded before calling the callback. */
  for (i in JSONRpcClient.async_responses)
  {
    if (JSONRpcClient.async_responses[i].requestId == requestId)
    {
      JSONRpcClient.async_responses[i].canceled = true;
      return true;
    }
  }

  return false;
};

JSONRpcClient.prototype._makeRequest = function (methodName, args, cb)
{
  var req = {};
  req.client = this;
  req.requestId = JSONRpcClient.requestId++;

  var obj = "{\"id\":"+req.requestId+",\"method\":";

  if (this.objectID)
  {
    obj += "\".obj#" + this.objectID + "." + methodName +"\"";
  }
  else
  {
    obj += "\"" + methodName + "\"";
  }

  if (cb)
  {
    req.cb = cb;
  }
  if (JSONRpcClient.profile_async)
  {
    req.profile = {submit: new Date() };
  }

  // use p as an alias for params to save space in the fixups
  var j= toJSON(args);

  obj += ",\"params\":" + j.json;

  // only attach duplicates/fixups if they are found
  // this is to provide graceful backwards compatibility to the json-rpc spec.
  if (j.fixups)
  {
    // todo: the call to toJSON here to turn the fixups into json is a bit 
    // inefficient, since there will never be fixups in the fixups... but
    // it saves us from writing some additional code at this point...
    obj += ",\"fixups\":" + toJSON(j.fixups).json;  
  }

  req.data = obj + "}";

  return req;
};

JSONRpcClient.prototype._sendRequest = function (req)
{
  var http,self;
  if (req.profile)
  {
    req.profile.start = new Date();
  }

  /* Get free http object from the pool */
  http = JSONRpcClient.poolGetHTTPRequest();
  JSONRpcClient.num_req_active++;

  /* Send the request */
  http.open("POST", this.serverURL, !!req.cb, this.user, this.pass);

  /* setRequestHeader is missing in Opera 8 Beta */
  try
  {
    http.setRequestHeader("Content-type", "text/plain");
    http.setRequestHeader("x-requested-with", "XMLHttpRequest");
  }
  catch(e)
  {
  }

  /* Construct call back if we have one */
  if (req.cb)
  {
    self = this;
    http.onreadystatechange = function()
    {
      var res;
      if (http.readyState == 4)
      {
        http.onreadystatechange = function ()
        {
        };
        res = {cb: req.cb, result: null, ex: null};
        if (req.profile)
        {
          res.profile = req.profile;
          res.profile.end = new Date();
        }
        else
        {
          res.profile = false;
        }
        try
        {
          res.result = self._handleResponse(http);
        }
        catch(e)
        {
          res.ex = e;
        }
        if (!JSONRpcClient.async_inflight[req.requestId].canceled)
        {
          JSONRpcClient.async_responses.push(res);
        }
        delete JSONRpcClient.async_inflight[req.requestId];
        JSONRpcClient.kick_async();
      }
    };
  }
  else
  {
    http.onreadystatechange = function()
    {
    };
  }

  JSONRpcClient.async_inflight[req.requestId] = req;

  try
  {
    http.send(req.data);
  }
  catch(e)
  {
    JSONRpcClient.poolReturnHTTPRequest(http);
    JSONRpcClient.num_req_active--;
    throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
  }

  if (!req.cb)
  {
    delete JSONRpcClient.async_inflight[req.requestId];
    return this._handleResponse(http);
  }
  return null;
};

JSONRpcClient.prototype._handleResponse = function (http)
{
  /**
   * Apply fixups.
   * @param obj root object to apply fixups against.
   * @param fixups array of fixups to apply.  each element of this array is a 2 element array, containing
   *        the array with the fixup location followed by an array with the original location to fix up into the fixup
   *        location.
   */
  function applyFixups(obj, fixups)
  {
    function findOriginal(ob, original)
    {
      for (var i=0,j=original.length;i<j;i++)
      {
        ob = ob[original[i]];
      }
      return ob;
    }
    function applyFixup(ob, fixups, value)
    {
      var j=fixups.length-1;
      for (var i=0;i<j;i++)
      {
        ob = ob[fixups[i]];
      }
      ob[fixups[j]] = value;
    }
    for (var i = 0,j = fixups.length; i < j; i++)
    {
      applyFixup(obj,fixups[i][0],findOriginal(obj,fixups[i][1]));
    }
  }

  /* Get the charset */
  if (!this.charset)
  {
    this.charset = JSONRpcClient._getCharsetFromHeaders(http);
  }

  /* Get request results */
  var status, statusText, data;
  try
  {
    status = http.status;
    statusText = http.statusText;
    data = http.responseText;
  }
  catch(e)
  {
/*
    todo:   don't throw away the original error information here!!
    todo:   and everywhere else, as well!
    if (e instanceof Error)
    {
      alert (e.name + ": " + e.message);
    }
*/
    JSONRpcClient.poolReturnHTTPRequest(http);
    JSONRpcClient.num_req_active--;
    JSONRpcClient.kick_async();
    throw new JSONRpcClient.Exception(JSONRpcClient.Exception.CODE_ERR_CLIENT, "Connection failed");
  }

  /* Return http object to the pool; */
  JSONRpcClient.poolReturnHTTPRequest(http);
  JSONRpcClient.num_req_active--;

  /* Unmarshall the response */
  if (status != 200)
  {
    throw new JSONRpcClient.Exception(status, statusText);
  }
  var obj;
  try
  {
    eval("obj = " + data);
  }
  catch(e)
  {
    throw new JSONRpcClient.Exception(550, "error parsing result");
  }
  if (obj.error)
  {
    throw new JSONRpcClient.Exception (obj.error.code, obj.error.msg, obj.error.trace);
  }
  var r = obj.result;

  // look for circular reference/duplicates fixups and execute them if they are there
  
  var i,tmp;
    
  /* Handle CallableProxy */
  if (r) 
  {
    if(r.objectID && r.JSONRPCType == "CallableReference")
    {  
      return new JSONRpcClient(this.serverURL, this.user, this.pass, r.objectID, 
        r.javaClass, r.JSONRPCType);
    }
    r=JSONRpcClient.extractCallableReferences(this,r);
  }
  if (obj.fixups)
  {
    applyFixups(r,obj.fixups);
  }
  return r;
};

JSONRpcClient.extractCallableReferences = function(self,root)
{
  var i,tmp,value;
  for (i in root)
  {
    if(typeof(root[i])=="object")
    {
      tmp=JSONRpcClient.makeCallableReference(self,root[i]);
      if(tmp)
      {
        root[i]=tmp;
      }
      else
      {
        tmp=JSONRpcClient.extractCallableReferences(self,root[i]);
        root[i]=tmp;
      }
    }
    if(typeof(i)=="object")
    {
      tmp=JSONRpcClient.makeCallableReference(self,i);
      if(tmp)
      {
        value=root[i];
        delete root[i];
        root[tmp]=value;
      }
      else
      {
        tmp=JSONRpcClient.extractCallableReferences(self,i);
        value=root[i];
        delete root[i];
        root[tmp]=value;
      }
    }
  }
  return root;
};

JSONRpcClient.makeCallableReference = function(self,value)
{
  if(value && value.objectID && value.javaClass && value.JSONRPCType == "CallableReference")
  {
    return new JSONRpcClient(self.serverURL, self.user, self.pass, value.objectID,value.javaClass,value.JSONRPCType);
  }
  return null;
};

/* XMLHttpRequest wrapper code */

/* XMLHttpRequest pool globals */
JSONRpcClient.http_spare = [];
JSONRpcClient.http_max_spare = 8;

JSONRpcClient.poolGetHTTPRequest = function ()
{
  if (JSONRpcClient.http_spare.length > 0)
  {
    return JSONRpcClient.http_spare.pop();
  }
  return JSONRpcClient.getHTTPRequest();
};

JSONRpcClient.poolReturnHTTPRequest = function (http)
{
  if (JSONRpcClient.http_spare.length >= JSONRpcClient.http_max_spare)
  {
    delete http;
  }
  else
  {
    JSONRpcClient.http_spare.push(http);
  }
};

/* the search order here may seem strange, but it's
   actually what Microsoft recommends */
JSONRpcClient.msxmlNames = [
  "MSXML2.XMLHTTP.6.0",
  "MSXML2.XMLHTTP.3.0",
  "MSXML2.XMLHTTP",
  "MSXML2.XMLHTTP.5.0",
  "MSXML2.XMLHTTP.4.0",
  "Microsoft.XMLHTTP" ];

JSONRpcClient.getHTTPRequest = function ()
{
  /* Look for a browser native XMLHttpRequest implementation (Mozilla/IE7/Opera/Safari, etc.) */
  try
  {
    JSONRpcClient.httpObjectName = "XMLHttpRequest";
    return new XMLHttpRequest();
  }
  catch(e)
  {
  }

  /* Microsoft MSXML ActiveX for IE versions < 7 */
  for (var i = 0; i < JSONRpcClient.msxmlNames.length; i++)
  {
    try
    {
      JSONRpcClient.httpObjectName = JSONRpcClient.msxmlNames[i];
      return new ActiveXObject(JSONRpcClient.msxmlNames[i]);
    }
    catch (e)
    {
    }
  }

  /* None found */
  JSONRpcClient.httpObjectName = null;
  throw new JSONRpcClient.Exception(0, "Can't create XMLHttpRequest object");
};